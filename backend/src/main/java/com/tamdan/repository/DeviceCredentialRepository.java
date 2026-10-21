package com.tamdan.repository;

import com.tamdan.entities.DeviceCredential;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface DeviceCredentialRepository extends JpaRepository<DeviceCredential, Long> {
    @Query("select credential from DeviceCredential credential where credential.credentialHash = :hash and credential.issuedAt <= :now and credential.expiresAt > :now and credential.revokedAt is null and credential.childDevice.active = true and credential.childDevice.revokedAt is null and credential.childDevice.childProfile.active = true and credential.childDevice.childProfile.family.active = true")
    Optional<DeviceCredential> findActiveByCredentialHash(@Param("hash") String hash, @Param("now") Instant now);

    @Query("select credential from DeviceCredential credential where credential.childDevice.id = :deviceId order by credential.issuedAt desc")
    Optional<DeviceCredential> findCurrentByDeviceId(@Param("deviceId") Long deviceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DeviceCredential> findFirstByChildDeviceIdAndRevokedAtIsNullOrderByIssuedAtDesc(Long deviceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DeviceCredential credential set credential.revokedAt = :revokedAt where credential.childDevice.id = :deviceId and credential.revokedAt is null")
    int revokeByDeviceId(@Param("deviceId") Long deviceId, @Param("revokedAt") Instant revokedAt);
}
