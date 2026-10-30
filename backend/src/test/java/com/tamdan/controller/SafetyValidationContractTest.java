package com.tamdan.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.tamdan.dto.SafetyDto.PolicyAcknowledgementRequest;
import com.tamdan.dto.SafetyDto.PolicyRequest;
import com.tamdan.dto.request.DeviceHealthUpdateRequest;
import com.tamdan.entities.PolicyDelivery;
import com.tamdan.entities.SafetyPolicy;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SafetyValidationContractTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void policyTargetRejectsControlCharactersThatCouldBreakDownstreamQueriesOrLogs() {
        var request = new PolicyRequest(SafetyPolicy.Type.DOMAIN, "example.com\nOR 1=1");

        assertThat(validator.validate(request)).extracting(error -> error.getPropertyPath().toString()).contains("target");
    }

    @Test
    void unsupportedPolicyAcknowledgementIsAValidExplicitCapabilityState() {
        var request = new PolicyAcknowledgementRequest(1L, PolicyDelivery.State.UNSUPPORTED, Instant.parse("2026-10-30T03:04:05Z"), null);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.status()).isEqualTo(PolicyDelivery.State.UNSUPPORTED);
    }

    @Test
    void failedPolicyAcknowledgementRequiresAReason() {
        var request = new PolicyAcknowledgementRequest(1L, PolicyDelivery.State.FAILED, Instant.parse("2026-10-30T03:04:05Z"), null);

        assertThat(validator.validate(request)).extracting(error -> error.getPropertyPath().toString()).contains("failureReasonWhenFailed");
    }

    @Test
    void deviceHealthKeepsUnsupportedAsAnExplicitMonitoringState() {
        assertThat(DeviceHealthUpdateRequest.MonitoringState.valueOf("UNSUPPORTED"))
            .isEqualTo(DeviceHealthUpdateRequest.MonitoringState.UNSUPPORTED);
    }
}
