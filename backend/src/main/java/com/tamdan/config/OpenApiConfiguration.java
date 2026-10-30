package com.tamdan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("api-docs")
public class OpenApiConfiguration {

    private static final String JWT_SCHEME = "parentJwt";
    private static final String DEVICE_SCHEME = "deviceCredential";

    @Bean
    public OpenAPI tamDanOpenApi() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Tam Dan API")
                    .description("Tam Dan versioned API documentation. Legacy /api routes are not part of the v1 contract.")
                    .version("1.2.0")
            )
            .components(
                new Components()
                    .addSecuritySchemes(
                        JWT_SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                    )
                    .addSecuritySchemes(
                        DEVICE_SCHEME,
                        new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.HEADER)
                            .name("X-Device-Credential")
                            .description("Paired-device credential. Never log or return it after issuance.")
                    )
            );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public OpenApiCustomizer tamDanOpenApiCustomizer() {
        return openAPI -> {
            openAPI.getInfo().version("1.2.0");
            openAPI.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((method, operation) -> {
                String operationId = operation.getOperationId();
                if (operationId == null) return;
                String successStatus = successStatus(operationId);
                if ("200".equals(successStatus) && !isV1Operation(operationId)) successStatus = legacySuccessStatus(path, method.name());
                if (!"200".equals(successStatus)) operation.getResponses().remove("200");
                operation.getResponses().addApiResponse(successStatus, new ApiResponse().description(successDescription(operationId)));
                errorStatuses(operationId).forEach(status -> operation.getResponses().addApiResponse(status, problemResponse(status)));
                if (operation.getSecurity() == null) operation.security(securityFor(path, method.name()));
            }));
            openAPI.getComponents().addSchemas("Problem", problemSchema());
        };
    }

    private boolean isV1Operation(String operationId) {
        return APPROVED_V1_OPERATION_IDS.contains(operationId);
    }

    private static final Set<String> APPROVED_V1_OPERATION_IDS = Set.of(
        "registerParent", "loginParent", "refreshParent", "logoutParent", "logoutAllParentSessions", "listChildren", "createChild",
        "updateChild", "deactivateChild", "createPairingCode", "completeDevicePairing", "listDevices", "renameDevice", "revokeDevice",
        "rotateDeviceCredential", "updateDeviceHealth", "getDeviceHealth", "getChildConsent", "recordChildConsent",
        "confirmDeviceConsentDisclosure", "ingestActivity", "ingestDomainObservations", "downloadPolicies", "acknowledgePolicy",
        "getUsageReport", "listAlerts", "reviewAlert", "dismissAlert", "listPolicies", "createPolicy", "updatePolicy", "removePolicy",
        "pausePolicy", "getPolicyDeliveryStatus"
    );

    private String legacySuccessStatus(String path, String method) {
        if ("POST".equals(method) && Set.of("/api/register", "/api/children", "/api/family", "/api/pairing-codes", "/api/admin/users", "/api/authorities").contains(path)) return "201";
        if ("GET".equals(method) && "/api/authenticate".equals(path)) return "204";
        if ("DELETE".equals(method) && (path.startsWith("/api/admin/") || path.startsWith("/api/authorities/"))) return "204";
        return "200";
    }

    private List<SecurityRequirement> securityFor(String path, String method) {
        if (path.equals("/api/register") || path.equals("/api/activate") || path.equals("/api/activate/resend") ||
            path.startsWith("/api/account/reset-password/") || path.equals("/api/authenticate") && Set.of("POST").contains(method) ||
            path.equals("/api/authenticate/refresh") || path.equals("/api/authenticate/logout") || path.equals("/api/pairing/complete") ||
            path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/refresh") ||
            path.equals("/api/v1/pairing/complete")) return List.of();
        if (path.startsWith("/api/v1/devices/me/") || path.startsWith("/api/device/credential/") || path.equals("/api/consent/device") ||
            path.startsWith("/api/events/") || path.startsWith("/api/policies/")) return List.of(new SecurityRequirement().addList(DEVICE_SCHEME));
        return List.of(new SecurityRequirement().addList(JWT_SCHEME));
    }

    private String successStatus(String operationId) {
        return Set.of("registerParent", "createChild", "createPairingCode", "completeDevicePairing", "createPolicy", "rotateDeviceCredential")
            .contains(operationId)
            ? "201"
            : Set.of("logoutParent", "logoutAllParentSessions", "deactivateChild", "revokeDevice", "updateDeviceHealth", "confirmDeviceConsentDisclosure", "removePolicy")
                .contains(operationId)
            ? "204"
            : "200";
    }

    private String successDescription(String operationId) {
        return switch (operationId) {
            case "registerParent" -> "Parent account created";
            case "createChild" -> "Child created";
            case "createPairingCode" -> "Pairing code created";
            case "completeDevicePairing" -> "Device paired";
            case "createPolicy" -> "Policy created";
            case "rotateDeviceCredential" -> "Credential rotated";
            case "logoutParent", "logoutAllParentSessions" -> "Session revoked";
            case "deactivateChild" -> "Child deactivated";
            case "revokeDevice" -> "Device revoked";
            case "updateDeviceHealth" -> "Health accepted";
            case "confirmDeviceConsentDisclosure" -> "Disclosure confirmation accepted";
            case "removePolicy" -> "Policy removed";
            default -> "Successful response";
        };
    }

    private Set<String> errorStatuses(String operationId) {
        return switch (operationId) {
            case "registerParent" -> Set.of("400", "409");
            case "loginParent", "refreshParent" -> Set.of("401");
            case "logoutParent", "logoutAllParentSessions" -> Set.of("401");
            case "listChildren" -> Set.of("401");
            case "createChild" -> Set.of("400", "401");
            case "updateChild" -> Set.of("400", "404");
            case "deactivateChild" -> Set.of("404", "409");
            case "createPairingCode" -> Set.of("404", "429");
            case "completeDevicePairing" -> Set.of("400", "409", "429");
            case "listDevices" -> Set.of("404");
            case "renameDevice" -> Set.of("400", "404");
            case "revokeDevice", "rotateDeviceCredential" -> Set.of("404", "409");
            case "updateDeviceHealth" -> Set.of("400", "401", "409");
            case "getDeviceHealth" -> Set.of("404");
            case "getChildConsent" -> Set.of("404");
            case "recordChildConsent" -> Set.of("400", "404");
            case "confirmDeviceConsentDisclosure" -> Set.of("400", "401");
            case "ingestActivity", "ingestDomainObservations" -> Set.of("400", "401", "413");
            case "downloadPolicies" -> Set.of("401");
            case "acknowledgePolicy" -> Set.of("400", "401", "409");
            case "getUsageReport", "listAlerts" -> Set.of("400", "404");
            case "listPolicies" -> Set.of("404");
            case "createPolicy" -> Set.of("400", "404");
            case "updatePolicy" -> Set.of("400", "404", "409");
            case "removePolicy", "pausePolicy" -> Set.of("404", "409");
            case "getPolicyDeliveryStatus" -> Set.of("404");
            case "reviewAlert" -> Set.of("400", "404");
            case "dismissAlert" -> Set.of("404");
            default -> Set.of();
        };
    }

    private ApiResponse problemResponse(String status) {
        return new ApiResponse()
            .description("RFC 7807-compatible problem response")
            .content(new Content().addMediaType("application/problem+json", new io.swagger.v3.oas.models.media.MediaType().schema(new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/Problem"))));
    }

    private io.swagger.v3.oas.models.media.Schema<?> problemSchema() {
        return new ObjectSchema()
            .addProperty("type", new StringSchema().format("uri"))
            .addProperty("title", new StringSchema())
            .addProperty("status", new io.swagger.v3.oas.models.media.IntegerSchema())
            .addProperty("detail", new StringSchema())
            .addProperty("code", new StringSchema())
            .addProperty("path", new StringSchema());
    }
}
