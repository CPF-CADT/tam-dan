package com.tamdan.controller;

import com.tamdan.dto.SafetyDto.AlertPageResponse;
import com.tamdan.dto.SafetyDto.AlertResponse;
import com.tamdan.dto.SafetyDto.AlertReviewRequest;
import com.tamdan.dto.SafetyDto.PolicyDeliveryResponse;
import com.tamdan.dto.SafetyDto.PolicyPageResponse;
import com.tamdan.dto.SafetyDto.PolicyRequest;
import com.tamdan.dto.SafetyDto.PolicyResponse;
import com.tamdan.dto.SafetyDto.UsageReportResponse;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.security.AuthoritiesConstants;
import com.tamdan.service.AlertService;
import com.tamdan.service.PolicyService;
import com.tamdan.service.UsageReportService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/families/me/children")
@PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.USER + "','" + AuthoritiesConstants.ADMIN + "')")
public class SafetyPolicyResource {
    private final AuthenticatedUserIdResolver parentResolver;
    private final UsageReportService usageReportService;
    private final AlertService alertService;
    private final PolicyService policyService;

    @GetMapping("/{childId}/reports/usage")
    public ApiResponse<UsageReportResponse> usageReport(@PathVariable Long childId, @RequestParam Instant from, @RequestParam Instant to) {
        return ApiResponse.success(usageReportService.getReport(parentResolver.requireCurrentUserId(), childId, from, to));
    }

    @GetMapping("/{childId}/alerts")
    public ApiResponse<AlertPageResponse> alerts(@PathVariable Long childId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > 100) throw new com.tamdan.exception.BadRequestException("Invalid page bounds");
        return ApiResponse.success(alertService.list(parentResolver.requireCurrentUserId(), childId, page, pageSize));
    }

    @GetMapping("/{childId}/policies")
    public ApiResponse<PolicyPageResponse> policies(@PathVariable Long childId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > 100) throw new com.tamdan.exception.BadRequestException("Invalid page bounds");
        Page<PolicyResponse> result = policyService.list(parentResolver.requireCurrentUserId(), childId, page, pageSize);
        return ApiResponse.success(new PolicyPageResponse(result.getContent(), page, pageSize, result.getTotalElements()));
    }

    @PostMapping("/{childId}/policies")
    public ResponseEntity<ApiResponse<PolicyResponse>> createPolicy(@PathVariable Long childId, @Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(policyService.create(parentResolver.requireCurrentUserId(), childId, request)));
    }

    @PatchMapping("/{childId}/policies/{policyId}")
    public ApiResponse<PolicyResponse> updatePolicy(@PathVariable Long childId, @PathVariable Long policyId, @Valid @RequestBody PolicyRequest request) {
        return ApiResponse.success(policyService.update(parentResolver.requireCurrentUserId(), childId, policyId, request));
    }

    @DeleteMapping("/{childId}/policies/{policyId}")
    public ResponseEntity<Void> removePolicy(@PathVariable Long childId, @PathVariable Long policyId) {
        policyService.remove(parentResolver.requireCurrentUserId(), childId, policyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{childId}/policies/{policyId}/pause")
    public ApiResponse<PolicyResponse> pausePolicy(@PathVariable Long childId, @PathVariable Long policyId) {
        return ApiResponse.success(policyService.pause(parentResolver.requireCurrentUserId(), childId, policyId));
    }

    @GetMapping("/{childId}/policies/{policyId}/delivery")
    public ApiResponse<PolicyDeliveryResponse> delivery(@PathVariable Long childId, @PathVariable Long policyId) {
        return ApiResponse.success(policyService.delivery(parentResolver.requireCurrentUserId(), childId, policyId));
    }
}
