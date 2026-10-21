package com.tamdan.repository;

import com.tamdan.entities.ParentSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParentSessionRepository extends JpaRepository<ParentSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParentSession> findByRefreshTokenHashAndRevokedAtIsNull(String hash);

    Optional<ParentSession> findByRefreshTokenHash(String hash);

    List<ParentSession> findAllByParentIdAndRevokedAtIsNull(Long parentId);

    @Modifying
    @Query("update ParentSession session set session.revokedAt = :revokedAt where session.parent.id = :parentId and session.revokedAt is null")
    int revokeAllActiveSessions(@Param("parentId") Long parentId, @Param("revokedAt") Instant revokedAt);
}
