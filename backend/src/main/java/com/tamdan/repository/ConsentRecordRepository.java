package com.tamdan.repository;

import com.tamdan.entities.ConsentRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {
    List<ConsentRecord> findAllByChildProfileIdAndFamilyIdOrderByAcknowledgedAtDesc(Long childId, Long familyId);
}
