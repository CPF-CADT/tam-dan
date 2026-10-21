package com.tamdan.controller;

import com.tamdan.config.Constants;
import com.tamdan.dto.request.AdminUserRequest;
import com.tamdan.dto.response.AdminUserDTO;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.dto.response.PageResponse;
import com.tamdan.exception.BadRequestException;
import com.tamdan.mail.MailService;
import com.tamdan.mapper.UserMapper;
import com.tamdan.security.AuthoritiesConstants;
import com.tamdan.service.UserService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(ApiPaths.ADMIN)
@PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
public class UserResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = List.of(
        "id", "login", "firstName", "lastName", "email", "activated", "langKey", "createdBy", "createdDate",
        "lastModifiedBy", "lastModifiedDate"
    );

    private final UserService userService;
    private final UserMapper userMapper;
    private final MailService mailService;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserDTO>> createUser(@Valid @RequestBody AdminUserRequest request) {
        if (request.id() != null) throw new BadRequestException("A new user cannot already have an ID");
        var user = userService.createUser(request);
        mailService.sendCreationEmail(user);
        AdminUserDTO response = userMapper.toAdminResponse(user);
        return ResponseEntity.created(URI.create(ApiPaths.ADMIN + "/users/" + response.login())).body(ApiResponse.success(response));
    }

    @PutMapping({ "/users", "/users/{login}" })
    public ApiResponse<AdminUserDTO> updateUser(
        @PathVariable(name = "login", required = false) @Pattern(regexp = Constants.LOGIN_REGEX) String login,
        @Valid @RequestBody AdminUserRequest request
    ) {
        if (login != null && !login.equalsIgnoreCase(request.login())) throw new BadRequestException("Path login must match request login");
        return ApiResponse.success(userService.updateUser(request));
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserDTO>> getAllUsers(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        ensureAllowedSort(pageable);
        Page<AdminUserDTO> page = userService.getAllManagedUsers(pageable);
        return ApiResponse.success(PageResponse.from(page));
    }

    @GetMapping("/users/{login}")
    public ApiResponse<AdminUserDTO> getUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        return ApiResponse.success(userService.getAdminUser(login));
    }

    @DeleteMapping("/users/{login}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        userService.deleteUser(login);
        return ResponseEntity.noContent().build();
    }

    private void ensureAllowedSort(Pageable pageable) {
        boolean valid = pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
        if (!valid) throw new BadRequestException("Unsupported sort property");
    }
}
