package com.tamdan.dto.response;

import java.time.Instant;

public record DeviceCredentialResponse(Long deviceId, String credential, Instant expiresAt, ChildContextResponse child) {}
