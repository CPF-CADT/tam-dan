package com.tamdan.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tamdan.IntegrationTest;
import com.tamdan.dto.request.AuthorityRequest;
import com.tamdan.repository.AuthorityRepository;
import com.tamdan.security.AuthoritiesConstants;
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
class AuthorityResourceIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthorityRepository authorityRepository;

    @Test
    void createsAuthorityWithoutExposingEntity() throws Exception {
        mockMvc
            .perform(
                post("/api/authorities")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new AuthorityRequest("role_test")))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("ROLE_TEST"));

        assertThat(authorityRepository.existsById("ROLE_TEST")).isTrue();
    }

    @Test
    void returnsAndDeletesAuthority() throws Exception {
        authorityRepository.saveAndFlush(com.tamdan.entities.Authority.named("ROLE_DELETE"));

        mockMvc.perform(get("/api/authorities/ROLE_DELETE")).andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("ROLE_DELETE"));
        mockMvc.perform(delete("/api/authorities/ROLE_DELETE")).andExpect(status().isNoContent());
        authorityRepository.flush();
        assertThat(authorityRepository.existsById("ROLE_DELETE")).isFalse();
    }
}
