package com.tamdan.controller;

import com.tamdan.dto.request.CreateChildRequest;
import com.tamdan.dto.request.UpdateChildV1Request;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.ChildProfileResponse;
import com.tamdan.dto.response.ChildV1Response;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.family.ChildProfileService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.V1_FAMILIES + "/children")
public class V1FamilyResource {

    private final ChildProfileService childProfileService;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @GetMapping
    public ApiResponse<List<ChildV1Response>> listChildren() {
        return ApiResponse.success(childProfileService.list(authenticatedUserIdResolver.requireCurrentUserId()).stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChildV1Response>> createChild(@Valid @RequestBody CreateChildRequest request) {
        ChildProfileResponse child = childProfileService.create(authenticatedUserIdResolver.requireCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(child)));
    }

    @PatchMapping("/{childId}")
    public ApiResponse<ChildV1Response> updateChild(@PathVariable Long childId, @Valid @RequestBody UpdateChildV1Request request) {
        ChildProfileResponse child = childProfileService.updateDisplayName(authenticatedUserIdResolver.requireCurrentUserId(), childId, request.displayName());
        return ApiResponse.success(toResponse(child));
    }

    @PostMapping("/{childId}/deactivation")
    public ResponseEntity<Void> deactivateChild(@PathVariable Long childId) {
        childProfileService.deactivate(authenticatedUserIdResolver.requireCurrentUserId(), childId);
        return ResponseEntity.noContent().build();
    }

    private ChildV1Response toResponse(ChildProfileResponse child) {
        return new ChildV1Response(child.id(), child.displayName(), Boolean.TRUE.equals(child.active()) ? "ACTIVE" : "DEACTIVATED");
    }
}
