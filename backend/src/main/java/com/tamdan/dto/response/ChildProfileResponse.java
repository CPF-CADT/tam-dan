package com.tamdan.dto.response;

import java.time.Instant;

public record ChildProfileResponse(Long id, String displayName, Boolean active, Instant createdAt, Instant updatedAt) {}
