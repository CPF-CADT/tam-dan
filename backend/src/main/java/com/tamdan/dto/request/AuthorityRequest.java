package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorityRequest(
    @NotBlank(message = "Authority name is required") @Size(max = 50, message = "Authority name must not exceed 50 characters") String name
) {}
