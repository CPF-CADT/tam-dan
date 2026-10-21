package com.tamdan.controller;

import com.tamdan.dto.request.CreateChildRequest;
import com.tamdan.dto.request.UpdateChildRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.ChildProfileResponse;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.ChildProfileService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API + "/children")
public class ChildProfileResource {

    private final ChildProfileService childProfileService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<ChildProfileResponse>> create(@Valid @RequestBody CreateChildRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(childProfileService.create(currentParentId(), request)));
    }

    @GetMapping
    public ApiResponse<List<ChildProfileResponse>> list() {
        return ApiResponse.success(childProfileService.list(currentParentId()));
    }

    @GetMapping("/{childId}")
    public ApiResponse<ChildProfileResponse> get(@PathVariable Long childId) {
        return ApiResponse.success(childProfileService.get(currentParentId(), childId));
    }

    @PutMapping("/{childId}")
    public ApiResponse<ChildProfileResponse> update(@PathVariable Long childId, @Valid @RequestBody UpdateChildRequest request) {
        return ApiResponse.success(childProfileService.update(currentParentId(), childId, request));
    }

    @DeleteMapping("/{childId}")
    public ApiResponse<Void> deactivate(@PathVariable Long childId) {
        childProfileService.deactivate(currentParentId(), childId);
        return ApiResponse.success();
    }

    private long currentParentId() {
        return authenticatedUserIdResolver.requireCurrentUserId();
    }
}
