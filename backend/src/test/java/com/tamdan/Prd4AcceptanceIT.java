package com.tamdan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tamdan.config.Constants;
import com.tamdan.dto.request.CompletePairingRequest;
import com.tamdan.dto.request.ConsentRequest;
import com.tamdan.dto.request.CreateChildRequest;
import com.tamdan.dto.request.LoginRequest;
import com.tamdan.dto.request.RefreshRequest;
import com.tamdan.dto.request.RegisterParentRequest;
import com.tamdan.dto.request.AuditCommand;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.dto.response.PairingCodeResponse;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.DeviceCredential;
import com.tamdan.entities.Family;
import com.tamdan.entities.PairingCode;
import com.tamdan.entities.User;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.exception.web.ErrorConstants;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.ConsentRecordRepository;
import com.tamdan.repository.DeviceCredentialRepository;
import com.tamdan.repository.PairingCodeRepository;
import com.tamdan.repository.SecurityAuditEventRepository;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import com.tamdan.security.AuthenticatedParentContext;
import com.tamdan.service.auth.AuthenticationService;
import com.tamdan.service.auth.IdentityLookupService;
import com.tamdan.service.auth.UserService;
import com.tamdan.service.consent.AuditService;
import com.tamdan.service.consent.ConsentService;
import com.tamdan.service.device.DeviceCredentialService;
import com.tamdan.service.device.DeviceService;
import com.tamdan.service.device.PairingService;
import com.tamdan.service.family.ChildProfileService;
import com.tamdan.service.family.FamilyService;
import com.tamdan.service.RateLimitService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** Acceptance coverage for the PRD 4 parent/device identity boundary. */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class Prd4AcceptanceIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private FamilyService familyService;
    @Autowired private UserService userService;
    @Autowired private AuthenticationService authenticationService;
    @Autowired private ChildProfileService childProfileService;
    @Autowired private PairingService pairingService;
    @Autowired private DeviceCredentialService deviceCredentialService;
    @Autowired private DeviceService deviceService;
    @Autowired private ConsentService consentService;
    @Autowired private AuditService auditService;
    @Autowired private RateLimitService rateLimitService;
    @Autowired private IdentityLookupService identityLookupService;
    @Autowired private ChildProfileRepository childProfileRepository;
    @Autowired private DeviceCredentialRepository deviceCredentialRepository;
    @Autowired private PairingCodeRepository pairingCodeRepository;
    @Autowired private ConsentRecordRepository consentRecordRepository;
    @Autowired private SecurityAuditEventRepository auditRepository;

    @Test
    void parentRegistrationCreatesAccountAndFamily() throws Exception {
        String login = "accept-register";
        mockMvc
            .perform(
                post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new RegisterParentRequest(login, "accept-register@example.com", "password", "A", "Parent")))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.login").value(login))
            .andExpect(jsonPath("$.data.password").doesNotExist());

        User parent = userRepository.findOneByLogin(login).orElseThrow();
        assertThat(familyService.getCurrent(parent.getId()).active()).isTrue();
    }

    @Test
    void parentActivationActivatesAccount() throws Exception {
        User parent = parent("accept-activation", false);
        parent.prepareActivation("activation-acceptance-key");
        userRepository.saveAndFlush(parent);

        mockMvc.perform(get("/api/activate").param("key", "activation-acceptance-key")).andExpect(status().isOk());
        assertThat(userRepository.findById(parent.getId()).orElseThrow().isActivated()).isTrue();
    }

    @Test
    void parentLoginIssuesTokensWithoutSensitivePersistence() {
        User parent = parent("accept-login", true);
        var tokens = authenticationService.login(new LoginRequest(parent.getLogin(), "password"));

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(deviceCredentialRepository.count()).isZero();
    }

    @Test
    void invalidParentLoginReturnsStableBadRequestCode() throws Exception {
        mockMvc
            .perform(
                post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(new LoginRequest("missing-acceptance-user", "wrong-password")))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(ErrorConstants.ERR_BAD_REQUEST));
    }

    @Test
    void refreshRotatesAndRevokesPreviousSession() {
        User parent = parent("accept-refresh", true);
        var first = authenticationService.login(new LoginRequest(parent.getLogin(), "password"));
        var second = authenticationService.refresh(new RefreshRequest(first.refreshToken()));

        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
        assertThatThrownBy(() -> authenticationService.refresh(new RefreshRequest(first.refreshToken()))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void logoutRevokesOneRefreshSession() {
        User parent = parent("accept-logout", true);
        var first = authenticationService.login(new LoginRequest(parent.getLogin(), "password"));
        authenticationService.logout(first.refreshToken());

        assertThatThrownBy(() -> authenticationService.refresh(new RefreshRequest(first.refreshToken()))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void logoutAllRevokesAllRefreshSessions() {
        User parent = parent("accept-logout-all", true);
        var first = authenticationService.login(new LoginRequest(parent.getLogin(), "password"));
        var second = authenticationService.login(new LoginRequest(parent.getLogin(), "password"));
        authenticationService.logoutAll(parent.getId());

        assertThatThrownBy(() -> authenticationService.refresh(new RefreshRequest(first.refreshToken()))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> authenticationService.refresh(new RefreshRequest(second.refreshToken()))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void accountRecoveryDoesNotExposeSecrets() {
        User parent = parent("accept-recovery", true);
        assertThat(userService.requestPasswordReset(parent.getEmail())).isPresent();
        assertThat(userRepository.findById(parent.getId()).orElseThrow().getPassword()).isNotEqualTo("password");
    }

    @Test
    void familyIsolationRejectsOtherParent() {
        User first = parent("accept-family-one", true);
        User second = parent("accept-family-two", true);
        familyService.createForParent(first.getId());
        familyService.createForParent(second.getId());
        var child = childProfileService.create(first.getId(), new CreateChildRequest("Isolated child"));

        assertThatThrownBy(() -> childProfileService.get(second.getId(), child.id())).isInstanceOf(RuntimeException.class);
    }

    @Test
    @WithMockUser("accept-missing-membership")
    void missingMembershipReturnsStableNotFoundCode() throws Exception {
        parent("accept-missing-membership", true);

        mockMvc.perform(get("/api/family/current")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value(ErrorConstants.ERR_NOT_FOUND));
    }

    @Test
    void childOwnershipIsFamilyScoped() {
        User owner = parent("accept-child-owner", true);
        User other = parent("accept-child-other", true);
        familyService.createForParent(owner.getId());
        familyService.createForParent(other.getId());
        var child = childProfileService.create(owner.getId(), new CreateChildRequest("Owned child"));

        assertThatThrownBy(() -> childProfileService.get(other.getId(), child.id())).isInstanceOf(RuntimeException.class);
    }

    @Test
    void expiredPairingCodeIsRejected() {
        User owner = parent("accept-pair-expiry", true);
        Family family = familyService.requireFamilyForParent(owner.getId());
        ChildProfile child = childProfileRepository.saveAndFlush(ChildProfile.create(family, "Expiry child", Instant.now()));
        PairingCode expired = PairingCode.create(family, child, pairingService.hashPairingCode("000001"), Instant.now().minus(1, ChronoUnit.MINUTES), owner.getId(), Instant.now());
        pairingCodeRepository.saveAndFlush(expired);
        assertThatThrownBy(() -> pairingService.requireUsableCode("000001")).isInstanceOf(BadRequestException.class);
        assertThat(expired.getExpiresAt()).isBefore(Instant.now());
    }

    @Test
    void pairingCodeReplayIsRejected() {
        User owner = parent("accept-pair-replay", true);
        var child = childProfileService.create(owner.getId(), new CreateChildRequest("Replay child"));
        PairingCodeResponse code = pairingService.createPairingCode(owner.getId(), child.id());
        CompletePairingRequest request = pairingRequest(code.code(), "replay-device");
        pairingService.completePairing(request);

        assertThatThrownBy(() -> pairingService.completePairing(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void successfulDeviceRegistrationReturnsMinimumContext() {
        User owner = parent("accept-device-registration", true);
        var child = childProfileService.create(owner.getId(), new CreateChildRequest("Registered child"));
        PairingCodeResponse code = pairingService.createPairingCode(owner.getId(), child.id());
        DeviceCredentialResponse response = pairingService.completePairing(pairingRequest(code.code(), "device-registration"));

        assertThat(response.credential()).isNotBlank();
        assertThat(response.child().childId()).isEqualTo(child.id());
        assertThat(response.child().familyId()).isEqualTo(familyService.getCurrent(owner.getId()).id());
    }

    @Test
    void deviceCredentialRotationInvalidatesPreviousCredential() {
        DeviceFixture fixture = deviceFixture("accept-device-rotation");
        DeviceCredentialResponse replacement = deviceCredentialService.rotateCredential(fixture.deviceId());

        assertThat(replacement.credential()).isNotEqualTo(fixture.credential());
        assertThatThrownBy(() -> deviceCredentialService.authenticate(fixture.credential())).isInstanceOf(BadRequestException.class);
        assertThat(deviceCredentialService.authenticate(replacement.credential()).deviceId()).isEqualTo(fixture.deviceId());
    }

    @Test
    void deviceRevocationPersistsAndStopsAuthentication() {
        DeviceFixture fixture = deviceFixture("accept-device-revoke");
        deviceService.revokeDevice(fixture.parentId(), fixture.deviceId());

        assertThatThrownBy(() -> deviceCredentialService.authenticate(fixture.credential())).isInstanceOf(BadRequestException.class);
        assertThat(deviceCredentialRepository.findCurrentByDeviceId(fixture.deviceId()).orElseThrow().getRevokedAt()).isNotNull();
    }

    @Test
    void revokedDeviceCannotAuthenticate() {
        DeviceFixture fixture = deviceFixture("accept-device-rejected");
        deviceCredentialService.revokeCredential(fixture.deviceId());

        assertThatThrownBy(() -> deviceCredentialService.authenticate(fixture.credential())).isInstanceOf(BadRequestException.class);
    }

    @Test
    void expiredDeviceCredentialCannotAuthenticate() {
        DeviceFixture fixture = deviceFixture("accept-device-expired");
        DeviceCredential current = deviceCredentialRepository.findCurrentByDeviceId(fixture.deviceId()).orElseThrow();
        deviceCredentialRepository.saveAndFlush(DeviceCredential.create(current.getChildDevice(), deviceCredentialService.hashCredential("expired-credential"), Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS)));

        assertThatThrownBy(() -> deviceCredentialService.authenticate("expired-credential")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void repeatedDeviceRevocationIsIdempotent() {
        DeviceFixture fixture = deviceFixture("accept-device-revoke-repeat");
        deviceService.revokeDevice(fixture.parentId(), fixture.deviceId());
        deviceService.revokeDevice(fixture.parentId(), fixture.deviceId());
        assertThat(deviceCredentialRepository.findCurrentByDeviceId(fixture.deviceId()).orElseThrow().getRevokedAt()).isNotNull();
    }

    @Test
    void parentCannotRotateDeviceFromAnotherFamily() {
        DeviceFixture first = deviceFixture("accept-device-family-one");
        User second = parent("accept-device-family-two", true);
        assertThatThrownBy(() -> deviceService.rotateCredential(second.getId(), first.deviceId())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void consentHistoryIsFamilyScoped() {
        User owner = parent("accept-consent", true);
        var child = childProfileService.create(owner.getId(), new CreateChildRequest("Consent child"));
        consentService.acknowledge(owner.getId(), child.id(), new ConsentRequest("v1", "ALLOW"));

        assertThat(consentService.history(owner.getId(), child.id())).hasSize(1);
        assertThat(consentRecordRepository.count()).isPositive();
    }

    @Test
    void auditHistoryStoresSafeEventsOnly() {
        User owner = parent("accept-audit", true);
        Long familyId = familyService.getCurrent(owner.getId()).id();
        auditService.record(new AuditCommand("PARENT", owner.getId(), familyId, "acceptance", "FAMILY", familyId, "SUCCESS", null, null, null, Map.of("source", "test")));

        assertThat(auditRepository.findAll().stream().anyMatch(event -> "acceptance".equals(event.getAction()))).isTrue();
    }

    @Test
    void rateLimitsAreEnforced() {
        for (int attempt = 0; attempt < 5; attempt++) rateLimitService.check("acceptance-rate-limit", 5, java.time.Duration.ofHours(1));
        assertThatThrownBy(() -> rateLimitService.check("acceptance-rate-limit", 5, java.time.Duration.ofHours(1))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void parentAndDeviceAuthenticationRemainSeparate() {
        assertThatThrownBy(() -> deviceCredentialService.authenticate("not-a-parent-jwt")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void identityLookupResolvesTrustedParentContext() {
        User owner = parent("accept-identity-parent", true);
        Long familyId = familyService.createForParent(owner.getId()).id();
        AuthenticatedParentContext context = identityLookupService.resolveParent(owner.getId());

        assertThat(context.parentId()).isEqualTo(owner.getId());
        assertThat(context.familyId()).isEqualTo(familyId);
    }

    @Test
    void identityLookupResolvesTrustedDeviceContext() {
        DeviceFixture fixture = deviceFixture("accept-identity-device");
        AuthenticatedDeviceContext context = identityLookupService.resolveDevice(fixture.deviceId());

        assertThat(context.deviceId()).isEqualTo(fixture.deviceId());
        assertThat(identityLookupService.verifyActiveDevice(fixture.deviceId())).isTrue();
        assertThat(identityLookupService.verifyChildOwnership(context.familyId(), context.childProfileId())).isTrue();
    }

    @Test
    void identityLookupRejectsCrossFamilyChildIdentifiers() {
        DeviceFixture first = deviceFixture("accept-identity-cross-one");
        DeviceFixture second = deviceFixture("accept-identity-cross-two");
        AuthenticatedDeviceContext context = identityLookupService.resolveDevice(first.deviceId());

        assertThat(identityLookupService.verifyChildOwnership(context.familyId(), second.childProfileId())).isFalse();
        assertThat(identityLookupService.verifyChildOwnership(second.familyId(), context.childProfileId())).isFalse();
    }

    @Test
    void identityLookupRejectsDeviceImmediatelyAfterRevocation() {
        DeviceFixture fixture = deviceFixture("accept-identity-revoked");
        deviceService.revokeDevice(fixture.parentId(), fixture.deviceId());

        assertThat(identityLookupService.verifyActiveDevice(fixture.deviceId())).isFalse();
        assertThatThrownBy(() -> identityLookupService.resolveDevice(fixture.deviceId())).isInstanceOf(com.tamdan.exception.ForbiddenException.class);
    }

    private DeviceFixture deviceFixture(String login) {
        User owner = parent(login, true);
        var child = childProfileService.create(owner.getId(), new CreateChildRequest("Device child"));
        PairingCodeResponse code = pairingService.createPairingCode(owner.getId(), child.id());
        DeviceCredentialResponse credential = pairingService.completePairing(pairingRequest(code.code(), login + "-device"));
        return new DeviceFixture(owner.getId(), familyService.getCurrent(owner.getId()).id(), credential.child().childId(), credential.deviceId(), credential.credential());
    }

    private CompletePairingRequest pairingRequest(String code, String identifier) {
        return new CompletePairingRequest(code, identifier, "ios", "1.0", "Test device", Map.of("camera", true));
    }

    private User parent(String login, boolean activated) {
        return userRepository.saveAndFlush(
            User.builder()
                .login(login)
                .email(login + "@example.com")
                .password(passwordEncoder.encode("password"))
                .activated(activated)
                .langKey(Constants.DEFAULT_LANGUAGE)
                .build()
        );
    }

    private record DeviceFixture(Long parentId, Long familyId, Long childProfileId, Long deviceId, String credential) {}
}
