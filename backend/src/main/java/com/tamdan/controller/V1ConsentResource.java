package com.tamdan.controller;

import com.tamdan.dto.request.ConsentDecisionRequest;
import com.tamdan.dto.request.ConsentDisclosureConfirmation;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.ConsentV1Response;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.consent.ConsentService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1)
public class V1ConsentResource {

    private final ConsentService consentService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;
    private final AuthenticatedDeviceIdResolver authenticatedDeviceIdResolver;

    @GetMapping("/families/me/children/{childId}/consent")
    @Operation(operationId = "getChildConsent", summary = "Read child consent")
    @SecurityRequirement(name = "parentJwt")
    public ApiResponse<ConsentV1Response> get(@PathVariable Long childId) {
        return ApiResponse.success(consentService.currentDecision(authenticatedUserIdResolver.requireCurrentUserId(), childId));
    }

    @PutMapping("/families/me/children/{childId}/consent")
    @Operation(operationId = "recordChildConsent", summary = "Record child consent")
    @SecurityRequirement(name = "parentJwt")
    public ApiResponse<ConsentV1Response> put(@PathVariable Long childId, @Valid @RequestBody ConsentDecisionRequest request) {
        return ApiResponse.success(consentService.recordDecision(authenticatedUserIdResolver.requireCurrentUserId(), childId, request));
    }

    @PostMapping("/devices/me/consent")
    @Operation(operationId = "confirmDeviceConsentDisclosure", summary = "Confirm consent disclosure on a device")
    @SecurityRequirement(name = "deviceCredential")
    public ResponseEntity<Void> confirm(@Valid @RequestBody ConsentDisclosureConfirmation request) {
        consentService.confirmDisclosure(authenticatedDeviceIdResolver.requireAuthenticatedDeviceId(), request.version(), request.confirmedAt());
        return ResponseEntity.noContent().build();
    }
}
