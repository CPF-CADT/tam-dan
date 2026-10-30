package com.tamdan.service.consent;

import com.tamdan.dto.request.AuditCommand;
import com.tamdan.dto.request.AuditSearchRequest;
import com.tamdan.dto.response.AuditResponse;
import com.tamdan.entities.SecurityAuditEvent;
import com.tamdan.exception.ForbiddenException;
import com.tamdan.repository.FamilyMemberRepository;
import com.tamdan.repository.SecurityAuditEventRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import com.tamdan.security.SecurityUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Set<String> APPROVED_METADATA_KEYS = Set.of("source", "channel", "platform", "resultCode");

    private final SecurityAuditEventRepository auditRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(AuditCommand command) {
        persist(command);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(AuditCommand command) {
        persist(command);
    }

    private void persist(AuditCommand command) {
        auditRepository.save(SecurityAuditEvent.create(
            command.actorType(), command.actorId(), command.familyId(), command.action(), command.resourceType(), command.resourceId(),
            command.result(), command.requestId(), hashNullable(command.ipAddress()), hashNullable(command.userAgent()), safeMetadata(command.safeMetadata()), Instant.now()
        ));
    }

    @Transactional(readOnly = true)
    public Page<AuditResponse> searchAuthorized(AuditSearchRequest request) {
        if (request.familyId() == null || !authorizedForFamily(request.familyId())) throw new ForbiddenException("Audit access is not authorized");
        Pageable pageable = request.pageable() == null ? Pageable.unpaged() : request.pageable();
        return auditRepository.search(request.familyId(), request.action(), request.resourceType(), request.from(), request.to(), pageable).map(this::toResponse);
    }

    private boolean authorizedForFamily(Long familyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_ADMIN", "ADMIN")) return true;
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedDeviceContext context) return familyId.equals(context.familyId());
        return SecurityUtils.getCurrentUserId().map(parentId -> familyMemberRepository.existsByFamilyIdAndParentIdAndActiveTrue(familyId, parentId)).orElse(false);
    }

    private AuditResponse toResponse(SecurityAuditEvent event) {
        return new AuditResponse(event.getId(), event.getActorType(), event.getAction(), event.getResourceType(), event.getResult(), event.getOccurredAt());
    }

    private String safeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(metadata.entrySet().stream().filter(entry -> APPROVED_METADATA_KEYS.contains(entry.getKey())).filter(entry -> entry.getValue() != null && isSafeValue(entry.getValue())).collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)));
        } catch (JacksonException exception) {
            return "{}";
        }
    }

    private boolean isSafeValue(Object value) {
        if (!(value instanceof String string)) return value instanceof Number || value instanceof Boolean;
        String normalized = string.toLowerCase(java.util.Locale.ROOT);
        return !normalized.contains("password") && !normalized.contains("token") && !normalized.contains("credential") && !normalized.contains("pairing") && !normalized.contains("secret");
    }

    private String hashNullable(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
