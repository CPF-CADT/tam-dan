package com.tamdan.repository;

import com.tamdan.entities.PairingCode;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PairingCodeRepository extends JpaRepository<PairingCode, Long> {

    @Query("select pairing from PairingCode pairing where pairing.childProfile.id = :childId and pairing.usedAt is null and pairing.expiresAt > :now")
    Optional<PairingCode> findActiveForChild(@Param("childId") Long childId, @Param("now") Instant now);

    @Query("select pairing from PairingCode pairing where pairing.codeHash = :codeHash and pairing.usedAt is null and pairing.expiresAt > :now")
    Optional<PairingCode> findUsableByCodeHash(@Param("codeHash") String codeHash, @Param("now") Instant now);

    Optional<PairingCode> findByCodeHash(String codeHash);

    @Modifying
    @Query("update PairingCode pairing set pairing.usedAt = :usedAt where pairing.id = :id and pairing.usedAt is null")
    int markUsed(@Param("id") Long id, @Param("usedAt") Instant usedAt);

    @Modifying
    @Query("update PairingCode pairing set pairing.failedAttempts = pairing.failedAttempts + 1 where pairing.id = :id")
    int incrementFailedAttempts(@Param("id") Long id);
}
