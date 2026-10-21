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
@Table(name = "parent_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @Column(name = "refresh_token_hash", nullable = false, length = 64, unique = true)
    private String refreshTokenHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_session_id")
    private Long replacedBySessionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static ParentSession create(User parent, String refreshTokenHash, Instant issuedAt, Instant expiresAt, Instant createdAt) {
        return new ParentSession(null, parent, refreshTokenHash, issuedAt, expiresAt, null, null, createdAt);
    }

    public void replaceWith(Long sessionId, Instant revokedAt) {
        this.revokedAt = revokedAt;
        this.replacedBySessionId = sessionId;
    }

    public void revoke(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
