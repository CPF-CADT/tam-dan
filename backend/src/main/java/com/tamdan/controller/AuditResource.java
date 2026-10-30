package com.tamdan.controller;

import com.tamdan.dto.request.AuditSearchRequest;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.AuditResponse;
import com.tamdan.dto.response.PageResponse;
import com.tamdan.service.consent.AuditService;
import com.tamdan.web.ApiPaths;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API + "/audit")
public class AuditResource {

    private final AuditService auditService;

    @GetMapping
    public ApiResponse<PageResponse<AuditResponse>> search(
        @RequestParam Long familyId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String resourceType,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        Page<AuditResponse> page = auditService.searchAuthorized(new AuditSearchRequest(familyId, action, resourceType, from, to, pageable));
        return ApiResponse.success(PageResponse.from(page));
    }
}
