package com.tamdan.service.reporting;

import com.tamdan.service.family.FamilyService;
import com.tamdan.dto.SafetyDto.SourceDeviceCoverageResponse;
import com.tamdan.dto.SafetyDto.UsageReportResponse;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.SafetyEvent;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.SafetyEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsageReportService {
    private final FamilyService familyService;
    private final ChildProfileRepository childProfileRepository;
    private final ChildDeviceRepository childDeviceRepository;
    private final SafetyEventRepository safetyEventRepository;

    @Transactional(readOnly = true)
    public UsageReportResponse getReport(Long parentId, Long childId, Instant from, Instant to) {
        if (from == null || to == null || !from.isBefore(to) || Duration.between(from, to).toDays() > 31) {
            throw new BadRequestException("Report range must be ordered and no longer than 31 days");
        }
        Long familyId = familyService.requireFamilyForParent(parentId).getId();
        var child = childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, familyId).orElseThrow(() -> new ResourceNotFoundException("Child was not found"));
        List<ChildDevice> devices = childDeviceRepository.findAllByChildProfileId(childId).stream().filter(d -> Boolean.TRUE.equals(d.getActive()) && d.getRevokedAt() == null).toList();
        List<SafetyEvent> events = safetyEventRepository.findAllByChildProfileIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanEqual(childId, from, to);
        Instant generatedAt = Instant.now();
        Instant dataThrough = events.stream().map(event -> event.getOccurredAt()).max(Comparator.naturalOrder()).orElse(from);
        List<SourceDeviceCoverageResponse> coverage = new ArrayList<>();
        boolean complete = !devices.isEmpty();
        for (ChildDevice device : devices) {
            Instant observed = events.stream().filter(e -> e.getChildDevice().getId().equals(device.getId())).map(event -> event.getOccurredAt()).max(Comparator.naturalOrder()).orElse(null);
            String status = observed == null ? "MISSING" : observed.isBefore(to.minus(24, java.time.temporal.ChronoUnit.HOURS)) ? "DELAYED" : "COMPLETE";
            if (!"COMPLETE".equals(status)) complete = false;
            coverage.add(new SourceDeviceCoverageResponse(device.getId(), to, observed, status, observed == null ? "No event received" : null));
        }
        String freshness = events.isEmpty() ? "UNKNOWN" : dataThrough.isBefore(generatedAt.minus(24, java.time.temporal.ChronoUnit.HOURS)) ? "STALE" : dataThrough.isBefore(to) ? "DELAYED" : "FRESH";
        String completeness = devices.isEmpty() ? "UNKNOWN" : complete ? "COMPLETE" : events.isEmpty() ? "INSUFFICIENT" : "PARTIAL";
        String risk = "INSUFFICIENT".equals(completeness) || "UNKNOWN".equals(completeness) || "UNKNOWN".equals(freshness) ? "UNKNOWN" : events.stream().anyMatch(e -> e.getKind() == SafetyEvent.Kind.DOMAIN && e.getPayloadJson().contains("\"risk\":\"RISK\"")) ? "RISK" : "SAFE";
        String reason = "UNKNOWN".equals(risk) ? "Evidence is missing, stale, or incomplete" : null;
        return new UsageReportResponse(child.getId(), from, to, generatedAt, dataThrough, freshness, completeness, reason, coverage, risk);
    }
}
