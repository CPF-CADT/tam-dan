package com.tamdan.service.device;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tamdan.dto.request.DeviceHealthUpdateRequest;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.DeviceHealth;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.DeviceHealthRepository;
import com.tamdan.service.family.FamilyService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceHealthServiceTest {

    @Mock
    private ChildDeviceRepository childDeviceRepository;

    @Mock
    private DeviceHealthRepository deviceHealthRepository;

    @Mock
    private FamilyService familyService;

    @Mock
    private ChildDevice device;

    @Mock
    private ChildProfile child;

    @Mock
    private DeviceHealth health;

    @InjectMocks
    private DeviceHealthService service;

    @Test
    void rejectsHealthFromInactiveDevice() {
        when(childDeviceRepository.findById(7L)).thenReturn(Optional.of(device));
        when(device.getActive()).thenReturn(false);

        assertThatThrownBy(() -> service.update(7L, request())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createsHealthSnapshotForActiveDevice() {
        when(childDeviceRepository.findById(7L)).thenReturn(Optional.of(device));
        when(device.getActive()).thenReturn(true);
        when(device.getRevokedAt()).thenReturn(null);
        when(device.getChildProfile()).thenReturn(child);
        when(child.getActive()).thenReturn(true);
        when(deviceHealthRepository.findByChildDeviceId(7L)).thenReturn(Optional.empty());

        service.update(7L, request());

        verify(deviceHealthRepository).save(any(DeviceHealth.class));
    }

    @Test
    void updatesExistingHealthSnapshot() {
        when(childDeviceRepository.findById(7L)).thenReturn(Optional.of(device));
        when(device.getActive()).thenReturn(true);
        when(device.getRevokedAt()).thenReturn(null);
        when(device.getChildProfile()).thenReturn(child);
        when(child.getActive()).thenReturn(true);
        when(deviceHealthRepository.findByChildDeviceId(7L)).thenReturn(Optional.of(health));
        when(health.getId()).thenReturn(8L);

        service.update(7L, request());

        verify(health).update(eq("1"), eq(request().reportedAt()), eq("ACTIVE"), eq("CURRENT"), eq("p1"), eq(2), any(Instant.class));
    }

    private DeviceHealthUpdateRequest request() {
        return new DeviceHealthUpdateRequest("1", Instant.parse("2026-10-29T10:00:00Z"), DeviceHealthUpdateRequest.MonitoringState.ACTIVE,
            DeviceHealthUpdateRequest.SyncState.CURRENT, "p1", 2);
    }
}
