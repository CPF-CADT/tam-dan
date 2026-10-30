package com.tamdan.service;

import com.tamdan.config.Constants;
import com.tamdan.entities.Authority;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.ConsentRecord;
import com.tamdan.entities.DeviceCredential;
import com.tamdan.entities.Family;
import com.tamdan.entities.FamilyMember;
import com.tamdan.entities.PolicyDelivery;
import com.tamdan.entities.SafetyAlert;
import com.tamdan.entities.SafetyEvent;
import com.tamdan.entities.SafetyPolicy;
import com.tamdan.entities.SecurityAuditEvent;
import com.tamdan.entities.User;
import com.tamdan.repository.AuthorityRepository;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.ConsentRecordRepository;
import com.tamdan.repository.DeviceCredentialRepository;
import com.tamdan.repository.FamilyMemberRepository;
import com.tamdan.repository.FamilyRepository;
import com.tamdan.repository.PolicyDeliveryRepository;
import com.tamdan.repository.SafetyAlertRepository;
import com.tamdan.repository.SafetyEventRepository;
import com.tamdan.repository.SafetyPolicyRepository;
import com.tamdan.repository.SecurityAuditEventRepository;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.AuthoritiesConstants;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds the opt-in local demo account and its owned safety data. */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "tamdan.demo-data.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    public static final String DEMO_LOGIN = "demo.parent@example.test";
    public static final String DEMO_PASSWORD = "TamDan-Demo-2026!";
    private static final String DEMO_DEVICE_A = "tamdan-demo-device-child-a-v1";
    private static final String DEMO_DEVICE_B = "tamdan-demo-device-child-b-v1";
    private static final Instant DEMO_BASE_TIME = Instant.parse("2026-01-15T08:00:00Z");

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ChildDeviceRepository childDeviceRepository;
    private final DeviceCredentialRepository deviceCredentialRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final SafetyEventRepository safetyEventRepository;
    private final SafetyAlertRepository safetyAlertRepository;
    private final SafetyPolicyRepository safetyPolicyRepository;
    private final PolicyDeliveryRepository policyDeliveryRepository;
    private final SecurityAuditEventRepository securityAuditEventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        User parent = ensureParent();
        Family family = ensureFamily(parent);
        ChildProfile childA = ensureChild(family, "Mina");
        ChildProfile childB = ensureChild(family, "Dara");
        ChildDevice deviceA = ensureDevice(childA, DEMO_DEVICE_A, "Mina's phone", "ANDROID");
        ChildDevice deviceB = ensureDevice(childB, DEMO_DEVICE_B, "Dara's tablet", "IOS");

        ensureCredential(deviceA, "tamdan-demo-credential-child-a-v1");
        ensureCredential(deviceB, "tamdan-demo-credential-child-b-v1");
        ensureConsent(family, childA, parent, "privacy-v1", DEMO_BASE_TIME.plus(1, ChronoUnit.HOURS));
        ensureConsent(family, childB, parent, "privacy-v1", DEMO_BASE_TIME.plus(1, ChronoUnit.HOURS));

        ensureEvent(deviceA, "demo-activity-001", SafetyEvent.Kind.ACTIVITY, "{\"app\":\"Maps\",\"minutes\":18}", DEMO_BASE_TIME);
        ensureEvent(deviceA, "demo-domain-001", SafetyEvent.Kind.DOMAIN, "{\"domain\":\"example-risk.test\",\"risk\":\"RISK\"}", DEMO_BASE_TIME.plus(30, ChronoUnit.MINUTES));
        ensureEvent(deviceB, "demo-activity-002", SafetyEvent.Kind.ACTIVITY, "{\"app\":\"Reading\",\"minutes\":24}", DEMO_BASE_TIME.plus(45, ChronoUnit.MINUTES));
        ensureAlert(childA, "demo-domain-001");

        SafetyPolicy policy = ensurePolicy(childA);
        ensureDeliveryAcknowledgement(policy, deviceA, DEMO_BASE_TIME.plus(2, ChronoUnit.HOURS));
        policy.activate();

        ensureAuditEvent(family, parent, "demo-fixture-seeded-v1", "DEMO_FIXTURE", "SUCCESS");
    }

    private User ensureParent() {
        User parent = userRepository.findOneByLogin(DEMO_LOGIN).orElseGet(() -> userRepository.saveAndFlush(User.builder()
            .login(DEMO_LOGIN)
            .password(passwordEncoder.encode(DEMO_PASSWORD))
            .firstName("Demo")
            .lastName("Parent")
            .email(DEMO_LOGIN)
            .activated(true)
            .langKey(Constants.DEFAULT_LANGUAGE)
            .authorities(Set.of(requireUserAuthority()))
            .build()));
        if (parent.getAuthorities().stream().noneMatch(authority -> AuthoritiesConstants.USER.equals(authority.getName()))) {
            parent.replaceAuthorities(Set.of(requireUserAuthority()));
            parent = userRepository.save(parent);
        }
        return parent;
    }

    private Authority requireUserAuthority() {
        return authorityRepository.findById(AuthoritiesConstants.USER).orElseGet(() -> authorityRepository.save(Authority.named(AuthoritiesConstants.USER)));
    }

    private Family ensureFamily(User parent) {
        return familyMemberRepository.findByParentIdAndActiveTrue(parent.getId()).map(FamilyMember::getFamily).orElseGet(() -> {
            Family family = familyRepository.saveAndFlush(Family.create(DEMO_BASE_TIME));
            familyMemberRepository.save(FamilyMember.owner(family, parent, DEMO_BASE_TIME));
            return family;
        });
    }

    private ChildProfile ensureChild(Family family, String displayName) {
        return childProfileRepository.findAllByFamilyIdAndActiveTrue(family.getId()).stream()
            .filter(child -> displayName.equals(child.getDisplayName()))
            .findFirst()
            .orElseGet(() -> childProfileRepository.save(ChildProfile.create(family, displayName, DEMO_BASE_TIME)));
    }

    private ChildDevice ensureDevice(ChildProfile child, String stableIdentifier, String displayName, String platform) {
        String identifierHash = sha256(stableIdentifier);
        return childDeviceRepository.findByDeviceIdentifierHash(identifierHash).orElseGet(() -> childDeviceRepository.save(ChildDevice.create(
            child, identifierHash, platform, "1.0.0-demo", displayName, "{\"location\":true,\"activity\":true}", DEMO_BASE_TIME
        )));
    }

    private void ensureCredential(ChildDevice device, String rawCredential) {
        if (deviceCredentialRepository.findCurrentByDeviceId(device.getId()).isEmpty()) {
            deviceCredentialRepository.save(DeviceCredential.create(device, sha256(rawCredential), DEMO_BASE_TIME, DEMO_BASE_TIME.plus(365, ChronoUnit.DAYS)));
        }
    }

    private void ensureConsent(Family family, ChildProfile child, User parent, String documentVersion, Instant acknowledgedAt) {
        boolean exists = consentRecordRepository.findAllByChildProfileIdAndFamilyIdOrderByAcknowledgedAtDesc(child.getId(), family.getId()).stream()
            .anyMatch(record -> documentVersion.equals(record.getDocumentVersion()) && parent.getId().equals(record.getActorId()));
        if (!exists) consentRecordRepository.save(ConsentRecord.create(family, child, documentVersion, "PARENT", parent.getId(), acknowledgedAt, "GRANTED"));
    }

    private void ensureEvent(ChildDevice device, String eventId, SafetyEvent.Kind kind, String payload, Instant occurredAt) {
        if (safetyEventRepository.findByChildDeviceIdAndEventId(device.getId(), eventId).isEmpty()) {
            safetyEventRepository.save(SafetyEvent.create(device, eventId, kind, "1.0", occurredAt, payload, DEMO_BASE_TIME.plus(3, ChronoUnit.HOURS)));
        }
    }

    private void ensureAlert(ChildProfile child, String dedupeKey) {
        if (safetyAlertRepository.findByChildProfileIdAndDedupeKey(child.getId(), dedupeKey).isEmpty()) {
            safetyAlertRepository.save(SafetyAlert.create(child, dedupeKey, SafetyAlert.Severity.HIGH, "Risk detected", "A demo domain observation requires review", DEMO_BASE_TIME.plus(3, ChronoUnit.HOURS)));
        }
    }

    private SafetyPolicy ensurePolicy(ChildProfile child) {
        return safetyPolicyRepository.findByChildProfileIdAndTarget(child.getId(), "example-risk.test")
            .orElseGet(() -> safetyPolicyRepository.save(SafetyPolicy.create(child, SafetyPolicy.Type.DOMAIN, "example-risk.test", DEMO_BASE_TIME)));
    }

    private void ensureDeliveryAcknowledgement(SafetyPolicy policy, ChildDevice device, Instant reportedAt) {
        if (!policy.getChildProfile().getId().equals(device.getChildProfile().getId())) {
            throw new IllegalArgumentException("Demo policy delivery must belong to the policy child");
        }
        PolicyDelivery delivery = policyDeliveryRepository.findByPolicyIdAndChildDeviceId(policy.getId(), device.getId()).orElseGet(() ->
            policyDeliveryRepository.save(PolicyDelivery.pending(policy, device))
        );
        if (delivery.getAcknowledgedVersion() == null) delivery.acknowledge(policy.getVersion(), PolicyDelivery.State.ACTIVE, reportedAt, null);
    }

    private void ensureAuditEvent(Family family, User parent, String requestId, String resourceType, String result) {
        if (!securityAuditEventRepository.existsByFamilyIdAndRequestId(family.getId(), requestId)) {
            securityAuditEventRepository.save(SecurityAuditEvent.create(
                "PARENT", parent.getId(), family.getId(), "demo-fixture", resourceType, family.getId(), result,
                requestId, null, null, "{}", DEMO_BASE_TIME.plus(4, ChronoUnit.HOURS)
            ));
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
