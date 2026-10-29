package com.tamdan.repository;

import com.tamdan.entities.ChildDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildDeviceRepository extends JpaRepository<ChildDevice, Long> {
    List<ChildDevice> findAllByChildProfileId(Long childId);

    Optional<ChildDevice> findByIdAndChildProfileFamilyId(Long deviceId, Long familyId);

    Optional<ChildDevice> findByIdAndChildProfileIdAndChildProfileFamilyId(Long deviceId, Long childId, Long familyId);

    boolean existsByDeviceIdentifierHash(String deviceIdentifierHash);
}
