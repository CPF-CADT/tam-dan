package com.tamdan.repository;

import com.tamdan.entities.ChildProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildProfileRepository extends JpaRepository<ChildProfile, Long> {
    List<ChildProfile> findAllByFamilyIdAndActiveTrue(Long familyId);

    Optional<ChildProfile> findByIdAndFamilyIdAndActiveTrue(Long childId, Long familyId);

    Optional<ChildProfile> findByIdAndFamilyId(Long childId, Long familyId);
}
