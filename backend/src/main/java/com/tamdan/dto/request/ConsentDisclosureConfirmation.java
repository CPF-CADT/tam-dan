package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ConsentDisclosureConfirmation(@NotBlank @Size(max = 30) String version, @NotNull Instant confirmedAt) {}
