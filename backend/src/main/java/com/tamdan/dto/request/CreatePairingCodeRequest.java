package com.tamdan.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreatePairingCodeRequest(@NotNull Long childId) {}
