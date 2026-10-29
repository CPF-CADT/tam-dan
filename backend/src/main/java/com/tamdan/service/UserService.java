package com.tamdan.service;

import com.tamdan.config.Constants;
import com.tamdan.dto.request.AccountUpdateRequest;
import com.tamdan.dto.request.AdminUserRequest;
import com.tamdan.dto.request.ManagedUserVM;
import com.tamdan.dto.request.RegisterParentRequest;
import com.tamdan.dto.request.UpdateParentRequest;
import com.tamdan.dto.response.AdminUserDTO;
import com.tamdan.dto.response.ParentAccountResponse;
import com.tamdan.dto.response.UserDTO;
import com.tamdan.entities.Authority;
import com.tamdan.entities.User;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ConflictException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.mapper.UserMapper;
import com.tamdan.mail.MailService;
import com.tamdan.repository.AuthorityRepository;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.AuthoritiesConstants;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final UserMapper userMapper;
    private final MailService mailService;
    private final FamilyService familyService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    @Autowired
    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        UserMapper userMapper,
        MailService mailService,
        FamilyService familyService,
        AuditService auditService,
        RateLimitService rateLimitService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.userMapper = userMapper;
        this.mailService = mailService;
        this.familyService = familyService;
        this.auditService = auditService;
        this.rateLimitService = rateLimitService;
    }

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthorityRepository authorityRepository,
        UserMapper userMapper,
        MailService mailService
    ) {
        this(userRepository, passwordEncoder, authorityRepository, userMapper, mailService, null, null, null);
    }

    @Transactional
    public ParentAccountResponse register(RegisterParentRequest request) {
        String login = normalizeLogin(request.login());
        String email = normalizeEmail(request.email());
        if (rateLimitService != null) rateLimitService.check(RateLimitService.Operation.REGISTRATION, login);
        try {
            ensureRegistrationIdentityAvailable(login, email);
        } catch (ConflictException exception) {
            if (auditService != null) auditService.recordFailure(new com.tamdan.dto.request.AuditCommand("PARENT", null, null, "registration", "PARENT", null, "FAILURE", null, null, null, java.util.Map.of()));
            throw exception;
        }

        User user = userMapper.toEntity(
            new RegisterParentRequest(login, email, request.password(), request.firstName(), request.lastName()),
            passwordEncoder.encode(request.password())
        );
        user.prepareActivation(RandomUtil.generateActivationKey());
        user.replaceAuthorities(Set.of(requireAuthority(AuthoritiesConstants.USER)));
        User savedUser = saveWithConflictTranslation(user);
        if (familyService != null) familyService.createForParent(savedUser.getId());
        mailService.sendActivationEmail(savedUser);
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", savedUser.getId(), null, "registration", "PARENT", savedUser.getId(), "SUCCESS", null, null, null, java.util.Map.of()));
        return userMapper.toParentResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public ParentAccountResponse getCurrent(Long authenticatedParentId) {
        return userMapper.toParentResponse(requireUserWithAuthorities(authenticatedParentId));
    }

    @Transactional
    public ParentAccountResponse update(Long authenticatedParentId, UpdateParentRequest request) {
        User user = requireUserWithAuthorities(authenticatedParentId);
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, authenticatedParentId);
        user.updateProfile(request.firstName(), request.lastName(), email, user.getLangKey(), user.getImageUrl());
        return userMapper.toParentResponse(saveWithConflictTranslation(user));
    }

    @Transactional
    public void activate(String activationKey) {
        User activated = activateRegistration(activationKey).orElse(null);
        if (activated != null) {
            if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", activated.getId(), null, "activation", "PARENT", activated.getId(), "SUCCESS", null, null, null, java.util.Map.of()));
            return;
        }
        if (auditService != null) auditService.recordFailure(new com.tamdan.dto.request.AuditCommand("PARENT", null, null, "activation", "PARENT", null, "FAILURE", null, null, null, java.util.Map.of()));
        throw new ResourceNotFoundException("Activation key was not found");
    }

    @Transactional
    public void requestActivationResend(String email) {
        if (rateLimitService != null) rateLimitService.check(RateLimitService.Operation.ACTIVATION_RESEND, normalizeEmail(email));
        userRepository.findOneByEmailIgnoreCase(normalizeEmail(email)).filter(user -> !user.isActivated()).ifPresent(user -> {
            user.prepareActivation(RandomUtil.generateActivationKey());
            mailService.sendActivationEmail(user);
        });
    }

    @Transactional
    public Optional<User> activateRegistration(String key) {
        return userRepository.findOneByActivationKey(key).map(user -> {
            user.activate();
            return user;
        });
    }

    @Transactional
    public Optional<User> completePasswordReset(String newPassword, String key) {
        Optional<User> result = userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate() != null && user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.completePasswordReset(passwordEncoder.encode(newPassword));
                if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", null, null, "recovery-completion", "PARENT", null, "SUCCESS", null, null, null, java.util.Map.of()));
                return user;
            });
        if (result.isEmpty() && auditService != null) auditService.recordFailure(new com.tamdan.dto.request.AuditCommand("PARENT", null, null, "recovery-completion", "PARENT", null, "FAILURE", null, null, null, java.util.Map.of()));
        return result;
    }

    @Transactional
    public Optional<User> requestPasswordReset(String email) {
        if (rateLimitService != null) rateLimitService.check(RateLimitService.Operation.RECOVERY, normalizeEmail(email));
        Optional<User> result = userRepository.findOneByEmailIgnoreCase(normalizeEmail(email)).filter(User::isActivated).map(user -> {
            user.preparePasswordReset(RandomUtil.generateResetKey(), Instant.now());
            return user;
        });
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", null, null, "recovery-request", "PARENT", null, "ACCEPTED", null, null, null, java.util.Map.of()));
        return result;
    }

    @Transactional
    public User registerUser(ManagedUserVM request) {
        String login = normalizeLogin(request.login());
        String email = normalizeEmail(request.email());
        ensureRegistrationIdentityAvailable(login, email);

        User user = userMapper.toEntity(
            new ManagedUserVM(login, request.firstName(), request.lastName(), email, request.imageUrl(), request.langKey(), request.password()),
            passwordEncoder.encode(request.password())
        );
        user.prepareActivation(RandomUtil.generateActivationKey());
        user.replaceAuthorities(Set.of(requireAuthority(AuthoritiesConstants.USER)));
        return saveWithConflictTranslation(user);
    }

    @Transactional
    public User createUser(AdminUserRequest request) {
        String login = normalizeLogin(request.login());
        String email = normalizeEmail(request.email());
        ensureIdentityAvailable(login, email, null);

        User user = User.builder()
            .login(login)
            .password(passwordEncoder.encode(RandomUtil.generatePassword()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(email)
            .imageUrl(request.imageUrl())
            .activated(true)
            .langKey(request.langKey() == null ? Constants.DEFAULT_LANGUAGE : request.langKey())
            .resetKey(RandomUtil.generateResetKey())
            .resetDate(Instant.now())
            .authorities(resolveAuthorities(request.authorities()))
            .build();
        return saveWithConflictTranslation(user);
    }

    @Transactional
    public AdminUserDTO updateUser(AdminUserRequest request) {
        if (request.id() == null) throw new ResourceNotFoundException("User ID is required");
        User user = requireUser(request.id());
        String login = normalizeLogin(request.login());
        String email = normalizeEmail(request.email());
        ensureIdentityAvailable(login, email, user.getId());
        user.updateAdminDetails(login, request.firstName(), request.lastName(), email, request.imageUrl(), request.activated(), request.langKey());
        user.replaceAuthorities(resolveAuthorities(request.authorities()));
        saveWithConflictTranslation(user);
        return userMapper.toAdminResponse(user);
    }

    @Transactional
    public void updateCurrentUser(long userId, AccountUpdateRequest request) {
        User user = requireUser(userId);
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, userId);
        user.updateProfile(request.firstName(), request.lastName(), email, request.langKey(), request.imageUrl());
        saveWithConflictTranslation(user);
    }

    @Transactional
    public void changePassword(long userId, String currentPassword, String newPassword) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) throw new BadRequestException("Incorrect password");
        user.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteUser(String login) {
        User user = requireUserByLogin(login);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminUserDTO getAdminUser(String login) {
        return userMapper.toAdminResponse(requireUserWithAuthorities(login));
    }

    @Transactional(readOnly = true)
    public AdminUserDTO getCurrentUser(long userId) {
        return userMapper.toAdminResponse(requireUserWithAuthorities(userId));
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(normalizeLogin(login));
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                LOG.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
            });
    }

    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).toList();
    }

    private void ensureRegistrationIdentityAvailable(String login, String email) {
        userRepository.findOneByLogin(login).ifPresent(existing -> {
            throw new ConflictException("Login name is already in use");
        });
        userRepository.findOneByEmailIgnoreCase(email).ifPresent(existing -> {
            throw new ConflictException("Email is already in use");
        });
    }

    private void ensureIdentityAvailable(String login, String email, Long excludedUserId) {
        userRepository.findOneByLogin(login).filter(existing -> !existing.getId().equals(excludedUserId)).ifPresent(existing -> {
            throw new ConflictException("Login name is already in use");
        });
        if (email != null) {
            userRepository.findOneByEmailIgnoreCase(email).filter(existing -> !existing.getId().equals(excludedUserId)).ifPresent(existing -> {
                throw new ConflictException("Email is already in use");
            });
        }
    }

    private void ensureEmailAvailable(String email, Long excludedUserId) {
        userRepository.findOneByEmailIgnoreCase(email).filter(existing -> !existing.getId().equals(excludedUserId)).ifPresent(existing -> {
            throw new ConflictException("Email is already in use");
        });
    }

    private User requireUser(long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private User requireUserByLogin(String login) {
        return userRepository.findOneByLogin(normalizeLogin(login)).orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private User requireUserWithAuthorities(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(normalizeLogin(login)).orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private User requireUserWithAuthorities(long id) {
        return userRepository.findOneWithAuthoritiesById(id).orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private Authority requireAuthority(String name) {
        return authorityRepository.findById(name).orElseThrow(() -> new ResourceNotFoundException("Authority was not found: " + name));
    }

    private Set<Authority> resolveAuthorities(Set<String> names) {
        if (names == null) return new HashSet<>();
        return names.stream().map(this::requireAuthority).collect(Collectors.toSet());
    }

    private User saveWithConflictTranslation(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Login or email already exists");
        }
    }

    private String normalizeLogin(String login) {
        return login == null ? null : login.trim().toLowerCase(Locale.ENGLISH);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ENGLISH);
    }
}
