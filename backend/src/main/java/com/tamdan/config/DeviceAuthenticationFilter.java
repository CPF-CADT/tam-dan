package com.tamdan.config;

import com.tamdan.security.AuthenticatedDeviceContext;
import com.tamdan.security.AuthoritiesConstants;
import com.tamdan.service.device.DeviceCredentialService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class DeviceAuthenticationFilter extends OncePerRequestFilter {

    public static final String DEVICE_CREDENTIAL_HEADER = "X-Device-Credential";
    private final DeviceCredentialService deviceCredentialService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String rawCredential = request.getHeader(DEVICE_CREDENTIAL_HEADER);
        if (rawCredential == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (rawCredential.isBlank() || request.getHeader("Authorization") != null || isParentOnlyRoute(request)) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Device authentication failed");
            return;
        }
        try {
            AuthenticatedDeviceContext context = deviceCredentialService.authenticate(rawCredential);
            var authentication = new UsernamePasswordAuthenticationToken(context, null, java.util.List.of(new SimpleGrantedAuthority(AuthoritiesConstants.DEVICE)));
            SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Device authentication failed");
        }
    }

    private boolean isParentOnlyRoute(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/authenticate") || path.equals("/api/authenticate/refresh") || path.equals("/api/authenticate/logout") || path.equals("/api/authenticate/logout-all") ||
            path.equals("/api/register") || path.equals("/api/activate") || path.equals("/api/activate/resend") ||
            path.startsWith("/api/account/") || path.startsWith("/api/v1/auth/") || path.equals("/api/v1/pairing/complete") ||
            path.startsWith("/api/v1/families/me/");
    }
}
