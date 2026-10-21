package com.tamdan.service;

import com.tamdan.dto.request.CompletePairingRequest;
import com.tamdan.dto.response.ChildContextResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.dto.response.PairingCodeResponse;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.DeviceCredential;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.Family;
import com.tamdan.entities.PairingCode;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ReplayedPairingCodeException;
import com.tamdan.exception.ConflictException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.DeviceCredentialRepository;
import com.tamdan.repository.PairingCodeRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class PairingService {

    private static final int CODE_BOUND = 1_000_000;
    private static final int CODE_LENGTH = 6;
    private static final long CODE_VALIDITY_MINUTES = 10;
    private static final long DEVICE_CREDENTIAL_VALIDITY_DAYS = 365;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final FamilyService familyService;
    private final ChildProfileRepository childProfileRepository;
    private final ChildDeviceRepository childDeviceRepository;
    private final DeviceCredentialRepository deviceCredentialRepository;
    private final PairingCodeRepository pairingCodeRepository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;
    private final RateLimitPolicy securityPolicy;

    @Transactional
    public PairingCodeResponse createPairingCode(Long parentId, Long childId) {
        if (rateLimitService != null) rateLimitService.check(RateLimitService.Operation.PAIRING_GENERATION, String.valueOf(parentId));
        Family family = familyService.requireFamilyForParent(parentId);
        ChildProfile child = childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        Instant now = Instant.now();
        if (pairingCodeRepository.findActiveForChild(child.getId(), now).isPresent()) {
            throw new ConflictException("An active pairing code already exists for this child");
        }

        String rawCode = generateCode();
        Instant expiresAt = now.plus(CODE_VALIDITY_MINUTES, ChronoUnit.MINUTES);
        pairingCodeRepository.saveAndFlush(PairingCode.create(family, child, hashPairingCode(rawCode), expiresAt, parentId, now));
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", parentId, family.getId(), "pairing-code-creation", "CHILD", childId, "SUCCESS", null, null, null, java.util.Map.of()));
        return new PairingCodeResponse(rawCode, expiresAt);
    }

    @Transactional
    public DeviceCredentialResponse completePairing(CompletePairingRequest request) {
        if (rateLimitService != null) rateLimitService.check(RateLimitService.Operation.PAIRING_SUBMISSION, hashPairingCode(request.pairingCode()));
        PairingCode pairingCode;
        try {
            pairingCode = requireUsableCode(request.pairingCode());
        } catch (RuntimeException exception) {
            if (auditService != null) auditService.recordFailure(new com.tamdan.dto.request.AuditCommand("DEVICE", null, null, "pairing-submission", "PAIRING_CODE", null, "FAILURE", null, null, null, java.util.Map.of()));
            throw exception;
        }
        Instant now = Instant.now();
        if (pairingCodeRepository.markUsed(pairingCode.getId(), now) != 1) {
            if (auditService != null) auditService.recordFailure(new com.tamdan.dto.request.AuditCommand("DEVICE", null, pairingCode.getFamily().getId(), "pairing-submission", "PAIRING_CODE", pairingCode.getId(), "FAILURE", null, null, null, java.util.Map.of()));
            throw new ReplayedPairingCodeException();
        }
        String capabilitiesJson;
        try {
            capabilitiesJson = objectMapper.writeValueAsString(request.capabilities());
        } catch (JacksonException exception) {
            throw new BadRequestException("Device capabilities are invalid");
        }
        ChildProfile child = pairingCode.getChildProfile();
        String deviceIdentifierHash = hashPairingCode(request.deviceIdentifier().trim());
        if (childDeviceRepository.existsByDeviceIdentifierHash(deviceIdentifierHash)) {
            throw new ConflictException("Device identifier is already registered");
        }
        ChildDevice device;
        try {
            device = childDeviceRepository.saveAndFlush(
                ChildDevice.create(
                child,
                deviceIdentifierHash,
                request.platform().trim(),
                request.appVersion().trim(),
                request.displayName().trim(),
                capabilitiesJson,
                now
            )
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Device identifier is already registered");
        }
        String credential = generateCredential();
        Instant expiresAt = now.plus(DEVICE_CREDENTIAL_VALIDITY_DAYS, ChronoUnit.DAYS);
        deviceCredentialRepository.saveAndFlush(DeviceCredential.create(device, hashPairingCode(credential), now, expiresAt));
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("DEVICE", device.getId(), pairingCode.getFamily().getId(), "pairing-completion", "DEVICE", device.getId(), "SUCCESS", null, null, null, java.util.Map.of()));
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("DEVICE", device.getId(), pairingCode.getFamily().getId(), "pairing-submission", "PAIRING_CODE", pairingCode.getId(), "SUCCESS", null, null, null, java.util.Map.of()));
        return new DeviceCredentialResponse(device.getId(), credential, expiresAt, new ChildContextResponse(child.getId(), child.getDisplayName(), pairingCode.getFamily().getId()));
    }

    @Transactional(readOnly = true)
    public PairingCode requireUsableCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) throw new BadRequestException("Pairing code is invalid or expired");
        String codeHash = hashPairingCode(rawCode.trim());
        PairingCode candidate = pairingCodeRepository.findByCodeHash(codeHash).orElse(null);
        if (candidate != null && candidate.getFailedAttempts() >= securityPolicy.pairingMaxAttempts()) {
            throw new BadRequestException("Pairing code is invalid or expired");
        }
        if (candidate != null && candidate.getUsedAt() == null && !candidate.getExpiresAt().isAfter(Instant.now())) {
            pairingCodeRepository.incrementFailedAttempts(candidate.getId());
        }
        return pairingCodeRepository.findUsableByCodeHash(codeHash, Instant.now()).orElseGet(() ->
            pairingCodeRepository.findByCodeHash(codeHash).map(pairing -> {
                if (pairing.getUsedAt() != null) throw new ReplayedPairingCodeException();
                throw new BadRequestException("Pairing code is invalid or expired");
            }).orElseThrow(() -> new BadRequestException("Pairing code is invalid or expired")));
    }

    public String hashPairingCode(String rawCode) {
        if (rawCode == null) throw new BadRequestException("Pairing code is invalid");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawCode.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String generateCode() {
        return String.format(Locale.ROOT, "%0" + CODE_LENGTH + "d", SECURE_RANDOM.nextInt(CODE_BOUND));
    }

    private String generateCredential() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
