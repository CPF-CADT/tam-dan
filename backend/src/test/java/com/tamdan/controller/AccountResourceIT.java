package com.tamdan.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tamdan.IntegrationTest;
import com.tamdan.config.Constants;
import com.tamdan.dto.request.KeyAndPasswordVM;
import com.tamdan.dto.request.ManagedUserVM;
import com.tamdan.dto.request.PasswordChangeDTO;
import com.tamdan.dto.request.UpdateParentRequest;
import com.tamdan.entities.User;
import com.tamdan.repository.UserRepository;
import com.tamdan.repository.FamilyMemberRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@IntegrationTest
@Transactional
class AccountResourceIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUnauthenticatedMockUser
    void rejectsUnauthenticatedAuthenticationCheck() throws Exception {
        mockMvc.perform(get("/api/authenticate")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser("authenticated-user")
    void acceptsAuthenticatedAuthenticationCheck() throws Exception {
        mockMvc.perform(get("/api/authenticate")).andExpect(status().isNoContent());
    }

    @Test
    void registersValidUser() throws Exception {
        ManagedUserVM request = registration("register-valid", "register-valid@example.com", "password");

        mockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.login").value("register-valid"))
            .andExpect(jsonPath("$.data.email").value("register-valid@example.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());

        assertThat(userRepository.findOneByLogin("register-valid")).isPresent();
        Long parentId = userRepository.findOneByLogin("register-valid").orElseThrow().getId();
        assertThat(familyMemberRepository.findByParentIdAndActiveTrue(parentId)).isPresent();
    }

    @Test
    void rejectsInvalidRegistration() throws Exception {
        ManagedUserVM request = registration("invalid log(in", "invalid", "123");

        mockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateLoginWithConflict() throws Exception {
        userRepository.saveAndFlush(activeUser("duplicate-login", "original@example.com", passwordEncoder.encode("password")));
        ManagedUserVM request = registration("duplicate-login", "different@example.com", "password");

        mockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void rejectsDuplicateEmailWithConflict() throws Exception {
        userRepository.saveAndFlush(activeUser("original-login", "duplicate@example.com", passwordEncoder.encode("password")));
        ManagedUserVM request = registration("different-login", "duplicate@example.com", "password");

        mockMvc
            .perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser("account-user")
    void returnsWrappedCurrentAccount() throws Exception {
        userRepository.saveAndFlush(activeUser("account-user", "account-user@example.com", passwordEncoder.encode("password")));

        mockMvc
            .perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.login").value("account-user"))
            .andExpect(jsonPath("$.data.email").value("account-user@example.com"));
    }

    @Test
    @WithMockUser("update-parent")
    void updatesAuthenticatedParentAccount() throws Exception {
        userRepository.saveAndFlush(activeUser("update-parent", "old-parent@example.com", passwordEncoder.encode("password")));

        mockMvc
            .perform(
                post("/api/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new UpdateParentRequest("new-parent@example.com", "New", "Parent")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.login").value("update-parent"))
            .andExpect(jsonPath("$.data.email").value("new-parent@example.com"));

        User updated = userRepository.findOneByLogin("update-parent").orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("New");
        assertThat(updated.getLastName()).isEqualTo("Parent");
    }

    @Test
    void activatesParentAccount() throws Exception {
        User user = activeUser("activation-parent", "activation-parent@example.com", passwordEncoder.encode("password"));
        user.prepareActivation("activation-key");
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/activate").param("key", "activation-key")).andExpect(status().isOk());

        assertThat(userRepository.findOneByLogin("activation-parent").orElseThrow().isActivated()).isTrue();
    }

    @Test
    @WithMockUser("password-user")
    void changesPasswordWhenCurrentPasswordMatches() throws Exception {
        userRepository.saveAndFlush(activeUser("password-user", "password-user@example.com", passwordEncoder.encode("current-password")));

        mockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new PasswordChangeDTO("current-password", "new-password")))
            )
            .andExpect(status().isOk());

        User updated = userRepository.findOneByLogin("password-user").orElseThrow();
        assertThat(passwordEncoder.matches("new-password", updated.getPassword())).isTrue();
    }

    @Test
    @WithMockUser("incorrect-password-user")
    void rejectsIncorrectCurrentPasswordWithBadRequest() throws Exception {
        userRepository.saveAndFlush(
            activeUser("incorrect-password-user", "incorrect-password@example.com", passwordEncoder.encode("current-password"))
        );

        mockMvc
            .perform(
                post("/api/account/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new PasswordChangeDTO("wrong-password", "new-password")))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void completesValidPasswordReset() throws Exception {
        User user = activeUser("reset-user", "reset-user@example.com", passwordEncoder.encode("old-password"));
        user.preparePasswordReset("valid-reset-key", Instant.now());
        userRepository.saveAndFlush(user);

        mockMvc
            .perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new KeyAndPasswordVM("valid-reset-key", "new-password")))
            )
            .andExpect(status().isOk());

        User updated = userRepository.findOneByLogin("reset-user").orElseThrow();
        assertThat(passwordEncoder.matches("new-password", updated.getPassword())).isTrue();
    }

    private ManagedUserVM registration(String login, String email, String password) {
        return new ManagedUserVM(login, "First", "Last", email, null, Constants.DEFAULT_LANGUAGE, password);
    }

    private User activeUser(String login, String email, String encodedPassword) {
        return User.builder()
            .login(login)
            .email(email)
            .password(encodedPassword)
            .activated(true)
            .langKey(Constants.DEFAULT_LANGUAGE)
            .build();
    }
}
