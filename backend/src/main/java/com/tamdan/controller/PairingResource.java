package com.tamdan.controller;

import com.tamdan.dto.request.CreatePairingCodeRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.PairingCodeResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.PairingService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API + "/pairing-codes")
public class PairingResource {

    private final PairingService pairingService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<PairingCodeResponse>> create(@Valid @RequestBody CreatePairingCodeRequest request) {
        PairingCodeResponse response = pairingService.createPairingCode(authenticatedUserIdResolver.requireCurrentUserId(), request.childId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
