package com.tamdan.repository;

import com.tamdan.entities.FamilyMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    Optional<FamilyMember> findByParentIdAndActiveTrue(Long parentId);

    boolean existsByFamilyIdAndParentIdAndActiveTrue(Long familyId, Long parentId);

    boolean existsByFamilyIdAndActiveTrue(Long familyId);
}
