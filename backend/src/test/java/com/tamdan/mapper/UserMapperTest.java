package com.tamdan.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.tamdan.dto.response.AdminUserDTO;
import com.tamdan.entities.Authority;
import com.tamdan.entities.User;
import com.tamdan.security.AuthoritiesConstants;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    private static final Long USER_ID = 1L;

    private UserMapper userMapper;
    private User user;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        user = User.builder()
            .id(USER_ID)
            .login("johndoe")
            .password("a".repeat(60))
            .activated(true)
            .email("johndoe@localhost")
            .firstName("john")
            .lastName("doe")
            .imageUrl("image_url")
            .langKey("en")
            .authorities(Set.of(Authority.named(AuthoritiesConstants.USER)))
            .build();
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        user.recordCreation("johndoe", timestamp);
        user.recordModification("johndoe", timestamp);
    }

    @Test
    void mapsUserToAdminResponse() {
        AdminUserDTO response = userMapper.toAdminResponse(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.login()).isEqualTo(user.getLogin());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.activated()).isTrue();
        assertThat(response.authorities()).containsExactly(AuthoritiesConstants.USER);
    }

    @Test
    void mapsUserToPublicResponseWithoutInternalFields() {
        var response = userMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.login()).isEqualTo(user.getLogin());
    }
}
