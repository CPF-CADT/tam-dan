package com.tamdan.service.safety;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tamdan.dto.SafetyDto.ActivityBatchRequest;
import com.tamdan.dto.SafetyDto.EventRequest;
import com.tamdan.entities.SafetyEvent;
import com.tamdan.repository.SafetyEventRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import com.tamdan.service.device.ChildDeviceRepositoryFacade;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityIngestionServiceTest {
    @Mock
    private SafetyEventRepository eventRepository;
    @Mock
    private ChildDeviceRepositoryFacade deviceFacade;
    @Mock
    private AlertService alertService;

    @Test
    void duplicateEventIsIdempotent() {
        ActivityIngestionService service = new ActivityIngestionService(eventRepository, deviceFacade, new ObjectMapper(), alertService);
        AuthenticatedDeviceContext device = new AuthenticatedDeviceContext(9L, 8L, 7L);
        EventRequest event = new EventRequest("stable-event", Instant.now(), null);
        when(eventRepository.findByChildDeviceIdAndEventId(9L, "stable-event")).thenReturn(Optional.of(org.mockito.Mockito.mock(SafetyEvent.class)));

        var result = service.ingest(device, new ActivityBatchRequest("1.2", List.of(event)), SafetyEvent.Kind.ACTIVITY);

        assertThat(result.accepted()).isZero();
        assertThat(result.duplicate()).isEqualTo(1);
        verify(eventRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
}
