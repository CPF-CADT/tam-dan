package com.tamdan.security;

import com.tamdan.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedDeviceIdResolver {

    public Long requireAuthenticatedDeviceId() {
        return requireContext().deviceId();
    }

    public AuthenticatedDeviceContext requireContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedDeviceContext context) return context;
        throw new ForbiddenException("Device authentication is required");
    }
}
