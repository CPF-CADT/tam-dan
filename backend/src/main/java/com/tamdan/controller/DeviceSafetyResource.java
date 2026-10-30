package com.tamdan.controller;

import com.tamdan.dto.SafetyDto.ActivityBatchRequest;
import com.tamdan.dto.SafetyDto.IngestionResultResponse;
import com.tamdan.dto.SafetyDto.PolicyAcknowledgementRequest;
import com.tamdan.dto.SafetyDto.PolicyResponse;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.entities.SafetyEvent;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import com.tamdan.security.AuthoritiesConstants;
import com.tamdan.service.safety.ActivityIngestionService;
import com.tamdan.service.safety.PolicyAcknowledgementService;
import com.tamdan.service.safety.PolicyService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API_V1 + "/devices/me")
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.DEVICE + "')")
public class DeviceSafetyResource {
    private final ActivityIngestionService activityIngestionService;
    private final PolicyService policyService;
    private final PolicyAcknowledgementService policyAcknowledgementService;
    private final AuthenticatedDeviceIdResolver deviceResolver;

    @PostMapping("/activity-events")
    @Operation(operationId = "ingestActivity", summary = "Ingest activity events")
    @SecurityRequirement(name = "deviceCredential")
    public ApiResponse<IngestionResultResponse> ingestActivity(@Valid @RequestBody ActivityBatchRequest request) {
        return ApiResponse.success(activityIngestionService.ingest(deviceResolver.requireContext(), request, SafetyEvent.Kind.ACTIVITY));
    }

    @PostMapping("/domain-observations")
    @Operation(
        operationId = "ingestDomainObservations",
        summary = "Ingest domain observations",
        description = "Domain enforcement capability may be unsupported; clients must use the reported capability state rather than infer enforcement."
    )
    @SecurityRequirement(name = "deviceCredential")
    public ApiResponse<IngestionResultResponse> ingestDomain(@Valid @RequestBody ActivityBatchRequest request) {
        return ApiResponse.success(activityIngestionService.ingest(deviceResolver.requireContext(), request, SafetyEvent.Kind.DOMAIN));
    }

    @GetMapping("/policies")
    @Operation(operationId = "downloadPolicies", summary = "Download policies for the paired device")
    @SecurityRequirement(name = "deviceCredential")
    public ApiResponse<List<PolicyResponse>> downloadPolicies() {
        return ApiResponse.success(policyService.download(deviceResolver.requireContext()));
    }

    @PostMapping("/policies/{policyId}/acknowledgements")
    @Operation(operationId = "acknowledgePolicy", summary = "Acknowledge policy delivery")
    @SecurityRequirement(name = "deviceCredential")
    public ApiResponse<com.tamdan.dto.SafetyDto.DevicePolicyDeliveryResponse> acknowledge(
        @PathVariable Long policyId,
        @Valid @RequestBody PolicyAcknowledgementRequest request
    ) {
        return ApiResponse.success(policyAcknowledgementService.acknowledge(deviceResolver.requireContext(), policyId, request));
    }
}
