package com.tamdan.controller;

import com.tamdan.dto.request.AccountUpdateRequest;
import com.tamdan.dto.request.KeyAndPasswordVM;
import com.tamdan.dto.request.ManagedUserVM;
import com.tamdan.dto.request.PasswordChangeDTO;
import com.tamdan.dto.response.AdminUserDTO;
import com.tamdan.dto.response.ApiResponse;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.mail.MailService;
import com.tamdan.security.AuthenticatedUserIdResolver;
import com.tamdan.service.UserService;
import com.tamdan.web.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class AccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    private final UserService userService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserIdResolver authenticatedUserIdResolver;

    @PostMapping(ApiPaths.API + "/register")
    public ResponseEntity<ApiResponse<Void>> registerAccount(@Valid @RequestBody ManagedUserVM request) {
        var user = userService.registerUser(request);
        mailService.sendActivationEmail(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @GetMapping(ApiPaths.API + "/activate")
    public ApiResponse<Void> activateAccount(@RequestParam String key) {
        userService.activateRegistration(key).orElseThrow(() -> new ResourceNotFoundException("Activation key was not found"));
        return ApiResponse.success();
    }

    @GetMapping(ApiPaths.ACCOUNT)
    public ApiResponse<AdminUserDTO> getAccount() {
        return ApiResponse.success(userService.getCurrentUser(authenticatedUserIdResolver.requireCurrentUserId()));
    }

    @PostMapping(ApiPaths.ACCOUNT)
    public ApiResponse<Void> saveAccount(@Valid @RequestBody AccountUpdateRequest request) {
        userService.updateCurrentUser(authenticatedUserIdResolver.requireCurrentUserId(), request);
        return ApiResponse.success();
    }

    @PostMapping(ApiPaths.ACCOUNT + "/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeDTO request) {
        userService.changePassword(
            authenticatedUserIdResolver.requireCurrentUserId(), request.currentPassword(), request.newPassword()
        );
        return ApiResponse.success();
    }

    @PostMapping(ApiPaths.ACCOUNT + "/reset-password/init")
    public ApiResponse<Void> requestPasswordReset(@RequestBody @Email @Size(min = 5, max = 254) String email) {
        userService.requestPasswordReset(email).ifPresentOrElse(mailService::sendPasswordResetMail, () ->
            LOG.warn("Password reset requested for a non-existing email")
        );
        return ApiResponse.success();
    }

    @PostMapping(ApiPaths.ACCOUNT + "/reset-password/finish")
    public ApiResponse<Void> finishPasswordReset(@Valid @RequestBody KeyAndPasswordVM request) {
        if (userService.completePasswordReset(request.newPassword(), request.key()).isEmpty()) {
            passwordEncoder.encode(request.newPassword());
            throw new BadRequestException("Reset key is invalid or expired");
        }
        return ApiResponse.success();
    }
}
