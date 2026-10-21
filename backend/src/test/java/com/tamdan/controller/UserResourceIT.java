package com.tamdan.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tamdan.IntegrationTest;
import com.tamdan.config.Constants;
import com.tamdan.dto.request.AdminUserRequest;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.AuthoritiesConstants;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@IntegrationTest
@Transactional
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class UserResourceIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @Test
    void createsAndReturnsWrappedAdminUser() throws Exception {
        AdminUserRequest request = request(null, "managed-user", "managed-user@example.com");

        mockMvc
            .perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.login").value("managed-user"))
            .andExpect(jsonPath("$.data.password").doesNotExist());

        assertThat(userRepository.findOneByLogin("managed-user")).isPresent();
    }

    @Test
    void listsUsersUsingPageResponse() throws Exception {
        mockMvc
            .perform(get("/api/admin/users?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    void deletesExistingUser() throws Exception {
        AdminUserRequest request = request(null, "delete-user", "delete-user@example.com");
        mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(request)));

        mockMvc.perform(delete("/api/admin/users/delete-user")).andExpect(status().isNoContent());
        assertThat(userRepository.findOneByLogin("delete-user")).isEmpty();
    }

    private AdminUserRequest request(Long id, String login, String email) {
        return new AdminUserRequest(
            id, login, "First", "Last", email, null, true, Constants.DEFAULT_LANGUAGE, Set.of(AuthoritiesConstants.USER)
        );
    }
}
