package com.tamdan.service.safety;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tamdan.IntegrationTest;
import com.tamdan.config.Constants;
import com.tamdan.dto.SafetyDto.ActivityBatchRequest;
import com.tamdan.dto.SafetyDto.AlertReviewRequest;
import com.tamdan.dto.SafetyDto.EventRequest;
import com.tamdan.dto.SafetyDto.PolicyAcknowledgementRequest;
import com.tamdan.dto.SafetyDto.PolicyRequest;
import com.tamdan.dto.SafetyDto.UsageReportResponse;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.Family;
import com.tamdan.entities.PolicyDelivery;
import com.tamdan.entities.User;
import com.tamdan.exception.ConflictException;
import com.tamdan.exception.ForbiddenException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.SafetyAlertRepository;
import com.tamdan.repository.UserRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import com.tamdan.service.family.FamilyService;
import com.tamdan.service.reporting.UsageReportService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@IntegrationTest
@Transactional
class SafetyPolicyFlowIT {

    @Autowired private FamilyService familyService;
    @Autowired private ChildProfileRepository childProfileRepository;
    @Autowired private ChildDeviceRepository childDeviceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ActivityIngestionService ingestionService;
    @Autowired private UsageReportService usageReportService;
    @Autowired private AlertService alertService;
    @Autowired private PolicyService policyService;
    @Autowired private PolicyAcknowledgementService acknowledgementService;
    @Autowired private SafetyAlertRepository alertRepository;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void ingestsReportsAlertsAndActivatesAcknowledgedPolicyAsDtos() {
        Fixture fixture = fixture("safety-flow");
        Instant occurredAt = Instant.now().minusSeconds(30);
        ActivityBatchRequest events = new ActivityBatchRequest(
            "1.2",
            List.of(
                new EventRequest("activity-1", occurredAt, objectMapper.createObjectNode().put("app", "school")),
                new EventRequest("domain-1", occurredAt, objectMapper.createObjectNode().put("risk", "RISK"))
            )
        );

        var activityResult = ingestionService.ingest(
            fixture.deviceContext(),
            new ActivityBatchRequest("1.2", List.of(events.events().get(0))),
            com.tamdan.entities.SafetyEvent.Kind.ACTIVITY
        );
        var duplicateResult = ingestionService.ingest(
            fixture.deviceContext(),
            new ActivityBatchRequest("1.2", List.of(events.events().get(0))),
            com.tamdan.entities.SafetyEvent.Kind.ACTIVITY
        );
        var domainResult = ingestionService.ingest(
            fixture.deviceContext(),
            new ActivityBatchRequest("1.2", List.of(events.events().get(1))),
            com.tamdan.entities.SafetyEvent.Kind.DOMAIN
        );

        assertThat(activityResult.accepted()).isEqualTo(1);
        assertThat(duplicateResult.duplicate()).isEqualTo(1);
        assertThat(domainResult.duplicate()).isEqualTo(1);

        Instant from = occurredAt.minusSeconds(60);
        Instant to = occurredAt.plusSeconds(3600);
        UsageReportResponse report = usageReportService.getReport(fixture.parent().getId(), fixture.child().getId(), from, to);
        assertThat(report.childId()).isEqualTo(fixture.child().getId());
        assertThat(report.risk()).isEqualTo("RISK");
        assertThat(report.sourceDeviceCoverage()).singleElement().satisfies(coverage -> {
            assertThat(coverage.deviceId()).isEqualTo(fixture.device().getId());
            assertThat(coverage.status()).isEqualTo("COMPLETE");
        });

        var alerts = alertService.list(fixture.parent().getId(), fixture.child().getId(), 0, 20);
        assertThat(alerts.items()).singleElement().satisfies(alert -> {
            assertThat(alert.childId()).isEqualTo(fixture.child().getId());
            assertThat(alert.state()).isEqualTo("OPEN");
            assertThat(alert.severity()).isEqualTo("HIGH");
        });
        Long alertId = alerts.items().get(0).id();
        var reviewed = alertService.review(fixture.parent().getId(), alertId, new AlertReviewRequest("REVIEWED", "Reviewed by parent"));
        assertThat(reviewed.state()).isEqualTo("REVIEWED");
        assertThat(alertRepository.findById(alertId).orElseThrow().getChildProfile().getId()).isEqualTo(fixture.child().getId());

        var createdPolicy = policyService.create(
            fixture.parent().getId(),
            fixture.child().getId(),
            new PolicyRequest(com.tamdan.entities.SafetyPolicy.Type.APPLICATION, "com.example.school")
        );
        assertThat(createdPolicy.childId()).isEqualTo(fixture.child().getId());
        assertThat(createdPolicy.state()).isEqualTo(com.tamdan.entities.SafetyPolicy.State.PENDING_DELIVERY);

        var downloaded = policyService.download(fixture.deviceContext());
        assertThat(downloaded).singleElement().satisfies(policy -> assertThat(policy.id()).isEqualTo(createdPolicy.id()));
        var pendingDelivery = policyService.delivery(fixture.parent().getId(), fixture.child().getId(), createdPolicy.id());
        assertThat(pendingDelivery.state()).isEqualTo("PARTIAL");
        assertThat(pendingDelivery.devices()).singleElement().satisfies(delivery -> assertThat(delivery.state()).isEqualTo(PolicyDelivery.State.PENDING));

        var acknowledgement = acknowledgementService.acknowledge(
            fixture.deviceContext(),
            createdPolicy.id(),
            new PolicyAcknowledgementRequest(createdPolicy.version(), PolicyDelivery.State.ACTIVE, Instant.now(), null)
        );
        assertThat(acknowledgement.deviceId()).isEqualTo(fixture.device().getId());
        assertThat(acknowledgement.state()).isEqualTo(PolicyDelivery.State.ACTIVE);
        assertThat(policyService.delivery(fixture.parent().getId(), fixture.child().getId(), createdPolicy.id()).state()).isEqualTo("ACKNOWLEDGED");
        assertThat(policyService.list(fixture.parent().getId(), fixture.child().getId(), 0, 20))
            .singleElement()
            .satisfies(policy -> assertThat(policy.state()).isEqualTo(com.tamdan.entities.SafetyPolicy.State.ACTIVE));

        var updatedPolicy = policyService.update(
            fixture.parent().getId(),
            fixture.child().getId(),
            createdPolicy.id(),
            new PolicyRequest(com.tamdan.entities.SafetyPolicy.Type.APPLICATION, "com.example.school.updated")
        );
        assertThat(updatedPolicy.version()).isEqualTo(2L);
        assertThat(updatedPolicy.state()).isEqualTo(com.tamdan.entities.SafetyPolicy.State.PENDING_DELIVERY);
        assertThatThrownBy(() -> acknowledgementService.acknowledge(
                fixture.deviceContext(),
                updatedPolicy.id(),
                new PolicyAcknowledgementRequest(1L, PolicyDelivery.State.ACTIVE, Instant.now(), null)
            ))
            .isInstanceOf(ConflictException.class);
        acknowledgementService.acknowledge(
            fixture.deviceContext(),
            updatedPolicy.id(),
            new PolicyAcknowledgementRequest(2L, PolicyDelivery.State.ACTIVE, Instant.now(), null)
        );
        var pausedPolicy = policyService.pause(fixture.parent().getId(), fixture.child().getId(), updatedPolicy.id());
        assertThat(pausedPolicy.state()).isEqualTo(com.tamdan.entities.SafetyPolicy.State.PAUSED);
        assertThat(policyService.download(fixture.deviceContext())).isEmpty();
    }

    @Test
    void parentAndDeviceCannotCrossFamilyBoundaries() {
        Fixture owner = fixture("safety-owner");
        Fixture other = fixture("safety-other");
        var policy = policyService.create(
            owner.parent().getId(),
            owner.child().getId(),
            new PolicyRequest(com.tamdan.entities.SafetyPolicy.Type.DOMAIN, "example.com")
        );

        assertThatThrownBy(() -> usageReportService.getReport(other.parent().getId(), owner.child().getId(), Instant.now().minusSeconds(60), Instant.now()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> policyService.list(other.parent().getId(), owner.child().getId(), 0, 20))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> policyService.delivery(other.parent().getId(), owner.child().getId(), policy.id()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> ingestionService.ingest(
                new AuthenticatedDeviceContext(owner.device().getId(), other.child().getId(), other.family().getId()),
                new ActivityBatchRequest("1.2", List.of(new EventRequest("cross-family", Instant.now(), null))),
                com.tamdan.entities.SafetyEvent.Kind.ACTIVITY
            ))
            .isInstanceOf(ForbiddenException.class);
    }

    private Fixture fixture(String prefix) {
        User parent = userRepository.saveAndFlush(
            User.builder()
                .login(prefix + "-parent")
                .email(prefix + "-parent@example.com")
                .password("x".repeat(60))
                .activated(true)
                .langKey(Constants.DEFAULT_LANGUAGE)
                .build()
        );
        familyService.createForParent(parent.getId());
        Family family = familyService.requireFamilyForParent(parent.getId());
        ChildProfile child = childProfileRepository.saveAndFlush(ChildProfile.create(family, prefix + " child", Instant.now()));
        ChildDevice device = childDeviceRepository.saveAndFlush(
            ChildDevice.create(child, prefix + "-device-hash", "android", "1.0", prefix + " device", "{}", Instant.now())
        );
        return new Fixture(parent, family, child, device);
    }

    private record Fixture(User parent, Family family, ChildProfile child, ChildDevice device) {
        AuthenticatedDeviceContext deviceContext() {
            return new AuthenticatedDeviceContext(device.getId(), child.getId(), family.getId());
        }
    }
}
