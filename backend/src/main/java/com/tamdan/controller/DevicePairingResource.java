package com.tamdan.controller;

import com.tamdan.dto.request.CompletePairingRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.service.device.PairingService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API + "/pairing")
public class DevicePairingResource {

    private final PairingService pairingService;

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<DeviceCredentialResponse>> complete(@Valid @RequestBody CompletePairingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pairingService.completePairing(request)));
    }
}
