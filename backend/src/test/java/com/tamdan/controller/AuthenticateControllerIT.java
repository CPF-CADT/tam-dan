package com.tamdan.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tamdan.IntegrationTest;
import com.tamdan.dto.request.LoginVM;
import com.tamdan.entities.User;
import com.tamdan.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@IntegrationTest
@Transactional
class AuthenticateControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void returnsTokenForValidCredentials() throws Exception {
        userRepository.saveAndFlush(activeUser("login-user", "password"));

        mockMvc
            .perform(
                post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new LoginVM("login-user", "password", false)))
            )
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
            .andExpect(jsonPath("$.data.id_token").isNotEmpty());
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc
            .perform(
                post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new LoginVM("missing-user", "wrong-password", false)))
            )
            .andExpect(status().isUnauthorized());
    }

    private User activeUser(String login, String password) {
        return User.builder()
            .login(login)
            .email(login + "@example.com")
            .password(passwordEncoder.encode(password))
            .activated(true)
            .build();
    }
}
