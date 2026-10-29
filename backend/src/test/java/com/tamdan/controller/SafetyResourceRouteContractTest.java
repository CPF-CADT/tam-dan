package com.tamdan.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SafetyResourceRouteContractTest {

    @Test
    void safetyPolicyRoutesUseVersionedContractPrefix() throws Exception {
        assertThat(SafetyPolicyResource.class.getAnnotation(RequestMapping.class).value())
            .containsExactly("/api/v1/families/me/children");
        assertMethodPath(SafetyPolicyResource.class, "usageReport", GetMapping.class, "/{childId}/reports/usage", Long.class, Instant.class, Instant.class);
        assertMethodPath(SafetyPolicyResource.class, "alerts", GetMapping.class, "/{childId}/alerts", Long.class, int.class, int.class);
        assertMethodPath(SafetyPolicyResource.class, "policies", GetMapping.class, "/{childId}/policies", Long.class, int.class, int.class);
        assertMethodPath(SafetyPolicyResource.class, "createPolicy", PostMapping.class, "/{childId}/policies", Long.class, com.tamdan.dto.SafetyDto.PolicyRequest.class);
        assertMethodPath(SafetyPolicyResource.class, "updatePolicy", PatchMapping.class, "/{childId}/policies/{policyId}", Long.class, Long.class, com.tamdan.dto.SafetyDto.PolicyRequest.class);
        assertMethodPath(SafetyPolicyResource.class, "removePolicy", DeleteMapping.class, "/{childId}/policies/{policyId}", Long.class, Long.class);
        assertMethodPath(SafetyPolicyResource.class, "pausePolicy", PostMapping.class, "/{childId}/policies/{policyId}/pause", Long.class, Long.class);
        assertMethodPath(SafetyPolicyResource.class, "delivery", GetMapping.class, "/{childId}/policies/{policyId}/delivery", Long.class, Long.class);
    }

    @Test
    void alertRoutesUseVersionedContractPrefix() throws Exception {
        assertThat(AlertResource.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1/alerts");
        assertMethodPath(AlertResource.class, "review", PostMapping.class, "/{alertId}/review", Long.class, com.tamdan.dto.SafetyDto.AlertReviewRequest.class);
        assertMethodPath(AlertResource.class, "dismiss", PostMapping.class, "/{alertId}/dismissal", Long.class);
    }

    @Test
    void deviceSafetyRoutesUseVersionedContractPrefix() throws Exception {
        assertThat(DeviceSafetyResource.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1/devices/me");
        assertMethodPath(DeviceSafetyResource.class, "ingestActivity", PostMapping.class, "/activity-events", com.tamdan.dto.SafetyDto.ActivityBatchRequest.class);
        assertMethodPath(DeviceSafetyResource.class, "ingestDomain", PostMapping.class, "/domain-observations", com.tamdan.dto.SafetyDto.ActivityBatchRequest.class);
        assertMethodPath(DeviceSafetyResource.class, "downloadPolicies", GetMapping.class, "/policies");
        assertMethodPath(
            DeviceSafetyResource.class,
            "acknowledge",
            PostMapping.class,
            "/policies/{policyId}/acknowledgements",
            Long.class,
            com.tamdan.dto.SafetyDto.PolicyAcknowledgementRequest.class
        );
    }

    private void assertMethodPath(
        Class<?> resource,
        String methodName,
        Class<? extends Annotation> mappingType,
        String expectedPath,
        Class<?>... parameterTypes
    )
        throws Exception {
        Method method = resource.getDeclaredMethod(methodName, parameterTypes);
        Annotation mapping = method.getAnnotation(mappingType);
        String[] paths = (String[]) mappingType.getMethod("value").invoke(mapping);
        assertThat(paths).containsExactly(expectedPath);
    }
}
