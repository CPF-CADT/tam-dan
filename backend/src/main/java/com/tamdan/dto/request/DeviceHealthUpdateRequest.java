package com.tamdan.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record DeviceHealthUpdateRequest(
    @NotBlank @Size(max = 30) String schemaVersion,
    @NotNull Instant reportedAt,
    @NotNull MonitoringState monitoringState,
    @NotNull SyncState syncState,
    @Size(max = 100) String policyVersion,
    @Min(0) @jakarta.validation.constraints.Max(100000) Integer queuedEventCount
) {
    public enum MonitoringState {
        ACTIVE,
        INACTIVE,
        PERMISSION_DENIED,
        UNSUPPORTED,
        FAILED,
    }

    public enum SyncState {
        CURRENT,
        STALE,
        OFFLINE,
        FAILED,
    }
}
