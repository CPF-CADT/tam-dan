package com.tamdan.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.ConsentRecord;
import com.tamdan.entities.Family;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.ConsentRecordRepository;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

    @Mock
    private FamilyService familyService;

    @Mock
    private ChildProfileRepository childProfileRepository;

    @Mock
    private ChildDeviceRepository childDeviceRepository;

    @Mock
    private ConsentRecordRepository consentRecordRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthenticatedDeviceIdResolver authenticatedDeviceIdResolver;

    @Mock
    private ChildDevice device;

    @Mock
    private ChildProfile child;

    @Mock
    private Family family;

    @Mock
    private ConsentRecord record;

    @InjectMocks
    private ConsentService service;

    @Test
    void preservesConfirmedAtWhenDeviceConfirmsDisclosure() {
        Instant confirmedAt = Instant.parse("2026-10-30T03:04:05Z");
        when(childDeviceRepository.findById(7L)).thenReturn(Optional.of(device));
        when(device.getActive()).thenReturn(true);
        when(device.getRevokedAt()).thenReturn(null);
        when(device.getChildProfile()).thenReturn(child);
        when(child.getActive()).thenReturn(true);
        when(child.getId()).thenReturn(8L);
        when(child.getFamily()).thenReturn(family);
        when(family.getId()).thenReturn(9L);
        when(consentRecordRepository.findFirstByChildProfileIdAndFamilyIdOrderByAcknowledgedAtDesc(8L, 9L)).thenReturn(Optional.of(record));
        when(record.getDocumentVersion()).thenReturn("v1");

        service.confirmDisclosure(7L, "v1", confirmedAt);

        verify(record).confirmByDevice(eq(confirmedAt));
    }
}
