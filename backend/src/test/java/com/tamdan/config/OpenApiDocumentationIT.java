package com.tamdan.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamdan.IntegrationTest;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@ActiveProfiles({ "test", "api-docs" })
class OpenApiDocumentationIT {

    private static final Map<String, Set<String>> APPROVED_V1_OPERATIONS = Map.ofEntries(
        Map.entry("/api/v1/auth/register", Set.of("post")),
        Map.entry("/api/v1/auth/login", Set.of("post")),
        Map.entry("/api/v1/auth/refresh", Set.of("post")),
        Map.entry("/api/v1/auth/logout", Set.of("post")),
        Map.entry("/api/v1/auth/logout-all", Set.of("post")),
        Map.entry("/api/v1/families/me/children", Set.of("get", "post")),
        Map.entry("/api/v1/families/me/children/{childId}", Set.of("patch")),
        Map.entry("/api/v1/families/me/children/{childId}/deactivation", Set.of("post")),
        Map.entry("/api/v1/families/me/children/{childId}/pairing-codes", Set.of("post")),
        Map.entry("/api/v1/pairing/complete", Set.of("post")),
        Map.entry("/api/v1/families/me/children/{childId}/devices", Set.of("get")),
        Map.entry("/api/v1/families/me/children/{childId}/devices/{deviceId}", Set.of("patch")),
        Map.entry("/api/v1/families/me/children/{childId}/devices/{deviceId}/revocation", Set.of("post")),
        Map.entry("/api/v1/families/me/children/{childId}/devices/{deviceId}/credential-rotation", Set.of("post")),
        Map.entry("/api/v1/devices/me/health", Set.of("put")),
        Map.entry("/api/v1/families/me/children/{childId}/devices/{deviceId}/health", Set.of("get")),
        Map.entry("/api/v1/families/me/children/{childId}/consent", Set.of("get", "put")),
        Map.entry("/api/v1/devices/me/consent", Set.of("post")),
        Map.entry("/api/v1/devices/me/activity-events", Set.of("post")),
        Map.entry("/api/v1/devices/me/domain-observations", Set.of("post")),
        Map.entry("/api/v1/devices/me/policies", Set.of("get")),
        Map.entry("/api/v1/devices/me/policies/{policyId}/acknowledgements", Set.of("post")),
        Map.entry("/api/v1/families/me/children/{childId}/reports/usage", Set.of("get")),
        Map.entry("/api/v1/families/me/children/{childId}/alerts", Set.of("get")),
        Map.entry("/api/v1/alerts/{alertId}/review", Set.of("post")),
        Map.entry("/api/v1/alerts/{alertId}/dismissal", Set.of("post")),
        Map.entry("/api/v1/families/me/children/{childId}/policies", Set.of("get", "post")),
        Map.entry("/api/v1/families/me/children/{childId}/policies/{policyId}", Set.of("patch", "delete")),
        Map.entry("/api/v1/families/me/children/{childId}/policies/{policyId}/pause", Set.of("post")),
        Map.entry("/api/v1/families/me/children/{childId}/policies/{policyId}/delivery", Set.of("get"))
    );

    private static final Set<String> LEGACY_API_PATHS = Set.of(
        "/api/register",
        "/api/activate",
        "/api/activate/resend",
        "/api/account",
        "/api/account/change-password",
        "/api/account/reset-password/init",
        "/api/account/reset-password/finish",
        "/api/authenticate",
        "/api/authenticate/refresh",
        "/api/authenticate/logout",
        "/api/authenticate/logout-all",
        "/api/admin/users",
        "/api/admin/users/{login}",
        "/api/authorities",
        "/api/authorities/{id}",
        "/api/children",
        "/api/children/{childId}",
        "/api/family",
        "/api/family/current",
        "/api/consent/children/{childId}",
        "/api/consent/device",
        "/api/device/credential",
        "/api/device/credential/rotate",
        "/api/devices",
        "/api/devices/{deviceId}",
        "/api/devices/{deviceId}/name",
        "/api/devices/{deviceId}/credential/rotate",
        "/api/devices/{deviceId}/credential",
        "/api/pairing-codes",
        "/api/pairing/complete",
        "/api/users",
        "/api/audit"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exposesOnlyApprovedVersionedOperationsWithExplicitSecuritySchemes() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode document = objectMapper.readTree(body);

        assertThat(document.path("info").path("version").asText()).isEqualTo("1.2.0");
        assertThat(document.path("components").path("securitySchemes").fieldNames()).toIterable().containsExactlyInAnyOrder(
            "parentJwt",
            "deviceCredential"
        );

        JsonNode paths = document.path("paths");
        assertThat(paths.size()).isEqualTo(62);
        assertThat(paths.fieldNames()).toIterable().containsAll(APPROVED_V1_OPERATIONS.keySet()).containsAll(LEGACY_API_PATHS);
        assertThat(paths.fieldNames()).toIterable().allMatch(path -> path.startsWith("/api/"));
        assertThat(paths.elements()).toIterable().allMatch(path -> path.fieldNames().hasNext());
        assertThat(paths.findValues("operationId")).hasSize(79);
        APPROVED_V1_OPERATIONS.forEach((path, methods) -> {
            JsonNode pathNode = paths.path(path);
            assertThat(pathNode.fieldNames()).toIterable().containsExactlyInAnyOrderElementsOf(methods);
            methods.forEach(method -> assertThat(pathNode.path(method).path("operationId").asText()).isNotBlank());
        });
    }
}
