package com.tamdan.controller;

import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.FamilyResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.FamilyService;
import com.tamdan.web.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API + "/family")
public class FamilyResource {

    private final FamilyService familyService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(familyService.createForParent(currentParentId())));
    }

    @GetMapping("/current")
    public ApiResponse<FamilyResponse> current() {
        return ApiResponse.success(familyService.getCurrent(currentParentId()));
    }

    private long currentParentId() {
        return authenticatedUserIdResolver.requireCurrentUserId();
    }
}
