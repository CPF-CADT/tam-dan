package com.tamdan.repository;

import com.tamdan.entities.Family;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByIdAndActiveTrue(Long id);

    boolean existsByIdAndActiveTrue(Long id);
}
