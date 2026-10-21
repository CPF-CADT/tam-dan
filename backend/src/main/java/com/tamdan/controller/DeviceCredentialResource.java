package com.tamdan.controller;

import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import com.tamdan.service.DeviceCredentialService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device/credential")
public class DeviceCredentialResource {

    private final DeviceCredentialService deviceCredentialService;
    private final AuthenticatedDeviceIdResolver deviceIdResolver;

    public DeviceCredentialResource(DeviceCredentialService deviceCredentialService, AuthenticatedDeviceIdResolver deviceIdResolver) {
        this.deviceCredentialService = deviceCredentialService;
        this.deviceIdResolver = deviceIdResolver;
    }

    @PostMapping("/rotate")
    public ApiResponse<DeviceCredentialResponse> rotate() {
        return ApiResponse.success(deviceCredentialService.rotateCredential(deviceIdResolver.requireAuthenticatedDeviceId()));
    }

    @DeleteMapping
    public ApiResponse<Void> revoke() {
        deviceCredentialService.revokeCredential(deviceIdResolver.requireAuthenticatedDeviceId());
        return ApiResponse.success();
    }
}
