package com.tamdan.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.tamdan.IntegrationTest;
import com.tamdan.entities.User;
import com.tamdan.repository.UserRepository;
import com.tamdan.service.UserService;
import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integrations tests for {@link DomainUserDetailsService}.
 */
@Transactional
@IntegrationTest
class DomainUserDetailsServiceIT {

    private static final String USER_ONE_LOGIN = "test-user-one";
    private static final String USER_ONE_EMAIL = "test-user-one@localhost";
    private static final String USER_TWO_LOGIN = "test-user-two";
    private static final String USER_TWO_EMAIL = "test-user-two@localhost";
    private static final String USER_THREE_LOGIN = "test-user-three";
    private static final String USER_THREE_EMAIL = "test-user-three@localhost";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService domainUserDetailsService;

    public User getUserOne() {
        return User.builder()
            .login(USER_ONE_LOGIN).password(RandomStringUtils.insecure().nextAlphanumeric(60)).activated(true)
            .email(USER_ONE_EMAIL).firstName("userOne").lastName("doe").langKey("en").build();
    }

    public User getUserTwo() {
        return User.builder()
            .login(USER_TWO_LOGIN).password(RandomStringUtils.insecure().nextAlphanumeric(60)).activated(true)
            .email(USER_TWO_EMAIL).firstName("userTwo").lastName("doe").langKey("en").build();
    }

    public User getUserThree() {
        return User.builder()
            .login(USER_THREE_LOGIN).password(RandomStringUtils.insecure().nextAlphanumeric(60)).activated(false)
            .email(USER_THREE_EMAIL).firstName("userThree").lastName("doe").langKey("en").build();
    }

    @BeforeEach
    void init() {
        userRepository.save(getUserOne());
        userRepository.save(getUserTwo());
        userRepository.save(getUserThree());
    }

    @AfterEach
    void cleanup() {
        userService.deleteUser(USER_ONE_LOGIN);
        userService.deleteUser(USER_TWO_LOGIN);
        userService.deleteUser(USER_THREE_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByLogin() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByLoginIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN.toUpperCase(Locale.ENGLISH));
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByEmail() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByEmailIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL.toUpperCase(Locale.ENGLISH));
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
    }

    @Test
    void assertThatEmailIsPrioritizedOverLogin() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserNotActivatedExceptionIsThrownForNotActivatedUsers() {
        assertThatExceptionOfType(UserNotActivatedException.class).isThrownBy(() ->
            domainUserDetailsService.loadUserByUsername(USER_THREE_LOGIN)
        );
    }
}
