package com.tamdan.security;

import static com.tamdan.security.JwtAuthenticationTestUtils.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;

@AutoConfigureMockMvc
@AuthenticationIntegrationTest
class TokenAuthenticationIT {

    @Autowired
    private MockMvc mvc;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    private String jwtKey;

    @Test
    void testLoginWithValidToken() throws Exception {
        expectOk(createValidToken(jwtKey));
    }

    @Test
    void testReturnFalseWhenJWThasInvalidSignature() throws Exception {
        expectUnauthorized(createTokenWithDifferentSignature());
    }

    @Test
    void testReturnFalseWhenJWTisMalformed() throws Exception {
        expectUnauthorized(createSignedInvalidJwt(jwtKey));
    }

    @Test
    void testReturnFalseWhenJWTisExpired() throws Exception {
        expectUnauthorized(createExpiredToken(jwtKey));
    }

    @Test
    void deviceCredentialCannotAccessParentRoute() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/family/current").header("X-Device-Credential", "invalid-device-credential"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deviceCredentialCannotAccessParentSessionRoute() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/authenticate/logout").header("X-Device-Credential", "invalid-device-credential"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void parentJwtCannotAccessDeviceOnlyRoute() throws Exception {
        mvc.perform(
            MockMvcRequestBuilders.post("/api/consent/device")
                .header(AUTHORIZATION, BEARER + createValidToken(jwtKey))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionState\":\"ALLOW\"}")
        ).andExpect(status().isForbidden());
    }

    @Test
    void conflictingParentAndDeviceCredentialsAreRejected() throws Exception {
        mvc.perform(
            MockMvcRequestBuilders.get("/api/authenticate")
                .header(AUTHORIZATION, BEARER + createValidToken(jwtKey))
                .header("X-Device-Credential", "invalid-device-credential")
        ).andExpect(status().isUnauthorized());
    }

    private void expectOk(String token) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/authenticate").header(AUTHORIZATION, BEARER + token)).andExpect(
            status().isNoContent()
        );
    }

    private void expectUnauthorized(String token) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/authenticate").header(AUTHORIZATION, BEARER + token)).andExpect(
            status().isUnauthorized()
        );
    }
}
