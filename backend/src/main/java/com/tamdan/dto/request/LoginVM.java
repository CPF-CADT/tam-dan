package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginVM(
    @NotBlank(message = "Username is required") @Size(max = 254, message = "Username must not exceed 254 characters") String username,
    @NotBlank(message = "Password is required") @Size(min = 4, max = 100, message = "Password must be between 4 and 100 characters") String password,
    boolean rememberMe
) {}
