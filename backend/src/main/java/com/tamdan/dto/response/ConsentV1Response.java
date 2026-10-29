package com.tamdan.dto.response;

import java.time.Instant;

public record ConsentV1Response(Long childId, String version, String decision, Instant decidedAt, Boolean confirmedByDevice) {}
