package com.tamdan.dto.response;

import java.time.Instant;

public record TokenResponse(String accessToken, String refreshToken, Instant accessTokenExpiresAt, Instant refreshTokenExpiresAt) {}
