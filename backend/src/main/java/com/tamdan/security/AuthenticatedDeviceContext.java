package com.tamdan.security;

/**
 * Trusted identity context for an authenticated child device.
 */
public record AuthenticatedDeviceContext(Long deviceId, Long childProfileId, Long familyId) {}
