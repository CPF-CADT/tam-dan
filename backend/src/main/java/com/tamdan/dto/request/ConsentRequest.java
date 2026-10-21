package com.tamdan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsentRequest(@NotBlank @Size(max = 50) String documentVersion, @NotBlank @Size(max = 30) String permissionState) {}
