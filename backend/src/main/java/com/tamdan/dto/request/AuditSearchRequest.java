package com.tamdan.dto.request;

import java.time.Instant;
import org.springframework.data.domain.Pageable;

public record AuditSearchRequest(Long familyId, String action, String resourceType, Instant from, Instant to, Pageable pageable) {}
