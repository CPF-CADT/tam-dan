package com.tamdan.controller;

import com.tamdan.dto.request.LoginRequest;
import com.tamdan.dto.request.RefreshRequest;
import com.tamdan.dto.request.RegisterParentRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.ParentAccountResponse;
import com.tamdan.dto.response.TokenResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.auth.AuthenticationService;
import com.tamdan.service.auth.UserService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1_AUTH)
public class V1AuthResource {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping("/register")
    @Operation(operationId = "registerParent", summary = "Register a parent account")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<ParentAccountResponse>> register(@Valid @RequestBody RegisterParentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.register(request)));
    }

    @PostMapping("/login")
    @Operation(operationId = "loginParent", summary = "Authenticate a parent")
    @SecurityRequirements
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(operationId = "refreshParent", summary = "Refresh a parent session")
    @SecurityRequirements
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authenticationService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(operationId = "logoutParent", summary = "Revoke a parent session")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(operationId = "logoutAllParentSessions", summary = "Revoke all parent sessions")
    public ResponseEntity<Void> logoutAll() {
        authenticationService.logoutAll(authenticatedUserIdResolver.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
