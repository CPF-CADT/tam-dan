package com.tamdan.repository;

import com.tamdan.entities.DeviceHealth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceHealthRepository extends JpaRepository<DeviceHealth, Long> {
    Optional<DeviceHealth> findByChildDeviceId(Long deviceId);

    Optional<DeviceHealth> findByChildDeviceIdAndChildDeviceChildProfileIdAndChildDeviceChildProfileFamilyId(Long deviceId, Long childId, Long familyId);
}
