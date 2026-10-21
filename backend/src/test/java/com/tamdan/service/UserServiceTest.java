package com.tamdan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tamdan.config.Constants;
import com.tamdan.dto.request.ManagedUserVM;
import com.tamdan.entities.Authority;
import com.tamdan.entities.User;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ConflictException;
import com.tamdan.mapper.UserMapper;
import com.tamdan.repository.AuthorityRepository;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.AuthoritiesConstants;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthorityRepository authorityRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, authorityRepository, new UserMapper());
    }

    @Test
    void normalizesLoginAndEmailBeforeRegistration() {
        ManagedUserVM request = registration("  MixedCase  ", "  User@Example.COM  ");
        prepareSuccessfulRegistration();

        User registered = userService.registerUser(request);

        assertThat(registered.getLogin()).isEqualTo("mixedcase");
        assertThat(registered.getEmail()).isEqualTo("user@example.com");
        verify(userRepository).findOneByLogin("mixedcase");
        verify(userRepository).findOneByEmailIgnoreCase("user@example.com");
    }

    @Test
    void rejectsDuplicateLogin() {
        User existing = User.builder().login("existing").activated(true).build();
        when(userRepository.findOneByLogin("existing")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.registerUser(registration("existing", "new@example.com")))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Login name is already in use");
    }

    @Test
    void translatesUniqueConstraintRaceToConflict() {
        when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(Optional.of(Authority.named(AuthoritiesConstants.USER)));
        when(passwordEncoder.encode("password")).thenReturn("a".repeat(60));
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userService.registerUser(registration("new-user", "new@example.com")))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Login or email already exists");
    }

    @Test
    void rejectsIncorrectCurrentPassword() {
        User user = User.builder().id(1L).login("user").password("a".repeat(60)).activated(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "wrong-password", "new-password"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Incorrect password");
    }

    private void prepareSuccessfulRegistration() {
        when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(Optional.of(Authority.named(AuthoritiesConstants.USER)));
        when(passwordEncoder.encode("password")).thenReturn("a".repeat(60));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ManagedUserVM registration(String login, String email) {
        return new ManagedUserVM(login, "First", "Last", email, null, Constants.DEFAULT_LANGUAGE, "password");
    }
}
