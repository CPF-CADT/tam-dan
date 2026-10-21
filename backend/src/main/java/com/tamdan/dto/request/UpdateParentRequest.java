package com.tamdan.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateParentRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    String email,
    @Size(max = 50, message = "First name must not exceed 50 characters") String firstName,
    @Size(max = 50, message = "Last name must not exceed 50 characters") String lastName
) {}
