package com.tamdan.dto.request;

import com.tamdan.config.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterParentRequest(
    @NotBlank(message = "Login is required")
    @Pattern(regexp = Constants.LOGIN_REGEX, message = "Login format is invalid")
    @Size(max = 50, message = "Login must not exceed 50 characters")
    String login,
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    String email,
    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100, message = "Password must be between 4 and 100 characters")
    String password,
    @Size(max = 50, message = "First name must not exceed 50 characters") String firstName,
    @Size(max = 50, message = "Last name must not exceed 50 characters") String lastName
) {}
