package com.tamdan.controller;

import com.tamdan.dto.request.DeviceHealthUpdateRequest;
import com.tamdan.dto.request.RenameDeviceV1Request;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.dto.response.DeviceHealthResponse;
import com.tamdan.dto.response.DeviceV1Response;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.device.DeviceHealthService;
import com.tamdan.service.device.DeviceService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1)
public class V1DeviceResource {

    private final DeviceService deviceService;
    private final DeviceHealthService deviceHealthService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;
    private final AuthenticatedDeviceIdResolver authenticatedDeviceIdResolver;

    @GetMapping("/families/me/children/{childId}/devices")
    @Operation(operationId = "listDevices", summary = "List devices paired to a child")
    @SecurityRequirement(name = "parentJwt")
    public ApiResponse<List<DeviceV1Response>> list(@PathVariable Long childId) {
        return ApiResponse.success(deviceService.listV1(authenticatedUserIdResolver.requireCurrentUserId(), childId));
    }

    @PatchMapping("/families/me/children/{childId}/devices/{deviceId}")
    @Operation(operationId = "renameDevice", summary = "Rename a paired device")
    @SecurityRequirement(name = "parentJwt")
    public ApiResponse<DeviceV1Response> rename(
        @PathVariable Long childId,
        @PathVariable Long deviceId,
        @Valid @RequestBody RenameDeviceV1Request request
    ) {
        deviceService.ensureParentOwnsDevice(authenticatedUserIdResolver.requireCurrentUserId(), childId, deviceId);
        return ApiResponse.success(deviceService.renameV1(authenticatedUserIdResolver.requireCurrentUserId(), deviceId, request.displayName()));
    }

    @PostMapping("/families/me/children/{childId}/devices/{deviceId}/revocation")
    @Operation(operationId = "revokeDevice", summary = "Revoke a paired device")
    @SecurityRequirement(name = "parentJwt")
    public ResponseEntity<Void> revoke(@PathVariable Long childId, @PathVariable Long deviceId) {
        Long parentId = authenticatedUserIdResolver.requireCurrentUserId();
        deviceService.ensureParentOwnsDevice(parentId, childId, deviceId);
        deviceService.revokeDevice(parentId, deviceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/families/me/children/{childId}/devices/{deviceId}/credential-rotation")
    @Operation(operationId = "rotateDeviceCredential", summary = "Rotate a device credential")
    @SecurityRequirement(name = "parentJwt")
    public ResponseEntity<ApiResponse<DeviceCredentialResponse>> rotate(@PathVariable Long childId, @PathVariable Long deviceId) {
        Long parentId = authenticatedUserIdResolver.requireCurrentUserId();
        deviceService.ensureParentOwnsDevice(parentId, childId, deviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(deviceService.rotateCredential(parentId, deviceId)));
    }

    @PutMapping("/devices/me/health")
    @Operation(
        operationId = "updateDeviceHealth",
        summary = "Report device health",
        description = "The device reports its own state. UNSUPPORTED is a truthful capability state and must not be treated as active enforcement."
    )
    @SecurityRequirement(name = "deviceCredential")
    public ResponseEntity<Void> updateHealth(@Valid @RequestBody DeviceHealthUpdateRequest request) {
        deviceHealthService.update(authenticatedDeviceIdResolver.requireAuthenticatedDeviceId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/families/me/children/{childId}/devices/{deviceId}/health")
    @Operation(operationId = "getDeviceHealth", summary = "Read device health")
    @SecurityRequirement(name = "parentJwt")
    public ApiResponse<DeviceHealthResponse> getHealth(@PathVariable Long childId, @PathVariable Long deviceId) {
        return ApiResponse.success(deviceHealthService.get(authenticatedUserIdResolver.requireCurrentUserId(), childId, deviceId));
    }
}
