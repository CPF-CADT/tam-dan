package com.tamdan.controller;

import com.tamdan.dto.request.LoginVM;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.TokenResponse;
import com.tamdan.security.TokenService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticateController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenService tokenService;

    @PostMapping(ApiPaths.AUTHENTICATE)
    public ResponseEntity<ApiResponse<TokenResponse>> authorize(@Valid @RequestBody LoginVM request) {
        Authentication authentication = authenticationManagerBuilder
            .getObject()
            .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenService.createToken(authentication, request.rememberMe());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return ResponseEntity.ok().headers(headers).body(ApiResponse.success(new TokenResponse(token)));
    }

    @GetMapping(ApiPaths.AUTHENTICATE)
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }
}
