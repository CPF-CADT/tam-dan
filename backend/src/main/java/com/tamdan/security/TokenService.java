package com.tamdan.security;

import static com.tamdan.security.SecurityUtils.AUTHORITIES_CLAIM;
import static com.tamdan.security.SecurityUtils.JWT_ALGORITHM;
import static com.tamdan.security.SecurityUtils.USER_ID_CLAIM;

import com.tamdan.security.DomainUserDetailsService.UserWithId;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import tech.jhipster.config.JHipsterProperties;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JHipsterProperties properties;

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        Instant now = Instant.now();
        var jwt = properties.getSecurity().getAuthentication().getJwt();
        long validitySeconds = rememberMe ? jwt.getTokenValidityInSecondsForRememberMe() : jwt.getTokenValidityInSeconds();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plus(validitySeconds, ChronoUnit.SECONDS))
            .subject(authentication.getName())
            .claim(AUTHORITIES_CLAIM, authorities);
        if (authentication.getPrincipal() instanceof UserWithId user) claims.claim(USER_ID_CLAIM, user.getId());

        JwsHeader header = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
