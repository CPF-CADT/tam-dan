package com.tamdan.controller;

import com.tamdan.dto.request.CompletePairingRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.dto.response.PairingCodeResponse;
import com.tamdan.dto.response.PairingCompletionResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.PairingService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1)
public class V1PairingResource {

    private final PairingService pairingService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping("/families/me/children/{childId}/pairing-codes")
    public ResponseEntity<ApiResponse<PairingCodeResponse>> createCode(@PathVariable Long childId) {
        PairingCodeResponse response = pairingService.createPairingCode(authenticatedUserIdResolver.requireCurrentUserId(), childId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/pairing/complete")
    public ResponseEntity<ApiResponse<PairingCompletionResponse>> complete(@Valid @RequestBody CompletePairingRequest request) {
        DeviceCredentialResponse credential = pairingService.completePairing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(pairingService.toV1Completion(credential)));
    }
}
