package com.tamdan.dto.request;

import java.util.Map;

public record AuditCommand(
    String actorType, Long actorId, Long familyId, String action, String resourceType, Long resourceId,
    String result, String requestId, String ipAddress, String userAgent, Map<String, Object> safeMetadata
) {}
