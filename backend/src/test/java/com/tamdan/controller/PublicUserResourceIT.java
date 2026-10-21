package com.tamdan.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tamdan.IntegrationTest;
import com.tamdan.entities.User;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.AuthoritiesConstants;
import com.tamdan.service.UserService;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PublicUserResource} REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class PublicUserResourceIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc restUserMockMvc;

    private User user;
    private Long numberOfUsers;

    @BeforeEach
    void initTest() {
        numberOfUsers = userRepository.count();
        user = User.builder()
            .login("public-user").password("a".repeat(60)).activated(true).email("public-user@localhost")
            .firstName("Public").lastName("User").langKey("en").build();
    }

    @AfterEach
    void cleanupAndCheck() {
        userService.deleteUser(user.getLogin());
        assertThat(userRepository.count()).isEqualTo(numberOfUsers);
        numberOfUsers = null;
    }

    @Test
    @Transactional
    void getAllPublicUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the users
        restUserMockMvc
            .perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data.content[?(@.id == %d)].login".formatted(user.getId())).value(user.getLogin()))
            .andExpect(jsonPath("$.data.content[?(@.id == %d)].keys()".formatted(user.getId())).value(Set.of("id", "login")))
            .andExpect(jsonPath("$.data.content[*].email").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.data.content[*].imageUrl").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.data.content[*].langKey").doesNotHaveJsonPath());
    }

    @Test
    @Transactional
    void getAllUsersSortedByParameters() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(get("/api/users?sort=resetKey,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        restUserMockMvc.perform(get("/api/users?sort=password,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        restUserMockMvc
            .perform(get("/api/users?sort=resetKey,desc&sort=id,desc").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        restUserMockMvc.perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }
}
