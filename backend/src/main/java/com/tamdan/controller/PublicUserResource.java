package com.tamdan.controller;

import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.PageResponse;
import com.tamdan.dto.response.UserDTO;
import com.tamdan.exception.BadRequestException;
import com.tamdan.service.UserService;
import com.tamdan.web.ApiPaths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicUserResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = List.of(
        "id", "login", "firstName", "lastName", "email", "activated", "langKey"
    );

    private final UserService userService;

    @GetMapping(ApiPaths.USERS)
    public ApiResponse<PageResponse<UserDTO>> getAllPublicUsers(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        ensureAllowedSort(pageable);
        Page<UserDTO> page = userService.getAllPublicUsers(pageable);
        return ApiResponse.success(PageResponse.from(page));
    }

    private void ensureAllowedSort(Pageable pageable) {
        boolean valid = pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
        if (!valid) throw new BadRequestException("Unsupported sort property");
    }
}
