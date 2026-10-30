package com.tamdan.repository;

import com.tamdan.entities.SecurityAuditEvent;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, Long> {
    boolean existsByFamilyIdAndRequestId(Long familyId, String requestId);

    @Query("select event from SecurityAuditEvent event where event.familyId = :familyId and (:action is null or event.action = :action) and (:resourceType is null or event.resourceType = :resourceType) and (:from is null or event.occurredAt >= :from) and (:to is null or event.occurredAt <= :to) order by event.occurredAt desc")
    Page<SecurityAuditEvent> search(@Param("familyId") Long familyId, @Param("action") String action, @Param("resourceType") String resourceType, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
