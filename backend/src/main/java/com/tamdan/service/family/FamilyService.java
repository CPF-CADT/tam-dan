package com.tamdan.service.family;

import com.tamdan.dto.response.FamilyResponse;
import com.tamdan.entities.Family;
import com.tamdan.entities.FamilyMember;
import com.tamdan.entities.User;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.exception.ConflictException;
import com.tamdan.repository.FamilyMemberRepository;
import com.tamdan.repository.FamilyRepository;
import com.tamdan.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public FamilyResponse createForParent(Long parentId) {
        User parent = userRepository.findById(parentId).orElseThrow(() -> new ResourceNotFoundException("Parent was not found"));
        var existingMembership = familyMemberRepository.findByParentIdAndActiveTrue(parentId);
        Family existingFamily = existingMembership.map(FamilyMember::getFamily).orElse(null);
        if (existingFamily != null) {
            if (!Boolean.TRUE.equals(existingFamily.getActive())) throw new ConflictException("Parent has an inactive active membership");
            return toResponse(existingFamily);
        }
        Instant now = Instant.now();
        Family family = familyRepository.saveAndFlush(Family.create(now));
        familyMemberRepository.save(FamilyMember.owner(family, parent, now));
        return toResponse(family);
    }

    @Transactional(readOnly = true)
    public Family requireFamilyForParent(Long parentId) {
        FamilyMember membership = familyMemberRepository.findByParentIdAndActiveTrue(parentId)
            .orElseThrow(() -> new ResourceNotFoundException("Active family membership was not found"));
        Family family = membership.getFamily();
        if (!Boolean.TRUE.equals(family.getActive())) throw new ResourceNotFoundException("Active family was not found");
        return familyRepository.findByIdAndActiveTrue(family.getId()).orElseThrow(() -> new ResourceNotFoundException("Active family was not found"));
    }

    @Transactional(readOnly = true)
    public FamilyResponse getCurrent(Long parentId) {
        return toResponse(requireFamilyForParent(parentId));
    }

    private FamilyResponse toResponse(Family family) {
        return new FamilyResponse(family.getId(), family.getActive(), family.getCreatedAt());
    }
}
