package com.tamdan.service;

import com.tamdan.dto.response.DeviceResponse;
import com.tamdan.dto.response.DeviceCredentialResponse;
import com.tamdan.dto.request.RenameDeviceRequest;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.Family;
import com.tamdan.exception.BadRequestException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.security.DeviceRevocationEvent;
import com.tamdan.security.DeviceRevocationListener;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final FamilyService familyService;
    private final ChildProfileRepository childProfileRepository;
    private final ChildDeviceRepository childDeviceRepository;
    private final DeviceCredentialService deviceCredentialService;
    private final AuditService auditService;
    private final List<DeviceRevocationListener> revocationListeners;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<DeviceResponse> list(Long parentId, Long childId) {
        Family family = familyService.requireFamilyForParent(parentId);
        childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        return childDeviceRepository.findAllByChildProfileId(childId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse get(Long parentId, Long deviceId) {
        Family family = familyService.requireFamilyForParent(parentId);
        return toResponse(childDeviceRepository.findByIdAndChildProfileFamilyId(deviceId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Device was not found")));
    }

    @Transactional
    public DeviceResponse rename(Long parentId, Long deviceId, String name) {
        if (name == null || name.isBlank()) throw new BadRequestException("Device name is required");
        Family family = familyService.requireFamilyForParent(parentId);
        ChildDevice device = childDeviceRepository.findByIdAndChildProfileFamilyId(deviceId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Device was not found"));
        device.rename(name.trim());
        return toResponse(device);
    }

    @Transactional
    public DeviceResponse rename(Long parentId, Long deviceId, RenameDeviceRequest request) {
        return rename(parentId, deviceId, request.name());
    }

    @Transactional
    public com.tamdan.dto.response.DeviceV1Response renameV1(Long parentId, Long deviceId, String displayName) {
        Family family = familyService.requireFamilyForParent(parentId);
        ChildDevice device = childDeviceRepository.findByIdAndChildProfileFamilyId(deviceId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Device was not found"));
        if (displayName == null || displayName.isBlank()) throw new BadRequestException("Device display name is required");
        device.rename(displayName.trim());
        return toV1Response(device);
    }

    @Transactional(readOnly = true)
    public void ensureParentOwnsDevice(Long parentId, Long childId, Long deviceId) {
        Family family = familyService.requireFamilyForParent(parentId);
        childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        childDeviceRepository.findByIdAndChildProfileIdAndChildProfileFamilyId(deviceId, childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Device was not found"));
    }

    @Transactional
    public void revokeDevice(Long parentId, Long deviceId) {
        Family family = familyService.requireFamilyForParent(parentId);
        ChildDevice device = childDeviceRepository.findByIdAndChildProfileFamilyId(deviceId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Device was not found"));
        if (!Boolean.TRUE.equals(device.getActive()) || device.getRevokedAt() != null) {
            deviceCredentialService.revokeAllCredentials(deviceId);
            return;
        }
        Instant revokedAt = Instant.now();
        device.revoke(revokedAt);
        deviceCredentialService.revokeAllCredentials(deviceId);
        if (auditService != null) auditService.record(new com.tamdan.dto.request.AuditCommand("PARENT", parentId, family.getId(), "device-revocation", "DEVICE", deviceId, "SUCCESS", null, null, null, java.util.Map.of()));
        if (revocationListeners != null) {
            DeviceRevocationEvent event = new DeviceRevocationEvent(deviceId, device.getChildProfile().getId(), family.getId(), revokedAt, "parent-request");
            revocationListeners.forEach(listener -> listener.onDeviceRevoked(event));
        }
    }

    @Transactional
    public DeviceCredentialResponse rotateCredential(Long parentId, Long deviceId) {
        Family family = familyService.requireFamilyForParent(parentId);
        childDeviceRepository.findByIdAndChildProfileFamilyId(deviceId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Device was not found"));
        return deviceCredentialService.rotateCredential(deviceId);
    }

    @Transactional(readOnly = true)
    public List<com.tamdan.dto.response.DeviceV1Response> listV1(Long parentId, Long childId) {
        Family family = familyService.requireFamilyForParent(parentId);
        childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Active child profile was not found"));
        return childDeviceRepository.findAllByChildProfileId(childId).stream().map(this::toV1Response).toList();
    }

    public com.tamdan.dto.response.DeviceV1Response toV1Response(ChildDevice device) {
        Map<String, Object> capabilities;
        try {
            capabilities = objectMapper.readValue(device.getCapabilitiesJson(), Map.class);
        } catch (JacksonException | ClassCastException exception) {
            capabilities = Map.of();
        }
        String status = device.getRevokedAt() != null || !Boolean.TRUE.equals(device.getActive()) ? "REVOKED" : "ACTIVE";
        return new com.tamdan.dto.response.DeviceV1Response(
            device.getId(), device.getChildProfile().getId(), device.getDisplayName(), status, device.getPlatform(), capabilities
        );
    }

    @Transactional
    public void revokeCredential(Long parentId, Long deviceId) {
        Family family = familyService.requireFamilyForParent(parentId);
        childDeviceRepository.findByIdAndChildProfileFamilyId(deviceId, family.getId()).orElseThrow(() ->
            new ResourceNotFoundException("Device was not found"));
        deviceCredentialService.revokeAllCredentials(deviceId);
    }

    private DeviceResponse toResponse(ChildDevice device) {
        return new DeviceResponse(device.getId(), device.getDisplayName(), device.getPlatform(), device.getAppVersion(), device.getActive(), device.getLastSeenAt());
    }
}
