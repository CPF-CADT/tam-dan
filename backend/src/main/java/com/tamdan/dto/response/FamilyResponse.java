package com.tamdan.dto.response;

import java.time.Instant;

public record FamilyResponse(Long id, Boolean active, Instant createdAt) {}
