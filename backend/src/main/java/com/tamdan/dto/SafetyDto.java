package com.tamdan.dto;

import com.tamdan.entities.PolicyDelivery;
import com.tamdan.entities.SafetyAlert;
import com.tamdan.entities.SafetyPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class SafetyDto {
    private SafetyDto() {}

    public record EventRequest(
        @NotBlank @Size(max = 100) String eventId,
        @NotNull Instant occurredAt,
        JsonNode payload
    ) {}

    public record ActivityBatchRequest(
        @NotBlank @Size(max = 30) String schemaVersion,
        @NotNull @Size(min = 1, max = 500) List<@Valid EventRequest> events
    ) {}

    public record IngestionResultResponse(int accepted, int duplicate, int rejected) {}

    public record SourceDeviceCoverageResponse(
        long deviceId,
        Instant expectedThrough,
        Instant observedThrough,
        String status,
        String missingReason
    ) {}

    public record UsageReportResponse(
        long childId,
        Instant from,
        Instant to,
        Instant generatedAt,
        Instant dataThrough,
        String freshness,
        String completeness,
        String unknownReason,
        List<SourceDeviceCoverageResponse> sourceDeviceCoverage,
        String risk
    ) {}

    public record AlertReviewRequest(
        @NotNull @Pattern(regexp = "REVIEWED") String state,
        @Size(max = 500) String note
    ) {}

    public record AlertResponse(
        long id,
        long childId,
        String state,
        String severity,
        String title,
        String detail,
        Instant createdAt,
        Instant reviewedAt
    ) {}

    public record AlertPageResponse(List<AlertResponse> items, int page, int pageSize, long total) {}

    public record PolicyRequest(
        @NotNull SafetyPolicy.Type type,
        @NotBlank @Size(max = 255) @Pattern(regexp = "^[^\\u0000-\\u001F\\u007F]+$") String target
    ) {}

    public record PolicyResponse(long id, long childId, long version, SafetyPolicy.Type type, String target, SafetyPolicy.State state, Instant updatedAt) {}

    public record PolicyPageResponse(List<PolicyResponse> items, int page, int pageSize, long total) {}

    public record PolicyAcknowledgementRequest(
        @Min(1) long policyVersion,
        @NotNull PolicyDelivery.State status,
        @NotNull Instant reportedAt,
        @Size(max = 500) String failureReason
    ) {
        @AssertTrue(message = "failureReason is required when status is FAILED")
        public boolean hasFailureReasonWhenFailed() {
            return status != PolicyDelivery.State.FAILED || (failureReason != null && !failureReason.isBlank());
        }
    }

    public record DevicePolicyDeliveryResponse(long deviceId, PolicyDelivery.State state, Long acknowledgedVersion, Instant reportedAt, String failureReason) {}

    public record PolicyDeliveryResponse(long policyId, long policyVersion, String state, List<DevicePolicyDeliveryResponse> devices) {}
}
