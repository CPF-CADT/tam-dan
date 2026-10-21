package com.tamdan.dto.response;

import java.time.Instant;
import java.util.Set;

public record AdminUserDTO(
    Long id,
    String login,
    String firstName,
    String lastName,
    String email,
    String imageUrl,
    boolean activated,
    String langKey,
    String createdBy,
    Instant createdDate,
    String lastModifiedBy,
    Instant lastModifiedDate,
    Set<String> authorities
) {}
