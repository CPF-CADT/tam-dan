package com.tamdan.repository;

import com.tamdan.entities.SafetyPolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyPolicyRepository extends JpaRepository<SafetyPolicy, Long> {
    Page<SafetyPolicy> findAllByChildProfileIdAndChildProfileFamilyId(Long childId, Long familyId, Pageable pageable);
    List<SafetyPolicy> findAllByChildProfileIdAndChildProfileFamilyIdAndStateNot(Long childId, Long familyId, SafetyPolicy.State state);
    Optional<SafetyPolicy> findByIdAndChildProfileIdAndChildProfileFamilyId(Long id, Long childId, Long familyId);
}
