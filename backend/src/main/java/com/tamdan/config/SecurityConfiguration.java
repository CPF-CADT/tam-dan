package com.tamdan.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.tamdan.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DeviceAuthenticationFilter deviceAuthenticationFilter) {
        http.cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz ->
                authz
                    .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/authenticate").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.POST, "/api/authenticate/refresh").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/authenticate/logout").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/pairing/complete").permitAll()
                    .requestMatchers("/api/register").permitAll()
                    .requestMatchers("/api/activate").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/activate/resend").permitAll()
                    .requestMatchers("/api/account/reset-password/init").permitAll()
                    .requestMatchers("/api/account/reset-password/finish").permitAll()
                    .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/device/credential/**").hasAuthority(AuthoritiesConstants.DEVICE)
                    .requestMatchers("/api/account/**", "/api/family/**", "/api/children/**", "/api/pairing-codes/**", "/api/audit/**", "/api/devices/**")
                    .hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.POST, "/api/authenticate/logout-all").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/consent/device", "/api/events/**", "/api/policies/**").hasAuthority(AuthoritiesConstants.DEVICE)
                    .requestMatchers("/api/**").hasAnyAuthority(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN, AuthoritiesConstants.DEVICE)
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                    .requestMatchers("/management/health").permitAll()
                    .requestMatchers("/management/health/**").permitAll()
                    .requestMatchers("/management/info").permitAll()
                    .requestMatchers("/management/prometheus").permitAll()
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        http.addFilterBefore(deviceAuthenticationFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
