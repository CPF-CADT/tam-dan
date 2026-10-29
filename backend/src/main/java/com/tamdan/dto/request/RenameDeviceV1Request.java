package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameDeviceV1Request(@NotBlank @Size(max = 100) String displayName) {}
