package com.tamdan.service.device;

import com.tamdan.service.consent.AuditService;
import com.tamdan.dto.response.ChildContextResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.DeviceCredential;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.DeviceCredentialRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceCredentialService {

    private static final long CREDENTIAL_VALIDITY_DAYS = 365;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DeviceCredentialRepository deviceCredentialRepository;
    private final ChildDeviceRepository childDeviceRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public AuthenticatedDeviceContext authenticate(String rawCredential) {
        if (rawCredential == null || rawCredential.isBlank()) throw new BadRequestException("Device credential is invalid");
        DeviceCredential credential = deviceCredentialRepository.findActiveByCredentialHash(hash(rawCredential.trim()), Instant.now()).orElseThrow(() ->
            new BadRequestException("Device credential is invalid or expired"));
        ChildDevice device = credential.getChildDevice();
        if (!Boolean.TRUE.equals(device.getActive()) || device.getRevokedAt() != null || !Boolean.TRUE.equals(device.getChildProfile().getActive()) ||
            !Boolean.TRUE.equals(device.getChildProfile().getFamily().getActive())) {
            throw new BadRequestException("Device credential is invalid or expired");
        }
        return new AuthenticatedDeviceContext(device.getId(), device.getChildProfile().getId(), device.getChildProfile().getFamily().getId());
    }

    @Transactional
    public DeviceCredentialResponse rotateCredential(Long deviceId) {
        DeviceCredential previous = deviceCredentialRepository.findFirstByChildDeviceIdAndRevokedAtIsNullOrderByIssuedAtDesc(deviceId).orElseThrow(() ->
            new ResourceNotFoundException("Device credential was not found"));
        validateDevice(previous.getChildDevice());
        Instant now = Instant.now();
        previous.replaceWith(null, now);
        deviceCredentialRepository.saveAndFlush(previous);
        String rawCredential = generateCredential();
        Instant expiresAt = now.plus(CREDENTIAL_VALIDITY_DAYS, ChronoUnit.DAYS);
        DeviceCredential replacement = deviceCredentialRepository.saveAndFlush(
            DeviceCredential.create(previous.getChildDevice(), hash(rawCredential), now, expiresAt)
        );
        previous.replaceWith(replacement.getId(), now);
        deviceCredentialRepository.save(previous);
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("DEVICE", deviceId, replacement.getChildDevice().getChildProfile().getFamily().getId(), "credential-rotation", "DEVICE", deviceId, "SUCCESS", null, null, null, java.util.Map.of()));
        return response(replacement, rawCredential, expiresAt);
    }

    @Transactional
    public void revokeCredential(Long deviceId) {
        validateActiveDevice(deviceId);
        revokeAllCredentials(deviceId);
    }

    @Transactional
    public void revokeAllCredentials(Long deviceId) {
        deviceCredentialRepository.revokeByDeviceId(deviceId, Instant.now());
    }

    @Transactional(readOnly = true)
    public void validateActiveDevice(Long deviceId) {
        ChildDevice device = childDeviceRepository.findById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device was not found"));
        validateDevice(device);
    }

    public String hashCredential(String rawCredential) {
        return hash(rawCredential);
    }

    private DeviceCredentialResponse response(DeviceCredential credential, String rawCredential, Instant expiresAt) {
        ChildDevice device = credential.getChildDevice();
        return new DeviceCredentialResponse(
            device.getId(),
            rawCredential,
            expiresAt,
            new ChildContextResponse(device.getChildProfile().getId(), device.getChildProfile().getDisplayName(), device.getChildProfile().getFamily().getId())
        );
    }

    private void validateDevice(ChildDevice device) {
        if (!Boolean.TRUE.equals(device.getActive()) || device.getRevokedAt() != null || !Boolean.TRUE.equals(device.getChildProfile().getActive()) ||
            !Boolean.TRUE.equals(device.getChildProfile().getFamily().getActive())) {
            throw new BadRequestException("Device is inactive or revoked");
        }
    }

    private String generateCredential() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawCredential) {
        if (rawCredential == null) throw new BadRequestException("Device credential is invalid");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawCredential.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
