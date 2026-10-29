package com.tamdan.service;

import com.tamdan.entities.ChildDevice;
import com.tamdan.exception.ForbiddenException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChildDeviceRepositoryFacade {
    private final ChildDeviceRepository repository;

    @Transactional(readOnly = true)
    public ChildDevice requireEntity(AuthenticatedDeviceContext context) {
        ChildDevice device = repository.findById(context.deviceId()).orElseThrow(() -> new ResourceNotFoundException("Device was not found"));
        if (!device.getChildProfile().getId().equals(context.childProfileId()) || !device.getChildProfile().getFamily().getId().equals(context.familyId()) ||
            !Boolean.TRUE.equals(device.getActive()) || device.getRevokedAt() != null || !Boolean.TRUE.equals(device.getChildProfile().getActive())) {
            throw new ForbiddenException("Device does not own this resource");
        }
        return device;
    }

    public void requireActive(AuthenticatedDeviceContext context) { requireEntity(context); }
}
