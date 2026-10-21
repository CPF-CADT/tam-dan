package com.tamdan.dto.response;

import java.time.Instant;

public record ConsentResponse(Long id, Long childId, String documentVersion, String actorType, Instant acknowledgedAt, String permissionState) {}
