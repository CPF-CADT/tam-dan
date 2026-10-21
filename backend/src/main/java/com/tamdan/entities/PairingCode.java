package com.tamdan.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pairing_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PairingCode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private ChildProfile childProfile;

    @Column(name = "code_hash", nullable = false, length = 64, unique = true)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @Column(name = "created_by_parent_id", nullable = false)
    private Long createdByParentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static PairingCode create(
        Family family,
        ChildProfile childProfile,
        String codeHash,
        Instant expiresAt,
        Long createdByParentId,
        Instant createdAt
    ) {
        return new PairingCode(null, family, childProfile, codeHash, expiresAt, null, 0, createdByParentId, createdAt);
    }
}
