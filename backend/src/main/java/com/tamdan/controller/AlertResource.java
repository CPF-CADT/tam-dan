package com.tamdan.controller;

import com.tamdan.dto.SafetyDto.AlertResponse;
import com.tamdan.dto.SafetyDto.AlertReviewRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.security.AuthoritiesConstants;
import com.tamdan.service.safety.AlertService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API_V1 + "/alerts")
@PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.USER + "','" + AuthoritiesConstants.ADMIN + "')")
public class AlertResource {
    private final AlertService alertService;
    private final AuthenticatedUserIdResolver parentResolver;

    @PostMapping("/{alertId}/review")
    @Operation(operationId = "reviewAlert", summary = "Review an alert")
    @SecurityRequirement(name = "parentJwt")
    public ApiResponse<AlertResponse> review(@PathVariable Long alertId, @Valid @RequestBody AlertReviewRequest request) {
        return ApiResponse.success(alertService.review(parentResolver.requireCurrentUserId(), alertId, request));
    }

    @PostMapping("/{alertId}/dismissal")
    @Operation(operationId = "dismissAlert", summary = "Dismiss an alert")
    @SecurityRequirement(name = "parentJwt")
    public ApiResponse<AlertResponse> dismiss(@PathVariable Long alertId) {
        return ApiResponse.success(alertService.dismiss(parentResolver.requireCurrentUserId(), alertId));
    }
}
