package com.tamdan.service;

import com.tamdan.dto.request.DeviceHealthUpdateRequest;
import com.tamdan.dto.response.DeviceHealthResponse;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.DeviceHealth;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.DeviceHealthRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceHealthService {

    private final ChildDeviceRepository childDeviceRepository;
    private final DeviceHealthRepository deviceHealthRepository;
    private final FamilyService familyService;

    @Transactional
    public void update(Long deviceId, DeviceHealthUpdateRequest request) {
        ChildDevice device = requireActiveDevice(deviceId);
        DeviceHealth health = deviceHealthRepository.findByChildDeviceId(deviceId).orElseGet(() ->
            DeviceHealth.create(device, request.schemaVersion().trim(), request.reportedAt(), request.monitoringState().name(), request.syncState().name(), request.policyVersion(), request.queuedEventCount(), Instant.now())
        );
        if (health.getId() == null) {
            deviceHealthRepository.save(health);
        } else {
            health.update(request.schemaVersion().trim(), request.reportedAt(), request.monitoringState().name(), request.syncState().name(), request.policyVersion(), request.queuedEventCount(), Instant.now());
        }
    }

    @Transactional(readOnly = true)
    public DeviceHealthResponse get(Long parentId, Long childId, Long deviceId) {
        var family = familyService.requireFamilyForParent(parentId);
        return deviceHealthRepository.findByChildDeviceIdAndChildDeviceChildProfileIdAndChildDeviceChildProfileFamilyId(deviceId, childId, family.getId())
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Device health was not found"));
    }

    private ChildDevice requireActiveDevice(Long deviceId) {
        ChildDevice device = childDeviceRepository.findById(deviceId).orElseThrow(() -> new ResourceNotFoundException("Device was not found"));
        if (!Boolean.TRUE.equals(device.getActive()) || device.getRevokedAt() != null || !Boolean.TRUE.equals(device.getChildProfile().getActive())) {
            throw new ResourceNotFoundException("Active device was not found");
        }
        return device;
    }

    private DeviceHealthResponse toResponse(DeviceHealth health) {
        return new DeviceHealthResponse(
            health.getChildDevice().getId(), health.getChildDevice().getChildProfile().getId(), health.getSchemaVersion(), health.getReportedAt(),
            health.getMonitoringState(), health.getSyncState(), health.getPolicyVersion(), health.getQueuedEventCount(), health.getObservedAt()
        );
    }
}
