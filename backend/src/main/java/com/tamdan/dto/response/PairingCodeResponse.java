package com.tamdan.dto.response;

import java.time.Instant;

public record PairingCodeResponse(String code, Instant expiresAt) {}
