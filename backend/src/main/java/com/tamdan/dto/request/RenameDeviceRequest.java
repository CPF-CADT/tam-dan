package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameDeviceRequest(@NotBlank @Size(max = 100) String name) {}
