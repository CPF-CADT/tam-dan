package com.tamdan.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.tamdan.dto.SafetyDto.AlertResponse;
import com.tamdan.dto.SafetyDto.PolicyResponse;
import com.tamdan.dto.SafetyDto.SourceDeviceCoverageResponse;
import com.tamdan.dto.SafetyDto.UsageReportResponse;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.ChildV1Response;
import com.tamdan.dto.response.DeviceV1Response;
import com.tamdan.dto.response.PairingCompletionResponse;
import com.tamdan.entities.SafetyPolicy;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class SafetyDtoContractTest {

    private static final Instant NOW = Instant.parse("2026-10-30T03:04:05Z");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void usageReportIsWrappedAndContainsOnlyContractFields() throws Exception {
        UsageReportResponse report = new UsageReportResponse(
            3L,
            NOW.minusSeconds(3600),
            NOW,
            NOW,
            NOW,
            "FRESH",
            "COMPLETE",
            null,
            List.of(new SourceDeviceCoverageResponse(7L, NOW, NOW, "COMPLETE", null)),
            "SAFE"
        );

        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(ApiResponse.success(report))).get("data").properties())
            .extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder(
                "childId", "from", "to", "generatedAt", "dataThrough", "freshness", "completeness", "unknownReason", "sourceDeviceCoverage", "risk"
            );
    }

    @Test
    void alertAndPolicyResponsesDoNotExposeEntitiesOrSecrets() throws Exception {
        AlertResponse alert = new AlertResponse(1L, 2L, "OPEN", "HIGH", "Risk", "Review", NOW, null);
        PolicyResponse policy = new PolicyResponse(4L, 2L, 1L, SafetyPolicy.Type.DOMAIN, "example.com", SafetyPolicy.State.UNSUPPORTED, NOW);

        String json = objectMapper.writeValueAsString(ApiResponse.success(List.of(alert, policy)));

        assertThat(json).doesNotContain("password", "token", "credential", "family", "entity");
        assertThat(objectMapper.readTree(json).get("data").get(0).properties()).extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("id", "childId", "state", "severity", "title", "detail", "createdAt", "reviewedAt");
        assertThat(objectMapper.readTree(json).get("data").get(1).properties()).extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("id", "childId", "version", "type", "target", "state", "updatedAt");
    }

    @Test
    void pairingResponseContainsCredentialOnlyAtTheExplicitIssuanceBoundary() throws Exception {
        PairingCompletionResponse response = new PairingCompletionResponse(
            new DeviceV1Response(7L, 2L, "phone", "ACTIVE", "android", Map.of("vpnEnforcement", false)),
            "one-time-credential"
        );

        var data = objectMapper.readTree(objectMapper.writeValueAsString(ApiResponse.success(response))).get("data");
        assertThat(data.properties()).extracting(Map.Entry::getKey).containsExactlyInAnyOrder("device", "credential");
        assertThat(data.get("device").properties()).extracting(Map.Entry::getKey)
            .containsExactlyInAnyOrder("id", "childId", "displayName", "status", "platform", "capabilities");
    }

    @Test
    void childResponseIsAFlatDto() throws Exception {
        var data = objectMapper.readTree(objectMapper.writeValueAsString(ApiResponse.success(new ChildV1Response(2L, "Child", "ACTIVE")))).get("data");

        assertThat(data.properties()).extracting(Map.Entry::getKey).containsExactlyInAnyOrder("id", "displayName", "status");
    }
}
