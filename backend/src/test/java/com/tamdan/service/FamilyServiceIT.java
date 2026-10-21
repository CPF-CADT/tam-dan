package com.tamdan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tamdan.IntegrationTest;
import com.tamdan.config.Constants;
import com.tamdan.entities.User;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.FamilyMemberRepository;
import com.tamdan.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class FamilyServiceIT {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser("family-current")
    void currentFamilyLookupUsesAuthenticatedParent() throws Exception {
        User parent = saveParent("family-current");
        familyService.createForParent(parent.getId());

        mockMvc.perform(get("/api/family/current")).andExpect(status().isOk()).andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @WithMockUser("family-create")
    void familyEndpointCreatesForAuthenticatedParent() throws Exception {
        User parent = saveParent("family-create");

        mockMvc.perform(post("/api/family").accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(jsonPath("$.data.id").isNumber());
        assertThat(familyMemberRepository.findByParentIdAndActiveTrue(parent.getId())).isPresent();
    }

    @Test
    void missingMembershipIsRejected() {
        User parent = saveParent("family-missing");

        assertThatThrownBy(() -> familyService.getCurrent(parent.getId())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void inactiveFamilyIsRejected() {
        User parent = saveParent("family-inactive");
        var response = familyService.createForParent(parent.getId());
        var family = familyService.requireFamilyForParent(parent.getId());
        family.deactivate();

        assertThatThrownBy(() -> familyService.getCurrent(parent.getId())).isInstanceOf(ResourceNotFoundException.class);
        assertThat(response.id()).isEqualTo(family.getId());
    }

    @Test
    void parentCannotAccessAnotherParentsFamily() {
        User first = saveParent("family-owner");
        User second = saveParent("family-other");
        familyService.createForParent(first.getId());

        assertThatThrownBy(() -> familyService.getCurrent(second.getId())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void familyProvisioningIsIdempotent() {
        User parent = saveParent("family-duplicate");

        var first = familyService.createForParent(parent.getId());
        var second = familyService.createForParent(parent.getId());

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(familyMemberRepository.findByParentIdAndActiveTrue(parent.getId())).isPresent();
    }

    private User saveParent(String prefix) {
        return userRepository.saveAndFlush(
            User.builder()
                .login(prefix)
                .email(prefix + "@example.com")
                .password("x".repeat(60))
                .activated(true)
                .langKey(Constants.DEFAULT_LANGUAGE)
                .build()
        );
    }
}
