package com.tamdan.dto.request;

import com.tamdan.config.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AdminUserRequest(
    Long id,
    @NotBlank(message = "Login is required")
    @Pattern(regexp = Constants.LOGIN_REGEX, message = "Login format is invalid")
    @Size(max = 50, message = "Login must not exceed 50 characters")
    String login,
    @Size(max = 50, message = "First name must not exceed 50 characters") String firstName,
    @Size(max = 50, message = "Last name must not exceed 50 characters") String lastName,
    @Email(message = "Email must be valid") @Size(max = 254, message = "Email must not exceed 254 characters") String email,
    @Size(max = 256, message = "Image URL must not exceed 256 characters") String imageUrl,
    boolean activated,
    @Size(min = 2, max = 10, message = "Language key must be between 2 and 10 characters") String langKey,
    @NotNull(message = "Authorities are required") Set<String> authorities
) {}
