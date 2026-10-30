package com.tamdan.service.auth;

import com.tamdan.service.family.FamilyService;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.Family;
import com.tamdan.exception.ForbiddenException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.FamilyMemberRepository;
import com.tamdan.repository.FamilyRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import com.tamdan.security.AuthenticatedParentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdentityLookupServiceImpl implements IdentityLookupService {

    private final FamilyService familyService;
    private final ChildDeviceRepository childDeviceRepository;
    private final ChildProfileRepository childProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyRepository familyRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedParentContext resolveParent(Long parentId) {
        Family family = familyService.requireFamilyForParent(parentId);
        return new AuthenticatedParentContext(parentId, family.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedDeviceContext resolveDevice(Long deviceId) {
        ChildDevice device = childDeviceRepository.findById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device was not found"));
        if (!isActive(device)) throw new ForbiddenException("Device is not active");
        return new AuthenticatedDeviceContext(device.getId(), device.getChildProfile().getId(), device.getChildProfile().getFamily().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyChildOwnership(Long familyId, Long childId) {
        return familyId != null && childId != null && familyRepository.existsByIdAndActiveTrue(familyId) && familyMemberRepository.existsByFamilyIdAndActiveTrue(familyId) &&
            childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, familyId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyActiveDevice(Long deviceId) {
        return childDeviceRepository.findById(deviceId).filter(this::isActive).isPresent();
    }

    private boolean isActive(ChildDevice device) {
        return Boolean.TRUE.equals(device.getActive()) && device.getRevokedAt() == null &&
            Boolean.TRUE.equals(device.getChildProfile().getActive()) && Boolean.TRUE.equals(device.getChildProfile().getFamily().getActive());
    }
}
