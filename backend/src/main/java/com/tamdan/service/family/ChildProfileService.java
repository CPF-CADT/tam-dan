package com.tamdan.service.family;

import com.tamdan.service.consent.AuditService;
import com.tamdan.dto.request.CreateChildRequest;
import com.tamdan.dto.request.UpdateChildRequest;
import com.tamdan.dto.response.ChildProfileResponse;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.Family;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildProfileRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChildProfileService {

    private final ChildProfileRepository childProfileRepository;
    private final FamilyService familyService;
    private final AuditService auditService;

    @Transactional
    public ChildProfileResponse create(Long parentId, CreateChildRequest request) {
        Family family = familyService.requireFamilyForParent(parentId);
        ChildProfile child = childProfileRepository.save(ChildProfile.create(family, request.displayName().trim(), Instant.now()));
        return toResponse(child);
    }

    @Transactional(readOnly = true)
    public List<ChildProfileResponse> list(Long parentId) {
        Family family = familyService.requireFamilyForParent(parentId);
        return childProfileRepository.findAllByFamilyIdAndActiveTrue(family.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ChildProfileResponse get(Long parentId, Long childId) {
        Family family = familyService.requireFamilyForParent(parentId);
        return toResponse(childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found")));
    }

    @Transactional
    public ChildProfileResponse update(Long parentId, Long childId, UpdateChildRequest request) {
        Family family = familyService.requireFamilyForParent(parentId);
        ChildProfile child = childProfileRepository.findByIdAndFamilyId(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Child profile was not found"));
        child.update(request.displayName().trim(), request.active());
        if (!request.active()) {
            if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", parentId, family.getId(), "child-deactivation", "CHILD", childId, "SUCCESS", null, null, null, java.util.Map.of()));
        }
        return toResponse(childProfileRepository.save(child));
    }

    @Transactional
    public ChildProfileResponse updateDisplayName(Long parentId, Long childId, String displayName) {
        Family family = familyService.requireFamilyForParent(parentId);
        ChildProfile child = childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        child.update(displayName.trim(), true);
        return toResponse(childProfileRepository.save(child));
    }

    @Transactional
    public void deactivate(Long parentId, Long childId) {
        Family family = familyService.requireFamilyForParent(parentId);
        ChildProfile child = childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        child.deactivate();
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", parentId, family.getId(), "child-deactivation", "CHILD", childId, "SUCCESS", null, null, null, java.util.Map.of()));
    }

    private ChildProfileResponse toResponse(ChildProfile child) {
        return new ChildProfileResponse(child.getId(), child.getDisplayName(), child.getActive(), child.getCreatedAt(), child.getUpdatedAt());
    }
}
