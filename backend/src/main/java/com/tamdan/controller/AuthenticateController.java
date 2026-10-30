package com.tamdan.controller;

import com.tamdan.dto.request.LoginRequest;
import com.tamdan.dto.request.RefreshRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.TokenResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.auth.AuthenticationService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticateController {

    private final AuthenticationService authenticationService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping(ApiPaths.AUTHENTICATE)
    public ResponseEntity<ApiResponse<TokenResponse>> authorize(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authenticationService.login(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(response.accessToken());
        return ResponseEntity.ok().headers(headers).body(ApiResponse.success(response));
    }

    @PostMapping(ApiPaths.AUTHENTICATE_REFRESH)
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authenticationService.refresh(request));
    }

    @PostMapping(ApiPaths.AUTHENTICATE_LOGOUT)
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authenticationService.logout(request.refreshToken());
        return ApiResponse.success();
    }

    @PostMapping(ApiPaths.AUTHENTICATE_LOGOUT_ALL)
    public ApiResponse<Void> logoutAll() {
        authenticationService.logoutAll(authenticatedUserIdResolver.requireCurrentUserId());
        return ApiResponse.success();
    }

    @GetMapping(ApiPaths.AUTHENTICATE)
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }
}
