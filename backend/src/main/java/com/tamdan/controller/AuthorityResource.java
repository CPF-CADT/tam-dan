package com.tamdan.controller;

import com.tamdan.dto.request.AuthorityRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.AuthorityResponse;
import com.tamdan.security.AuthoritiesConstants;
import com.tamdan.service.AuthorityService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.AUTHORITIES)
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public class AuthorityResource {

    private final AuthorityService authorityService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuthorityResponse>> createAuthority(@Valid @RequestBody AuthorityRequest request) {
        AuthorityResponse response = authorityService.create(request);
        return ResponseEntity.created(URI.create(ApiPaths.AUTHORITIES + "/" + response.name())).body(ApiResponse.success(response));
    }

    @GetMapping
    public ApiResponse<List<AuthorityResponse>> getAllAuthorities() {
        return ApiResponse.success(authorityService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AuthorityResponse> getAuthority(@PathVariable String id) {
        return ApiResponse.success(authorityService.findOne(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthority(@PathVariable String id) {
        authorityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
