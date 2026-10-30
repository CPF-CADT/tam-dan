package com.tamdan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tamdan.IntegrationTest;
import com.tamdan.entities.ChildProfile;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.ConsentRecordRepository;
import com.tamdan.repository.DeviceCredentialRepository;
import com.tamdan.repository.FamilyMemberRepository;
import com.tamdan.repository.PolicyDeliveryRepository;
import com.tamdan.repository.SafetyAlertRepository;
import com.tamdan.repository.SafetyEventRepository;
import com.tamdan.repository.SafetyPolicyRepository;
import com.tamdan.repository.SecurityAuditEventRepository;
import com.tamdan.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@IntegrationTest
@SpringBootTest
@ActiveProfiles({ "dev", "testcontainers" })
@TestPropertySource(properties = "tamdan.demo-data.enabled=true")
class DemoDataInitializerIT {

    @Autowired private DemoDataInitializer initializer;
    @Autowired private UserRepository userRepository;
    @Autowired private FamilyMemberRepository familyMemberRepository;
    @Autowired private ChildProfileRepository childProfileRepository;
    @Autowired private ChildDeviceRepository childDeviceRepository;
    @Autowired private DeviceCredentialRepository deviceCredentialRepository;
    @Autowired private ConsentRecordRepository consentRecordRepository;
    @Autowired private SafetyEventRepository safetyEventRepository;
    @Autowired private SafetyAlertRepository safetyAlertRepository;
    @Autowired private SafetyPolicyRepository safetyPolicyRepository;
    @Autowired private PolicyDeliveryRepository policyDeliveryRepository;
    @Autowired private SecurityAuditEventRepository securityAuditEventRepository;

    @Test
    void rerunningFixtureDoesNotDuplicateRepresentativeData() throws Exception {
        initializer.run(new DefaultApplicationArguments());
        initializer.run(new DefaultApplicationArguments());

        var parent = userRepository.findOneByLogin(DemoDataInitializer.DEMO_LOGIN).orElseThrow();
        var family = familyMemberRepository.findByParentIdAndActiveTrue(parent.getId()).orElseThrow().getFamily();
        var children = childProfileRepository.findAllByFamilyIdAndActiveTrue(family.getId());
        assertThat(children).extracting(ChildProfile::getDisplayName).containsExactlyInAnyOrder("Mina", "Dara");

        Map<Long, Long> devicesByChild = children.stream().collect(java.util.stream.Collectors.toMap(ChildProfile::getId, child -> (long) childDeviceRepository.findAllByChildProfileId(child.getId()).size()));
        assertThat(devicesByChild.values()).containsExactlyInAnyOrder(1L, 1L);
        assertThat(childDeviceRepository.findAllByChildProfileId(children.getFirst().getId())).allMatch(device -> deviceCredentialRepository.findCurrentByDeviceId(device.getId()).isPresent());

        long eventCount = children.stream().flatMap(child -> childDeviceRepository.findAllByChildProfileId(child.getId()).stream())
            .mapToLong(device -> safetyEventRepository.findAllByChildDeviceIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanEqual(device.getId(), DemoDataInitializerTestTimes.FROM, DemoDataInitializerTestTimes.TO).size()).sum();
        assertThat(eventCount).isEqualTo(3);
        assertThat(safetyAlertRepository.findAllByChildProfileIdAndChildProfileFamilyId(children.getFirst().getId(), family.getId(), Pageable.unpaged()).getTotalElements()).isEqualTo(1);
        var policy = safetyPolicyRepository.findByChildProfileIdAndTarget(children.getFirst().getId(), "example-risk.test").orElseThrow();
        assertThat(policy.getState()).isEqualTo(com.tamdan.entities.SafetyPolicy.State.ACTIVE);
        assertThat(policyDeliveryRepository.findAllByPolicyIdAndPolicyChildProfileFamilyId(policy.getId(), family.getId())).hasSize(2).allMatch(delivery -> delivery.getAcknowledgedVersion() != null);
        assertThat(consentRecordRepository.findAllByChildProfileIdAndFamilyIdOrderByAcknowledgedAtDesc(children.getFirst().getId(), family.getId())).hasSize(1);
        assertThat(securityAuditEventRepository.existsByFamilyIdAndRequestId(family.getId(), "demo-fixture-seeded-v1")).isTrue();
    }

    private static final class DemoDataInitializerTestTimes {
        private static final java.time.Instant FROM = java.time.Instant.parse("2026-01-15T00:00:00Z");
        private static final java.time.Instant TO = java.time.Instant.parse("2026-01-16T00:00:00Z");
    }
}
