package com.tamdan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tamdan.dto.SafetyDto.UsageReportResponse;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.Family;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.SafetyEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UsageReportServiceTest {

    private static final Instant FROM = Instant.parse("2026-10-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-10-02T00:00:00Z");

    @Test
    void returnsContractSafeDataThroughAndUnknownSemanticsWhenNoEventsExist() {
        FamilyService familyService = mock(FamilyService.class);
        ChildProfileRepository childProfileRepository = mock(ChildProfileRepository.class);
        ChildDeviceRepository childDeviceRepository = mock(ChildDeviceRepository.class);
        SafetyEventRepository safetyEventRepository = mock(SafetyEventRepository.class);
        Family family = mock(Family.class);
        ChildProfile child = mock(ChildProfile.class);
        when(familyService.requireFamilyForParent(1L)).thenReturn(family);
        when(family.getId()).thenReturn(2L);
        when(childProfileRepository.findByIdAndFamilyIdAndActiveTrue(3L, 2L)).thenReturn(Optional.of(child));
        when(child.getId()).thenReturn(3L);
        when(childDeviceRepository.findAllByChildProfileId(3L)).thenReturn(List.of());
        when(safetyEventRepository.findAllByChildProfileIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanEqual(3L, FROM, TO)).thenReturn(List.of());

        UsageReportResponse report = new UsageReportService(familyService, childProfileRepository, childDeviceRepository, safetyEventRepository)
            .getReport(1L, 3L, FROM, TO);

        assertThat(report.dataThrough()).isEqualTo(FROM);
        assertThat(report.freshness()).isEqualTo("UNKNOWN");
        assertThat(report.completeness()).isEqualTo("UNKNOWN");
        assertThat(report.risk()).isEqualTo("UNKNOWN");
    }
}
