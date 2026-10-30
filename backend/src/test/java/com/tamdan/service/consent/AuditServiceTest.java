package com.tamdan.service.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.tamdan.dto.request.AuditCommand;
import com.tamdan.entities.SecurityAuditEvent;
import com.tamdan.repository.FamilyMemberRepository;
import com.tamdan.repository.SecurityAuditEventRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

class AuditServiceTest {

    @Test
    void hashesNetworkIdentityAndFiltersSensitiveMetadata() throws Exception {
        SecurityAuditEventRepository repository = mock(SecurityAuditEventRepository.class);
        FamilyMemberRepository members = mock(FamilyMemberRepository.class);
        ObjectMapper mapper = new ObjectMapper();
        AuditService service = new AuditService(repository, members, mapper);

        service.record(new AuditCommand("PARENT", 7L, 9L, "login", "PARENT", 7L, "SUCCESS", null, "127.0.0.1", "browser", Map.of("source", "password", "password", "secret", "refreshToken", "token")));

        ArgumentCaptor<SecurityAuditEvent> captor = ArgumentCaptor.forClass(SecurityAuditEvent.class);
        verify(repository).save(captor.capture());
        SecurityAuditEvent event = captor.getValue();
        assertThat(event.getIpAddressHash()).hasSize(64).doesNotContain("127.0.0.1");
        assertThat(event.getUserAgentHash()).hasSize(64).doesNotContain("browser");
        assertThat(event.getSafeMetadataJson()).doesNotContain("source").doesNotContain("secret").doesNotContain("token").doesNotContain("password");
    }

    @Test
    void recordsExplicitFailureResultWithoutRequestSecrets() {
        SecurityAuditEventRepository repository = mock(SecurityAuditEventRepository.class);
        AuditService service = new AuditService(repository, mock(FamilyMemberRepository.class), new ObjectMapper());

        service.recordFailure(new AuditCommand("DEVICE", null, null, "pairing-submission", "PAIRING_CODE", null, "FAILURE", null, null, null, Map.of()));

        ArgumentCaptor<SecurityAuditEvent> captor = ArgumentCaptor.forClass(SecurityAuditEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getResult()).isEqualTo("FAILURE");
        assertThat(captor.getValue().getSafeMetadataJson()).isEqualTo("{}");
    }

    @Test
    void supportsEveryApprovedSecurityAction() {
        SecurityAuditEventRepository repository = mock(SecurityAuditEventRepository.class);
        AuditService service = new AuditService(repository, mock(FamilyMemberRepository.class), new ObjectMapper());
        String[] actions = {
            "registration", "activation", "login", "logout", "logout-all", "recovery-request", "recovery-completion",
            "pairing-code-creation", "pairing-submission", "pairing-completion", "credential-rotation", "device-revocation",
            "consent-changes", "child-deactivation"
        };

        for (String action : actions) service.record(new AuditCommand("PARENT", null, null, action, "RESOURCE", null, "SUCCESS", null, null, null, Map.of()));

        verify(repository, times(actions.length)).save(org.mockito.ArgumentMatchers.any(SecurityAuditEvent.class));
    }
}
