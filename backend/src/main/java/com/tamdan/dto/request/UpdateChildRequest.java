package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateChildRequest(@NotBlank @Size(max = 100) String displayName, @NotNull Boolean active) {}
