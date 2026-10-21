package com.tamdan.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "application.runtime")
public record RuntimeConfigurationProperties(@NotBlank String applicationName, @NotBlank String serverPort) {}
