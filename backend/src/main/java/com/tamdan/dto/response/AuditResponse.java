package com.tamdan.dto.response;

import java.time.Instant;

public record AuditResponse(Long id, String actorType, String action, String resourceType, String result, Instant occurredAt) {}
