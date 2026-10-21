package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeDTO(
    @NotBlank(message = "Current password is required") String currentPassword,
    @NotBlank(message = "New password is required")
    @Size(min = 4, max = 100, message = "New password must be between 4 and 100 characters")
    String newPassword
) {}
