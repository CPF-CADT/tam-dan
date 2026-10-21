package com.tamdan.service;

import com.tamdan.dto.request.ConsentRequest;
import com.tamdan.dto.request.DevicePermissionRequest;
import com.tamdan.dto.response.ConsentResponse;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.ConsentRecord;
import com.tamdan.entities.Family;
import com.tamdan.entities.ChildDevice;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.ConsentRecordRepository;
import com.tamdan.security.AuthenticatedDeviceIdResolver;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final FamilyService familyService;
    private final ChildProfileRepository childProfileRepository;
    private final ChildDeviceRepository childDeviceRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final AuditService auditService;
    private final AuthenticatedDeviceIdResolver authenticatedDeviceIdResolver;

    @Transactional
    public ConsentResponse acknowledge(Long parentId, Long childId, ConsentRequest request) {
        Family family = familyService.requireFamilyForParent(parentId);
        ChildProfile child = childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        ConsentRecord record = consentRecordRepository.save(
            ConsentRecord.create(family, child, request.documentVersion().trim(), "PARENT", parentId, Instant.now(), request.permissionState().trim())
        );
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", parentId, family.getId(), "consent-changes", "CHILD", childId, "SUCCESS", null, null, null, java.util.Map.of()));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> history(Long parentId, Long childId) {
        Family family = familyService.requireFamilyForParent(parentId);
        childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        return consentRecordRepository.findAllByChildProfileIdAndFamilyIdOrderByAcknowledgedAtDesc(childId, family.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ConsentResponse recordDevicePermission(Long deviceId, DevicePermissionRequest request) {
        if (authenticatedDeviceIdResolver != null && !deviceId.equals(authenticatedDeviceIdResolver.requireAuthenticatedDeviceId())) {
            throw new com.tamdan.exception.ForbiddenException("Device is not authorized");
        }
        ChildDevice device = childDeviceRepository.findById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device was not found"));
        if (!Boolean.TRUE.equals(device.getActive()) || device.getRevokedAt() != null || !Boolean.TRUE.equals(device.getChildProfile().getActive())) {
            throw new ResourceNotFoundException("Active device was not found");
        }
        ConsentRecord record = consentRecordRepository.save(
            ConsentRecord.create(device.getChildProfile().getFamily(), device.getChildProfile(), "device-permission", "DEVICE", deviceId, Instant.now(), request.permissionState().trim())
        );
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("DEVICE", deviceId, device.getChildProfile().getFamily().getId(), "consent-changes", "CHILD", device.getChildProfile().getId(), "SUCCESS", null, null, null, java.util.Map.of()));
        return toResponse(record);
    }

    private ConsentResponse toResponse(ConsentRecord record) {
        return new ConsentResponse(record.getId(), record.getChildProfile().getId(), record.getDocumentVersion(), record.getActorType(), record.getAcknowledgedAt(), record.getPermissionState());
    }
}
