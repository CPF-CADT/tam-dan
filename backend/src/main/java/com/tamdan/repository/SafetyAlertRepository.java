package com.tamdan.repository;

import com.tamdan.entities.SafetyAlert;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyAlertRepository extends JpaRepository<SafetyAlert, Long> {
    Page<SafetyAlert> findAllByChildProfileIdAndChildProfileFamilyId(Long childId, Long familyId, Pageable pageable);
    Optional<SafetyAlert> findByIdAndChildProfileFamilyId(Long id, Long familyId);
    Optional<SafetyAlert> findByChildProfileIdAndDedupeKey(Long childId, String dedupeKey);
}
