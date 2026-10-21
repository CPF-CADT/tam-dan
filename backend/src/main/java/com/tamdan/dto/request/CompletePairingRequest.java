package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record CompletePairingRequest(
    @NotBlank @Size(max = 20) String pairingCode,
    @NotBlank @Size(max = 255) String deviceIdentifier,
    @NotBlank @Size(max = 30) String platform,
    @NotBlank @Size(max = 30) String appVersion,
    @NotBlank @Size(max = 100) String displayName,
    @NotNull Map<String, Object> capabilities
) {}
