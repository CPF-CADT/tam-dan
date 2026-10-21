package com.tamdan.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @JsonAlias("username") @NotBlank @Size(max = 254) String login,
    @NotBlank @Size(min = 4, max = 100) String password
) {}
