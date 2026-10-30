package com.tamdan.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tamdan.config.Constants;
import com.tamdan.dto.request.ManagedUserVM;
import com.tamdan.dto.request.RegisterParentRequest;
import com.tamdan.dto.request.UpdateParentRequest;
import com.tamdan.dto.response.ParentAccountResponse;
import com.tamdan.entities.Authority;
import com.tamdan.entities.User;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ConflictException;
import com.tamdan.mail.MailService;
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
    @Mock private MailService mailService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, authorityRepository, new UserMapper(), mailService);
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
    void registersParentWithNormalizedIdentityAndHashedPassword() {
        RegisterParentRequest request = new RegisterParentRequest(
            "  ParentUser  ", "  Parent@Example.COM  ", "password", "First", "Last"
        );
        prepareSuccessfulRegistration();

        ParentAccountResponse response = userService.register(request);

        assertThat(response.login()).isEqualTo("parentuser");
        assertThat(response.email()).isEqualTo("parent@example.com");
        assertThat(response.activated()).isFalse();
        assertThat(response.authorities()).containsExactly(AuthoritiesConstants.USER);
        verify(passwordEncoder).encode("password");
        verify(mailService).sendActivationEmail(any(User.class));
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

    @Test
    void updatesOnlyTheAuthenticatedParent() {
        User user = User.builder()
            .id(1L).login("parent").email("old@example.com").firstName("Old").lastName("Name")
            .activated(true).langKey(Constants.DEFAULT_LANGUAGE).build();
        when(userRepository.findOneWithAuthoritiesById(1L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        ParentAccountResponse response = userService.update(1L, new UpdateParentRequest(" NEW@Example.com ", "New", "Parent"));

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(user.getFirstName()).isEqualTo("New");
        verify(userRepository).findOneWithAuthoritiesById(1L);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void activatesAccountByActivationKey() {
        User user = User.builder().login("parent").activated(false).build();
        user.prepareActivation("activation-key");
        when(userRepository.findOneByActivationKey("activation-key")).thenReturn(Optional.of(user));

        userService.activate("activation-key");

        assertThat(user.isActivated()).isTrue();
        assertThat(user.getActivationKey()).isNull();
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
