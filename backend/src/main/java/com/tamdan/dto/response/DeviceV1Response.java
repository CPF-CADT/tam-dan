package com.tamdan.dto.response;

import java.util.Map;

public record DeviceV1Response(Long id, Long childId, String displayName, String status, String platform, Map<String, Object> capabilities) {}
