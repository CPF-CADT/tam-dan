package com.tamdan.security;

import com.tamdan.exception.ForbiddenException;
import com.tamdan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserIdResolver {

    private final UserRepository userRepository;

    public long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId().orElseGet(() -> {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new ForbiddenException("Authentication is required"));
            return userRepository
                .findOneByLogin(login)
                .map(user -> user.getId())
                .orElseThrow(() -> new ForbiddenException("Authenticated user no longer exists"));
        });
    }
}
