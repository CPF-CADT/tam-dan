package com.tamdan.controller;

import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.DeviceResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.dto.request.RenameDeviceRequest;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.device.DeviceService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API + "/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @GetMapping
    public ApiResponse<List<DeviceResponse>> list(@RequestParam Long childId) {
        return ApiResponse.success(deviceService.list(authenticatedUserIdResolver.requireCurrentUserId(), childId));
    }

    @GetMapping("/{deviceId}")
    public ApiResponse<DeviceResponse> get(@PathVariable Long deviceId) {
        return ApiResponse.success(deviceService.get(authenticatedUserIdResolver.requireCurrentUserId(), deviceId));
    }

    @PutMapping("/{deviceId}/name")
    public ApiResponse<DeviceResponse> rename(@PathVariable Long deviceId, @Valid @RequestBody RenameDeviceRequest request) {
        return ApiResponse.success(deviceService.rename(authenticatedUserIdResolver.requireCurrentUserId(), deviceId, request));
    }

    @DeleteMapping("/{deviceId}")
    public ApiResponse<Void> revoke(@PathVariable Long deviceId) {
        deviceService.revokeDevice(authenticatedUserIdResolver.requireCurrentUserId(), deviceId);
        return ApiResponse.success();
    }

    @org.springframework.web.bind.annotation.PostMapping("/{deviceId}/credential/rotate")
    public ApiResponse<DeviceCredentialResponse> rotateCredential(@PathVariable Long deviceId) {
        return ApiResponse.success(deviceService.rotateCredential(authenticatedUserIdResolver.requireCurrentUserId(), deviceId));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{deviceId}/credential")
    public ApiResponse<Void> revokeCredential(@PathVariable Long deviceId) {
        deviceService.revokeCredential(authenticatedUserIdResolver.requireCurrentUserId(), deviceId);
        return ApiResponse.success();
    }
}
