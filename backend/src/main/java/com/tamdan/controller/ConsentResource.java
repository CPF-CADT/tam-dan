package com.tamdan.controller;

import com.tamdan.dto.request.ConsentRequest;
import com.tamdan.dto.request.DevicePermissionRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.ConsentResponse;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.ConsentService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API + "/consent")
public class ConsentResource {

    private final ConsentService consentService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;
    private final AuthenticatedDeviceIdResolver authenticatedDeviceIdResolver;

    @PostMapping("/children/{childId}")
    public ApiResponse<ConsentResponse> acknowledge(@PathVariable Long childId, @Valid @RequestBody ConsentRequest request) {
        return ApiResponse.success(consentService.acknowledge(authenticatedUserIdResolver.requireCurrentUserId(), childId, request));
    }

    @GetMapping("/children/{childId}")
    public ApiResponse<List<ConsentResponse>> history(@PathVariable Long childId) {
        return ApiResponse.success(consentService.history(authenticatedUserIdResolver.requireCurrentUserId(), childId));
    }

    @PostMapping("/device")
    public ApiResponse<ConsentResponse> devicePermission(@Valid @RequestBody DevicePermissionRequest request) {
        return ApiResponse.success(consentService.recordDevicePermission(authenticatedDeviceIdResolver.requireAuthenticatedDeviceId(), request));
    }
}
