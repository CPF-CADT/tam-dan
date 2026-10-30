package com.tamdan.service.safety;

import com.tamdan.service.family.FamilyService;
import com.tamdan.dto.SafetyDto.AlertPageResponse;
import com.tamdan.dto.SafetyDto.AlertResponse;
import com.tamdan.dto.SafetyDto.AlertReviewRequest;
import com.tamdan.entities.SafetyAlert;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.SafetyAlertRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final FamilyService familyService;
    private final ChildProfileRepository childProfileRepository;
    private final SafetyAlertRepository alertRepository;

    @Transactional
    public void upsertRiskAlert(Long childId, String eventId, Instant now) {
        var child = childProfileRepository.findById(childId).orElseThrow(() -> new ResourceNotFoundException("Child was not found"));
        alertRepository.findByChildProfileIdAndDedupeKey(childId, eventId).orElseGet(() -> alertRepository.save(SafetyAlert.create(child, eventId, SafetyAlert.Severity.HIGH, "Risk detected", "A domain observation requires review", now)));
    }

    @Transactional(readOnly = true)
    public AlertPageResponse list(Long parentId, Long childId, int page, int pageSize) {
        Long familyId = familyService.requireFamilyForParent(parentId).getId();
        var result = alertRepository.findAllByChildProfileIdAndChildProfileFamilyId(childId, familyId, PageRequest.of(page, pageSize));
        return new AlertPageResponse(result.getContent().stream().map(this::toResponse).toList(), page, pageSize, result.getTotalElements());
    }

    @Transactional
    public AlertResponse review(Long parentId, Long alertId, AlertReviewRequest request) {
        Long familyId = familyService.requireFamilyForParent(parentId).getId();
        SafetyAlert alert = alertRepository.findByIdAndChildProfileFamilyId(alertId, familyId).orElseThrow(() -> new ResourceNotFoundException("Alert was not found"));
        alert.review(request.note(), Instant.now());
        return toResponse(alert);
    }

    @Transactional
    public AlertResponse dismiss(Long parentId, Long alertId) {
        Long familyId = familyService.requireFamilyForParent(parentId).getId();
        SafetyAlert alert = alertRepository.findByIdAndChildProfileFamilyId(alertId, familyId).orElseThrow(() -> new ResourceNotFoundException("Alert was not found"));
        alert.dismiss(Instant.now());
        return toResponse(alert);
    }

    private AlertResponse toResponse(SafetyAlert alert) {
        return new AlertResponse(alert.getId(), alert.getChildProfile().getId(), alert.getState().name(), alert.getSeverity().name(), alert.getTitle(), alert.getDetail(), alert.getCreatedAt(), alert.getReviewedAt());
    }
}
