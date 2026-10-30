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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

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

    @Test
    void v1ParentAndDeviceRoutesUseExpectedPrefixesAndMethods() throws Exception {
        assertThat(V1AuthResource.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1/auth");
        assertMethodPath(V1AuthResource.class, "register", PostMapping.class, "/register", com.tamdan.dto.request.RegisterParentRequest.class);
        assertMethodPath(V1AuthResource.class, "login", PostMapping.class, "/login", com.tamdan.dto.request.LoginRequest.class);
        assertMethodPath(V1AuthResource.class, "refresh", PostMapping.class, "/refresh", com.tamdan.dto.request.RefreshRequest.class);
        assertMethodPath(V1AuthResource.class, "logout", PostMapping.class, "/logout", com.tamdan.dto.request.RefreshRequest.class);
        assertMethodPath(V1AuthResource.class, "logoutAll", PostMapping.class, "/logout-all");

        assertThat(V1FamilyResource.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1/families/me/children");
        assertMethodPath(V1FamilyResource.class, "listChildren", GetMapping.class, "");
        assertMethodPath(V1FamilyResource.class, "createChild", PostMapping.class, "", com.tamdan.dto.request.CreateChildRequest.class);
        assertMethodPath(V1FamilyResource.class, "updateChild", PatchMapping.class, "/{childId}", Long.class, com.tamdan.dto.request.UpdateChildV1Request.class);
        assertMethodPath(V1FamilyResource.class, "deactivateChild", PostMapping.class, "/{childId}/deactivation", Long.class);

        assertThat(V1DeviceResource.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1");
        assertMethodPath(V1DeviceResource.class, "list", GetMapping.class, "/families/me/children/{childId}/devices", Long.class);
        assertMethodPath(V1DeviceResource.class, "rename", PatchMapping.class, "/families/me/children/{childId}/devices/{deviceId}", Long.class, Long.class, com.tamdan.dto.request.RenameDeviceV1Request.class);
        assertMethodPath(V1DeviceResource.class, "revoke", PostMapping.class, "/families/me/children/{childId}/devices/{deviceId}/revocation", Long.class, Long.class);
        assertMethodPath(V1DeviceResource.class, "rotate", PostMapping.class, "/families/me/children/{childId}/devices/{deviceId}/credential-rotation", Long.class, Long.class);
        assertMethodPath(V1DeviceResource.class, "updateHealth", PutMapping.class, "/devices/me/health", com.tamdan.dto.request.DeviceHealthUpdateRequest.class);
        assertMethodPath(V1DeviceResource.class, "getHealth", GetMapping.class, "/families/me/children/{childId}/devices/{deviceId}/health", Long.class, Long.class);

        assertThat(V1PairingResource.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1");
        assertMethodPath(V1PairingResource.class, "createCode", PostMapping.class, "/families/me/children/{childId}/pairing-codes", Long.class);
        assertMethodPath(V1PairingResource.class, "complete", PostMapping.class, "/pairing/complete", com.tamdan.dto.request.CompletePairingRequest.class);

        assertThat(V1ConsentResource.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/v1");
        assertMethodPath(V1ConsentResource.class, "get", GetMapping.class, "/families/me/children/{childId}/consent", Long.class);
        assertMethodPath(V1ConsentResource.class, "put", PutMapping.class, "/families/me/children/{childId}/consent", Long.class, com.tamdan.dto.request.ConsentDecisionRequest.class);
        assertMethodPath(V1ConsentResource.class, "confirm", PostMapping.class, "/devices/me/consent", com.tamdan.dto.request.ConsentDisclosureConfirmation.class);
    }

    @Test
    void safetyRoutesDeclareSeparateParentAndDeviceAuthorities() {
        assertThat(SafetyPolicyResource.class.getAnnotation(PreAuthorize.class).value())
            .isEqualTo("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')");
        assertThat(AlertResource.class.getAnnotation(PreAuthorize.class).value())
            .isEqualTo("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')");
        assertThat(DeviceSafetyResource.class.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('DEVICE')");
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
        if (expectedPath.isEmpty()) assertThat(paths).isEmpty();
        else assertThat(paths).containsExactly(expectedPath);
    }
}
