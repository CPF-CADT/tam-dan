package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DevicePermissionRequest(@NotBlank @Size(max = 30) String permissionState) {}
