package com.tamdan.security;

import java.time.Instant;

public record DeviceRevocationEvent(Long deviceId, Long childProfileId, Long familyId, Instant revokedAt, String reason) {}
