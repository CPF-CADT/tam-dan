package com.tamdan.service.auth;

import com.tamdan.service.RateLimitPolicy;
import com.tamdan.service.RateLimitService;
import com.tamdan.service.consent.AuditService;
import com.tamdan.dto.request.LoginRequest;
import com.tamdan.dto.request.RefreshRequest;
import com.tamdan.dto.response.TokenResponse;
import com.tamdan.entities.ParentSession;
import com.tamdan.entities.User;
import com.tamdan.exception.BadRequestException;
import com.tamdan.repository.ParentSessionRepository;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.DomainUserDetailsService.UserWithId;
import com.tamdan.security.TokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final ParentSessionRepository parentSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;
    private final RateLimitPolicy securityPolicy;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String login = normalizeLogin(request.login());
        if (rateLimitService != null) rateLimitService.check(RateLimitService.Operation.LOGIN, login);
        User user = userRepository.findOneWithAuthoritiesByLogin(login).orElse(null);
        Instant now = Instant.now();
        if (user == null || !user.isActivated() || user.isLocked(now) || !passwordEncoder.matches(request.password(), user.getPassword())) {
            if (user != null) {
                user.recordFailedLogin(now, securityPolicy.failedLoginMaxAttempts(), now.plus(securityPolicy.loginLockDuration()));
                userRepository.save(user);
            }
            if (auditService != null) auditService.recordFailure(new com.tamdan.dto.request.AuditCommand("PARENT", null, null, "login", "PARENT", null, "FAILURE", null, null, null, java.util.Map.of()));
            throw invalidCredentials();
        }
        if (user.getFailedLoginAttempts() > 0 || user.isLocked(now)) {
            user.resetFailedLogins();
            userRepository.save(user);
        }
        TokenResponse response = issueTokens(user);
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", user.getId(), null, "login", "PARENT", user.getId(), "SUCCESS", null, null, null, java.util.Map.of()));
        return response;
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        if (rateLimitService != null) rateLimitService.check(RateLimitService.Operation.REFRESH, "global");
        Instant now = Instant.now();
        String refreshHash = hash(request.refreshToken());
        ParentSession previous = parentSessionRepository.findByRefreshTokenHashAndRevokedAtIsNull(refreshHash).orElse(null);
        if (previous == null) {
            parentSessionRepository.findByRefreshTokenHash(refreshHash).ifPresent(session -> {
                if (session.getRevokedAt() != null || session.getReplacedBySessionId() != null) {
                    parentSessionRepository.revokeAllActiveSessions(session.getParent().getId(), Instant.now());
                    if (auditService != null) auditService.recordFailure(new com.tamdan.dto.request.AuditCommand("PARENT", session.getParent().getId(), null, "refresh-reuse", "SESSION", session.getId(), "FAILURE", null, null, null, java.util.Map.of()));
                    throw new BadRequestException("Refresh token is revoked");
                }
            });
            throw new BadRequestException("Refresh token is invalid or expired");
        }
        if (!previous.getExpiresAt().isAfter(now)) throw new BadRequestException("Refresh token is invalid or expired");
        User parent = previous.getParent();
        if (!parent.isActivated()) throw new BadRequestException("Refresh token is invalid or expired");

        TokenResponse response = issueTokens(parent);
        ParentSession replacement = parentSessionRepository
            .findByRefreshTokenHashAndRevokedAtIsNull(hash(response.refreshToken()))
            .orElseThrow(() -> new BadRequestException("Refresh token could not be created"));
        previous.replaceWith(replacement.getId(), now);
        return response;
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        parentSessionRepository.findByRefreshTokenHashAndRevokedAtIsNull(hash(refreshToken)).ifPresent(session -> session.revoke(Instant.now()));
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", null, null, "logout", "SESSION", null, "SUCCESS", null, null, null, java.util.Map.of()));
    }

    @Transactional
    public void logoutAll(Long authenticatedParentId) {
        parentSessionRepository.revokeAllActiveSessions(authenticatedParentId, Instant.now());
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", authenticatedParentId, null, "logout-all", "SESSION", null, "SUCCESS", null, null, null, java.util.Map.of()));
    }

    private TokenResponse issueTokens(User user) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(tokenService.accessTokenValiditySeconds(), ChronoUnit.SECONDS);
        Instant refreshExpiresAt = now.plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS);
        UserWithId principal = UserWithId.fromUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        String accessToken = tokenService.createToken(authentication, false);
        String refreshToken = generateRefreshToken();
        parentSessionRepository.saveAndFlush(ParentSession.create(user, hash(refreshToken), now, refreshExpiresAt, now));
        return new TokenResponse(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private BadRequestException invalidCredentials() {
        return new BadRequestException("Invalid login or password");
    }

    private String normalizeLogin(String login) {
        return login == null ? null : login.trim().toLowerCase(Locale.ENGLISH);
    }
}
