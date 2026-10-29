package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ConsentDecisionRequest(
    @NotBlank @Size(max = 30) String version,
    @NotNull Decision decision,
    @NotNull Instant decidedAt,
    @Size(max = 128) String disclosureHash
) {
    public enum Decision {
        GRANTED,
        WITHDRAWN,
    }
}
