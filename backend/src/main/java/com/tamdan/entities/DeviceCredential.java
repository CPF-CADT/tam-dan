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
@Table(name = "device_credential")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_device_id", nullable = false)
    private ChildDevice childDevice;

    @Column(name = "credential_hash", nullable = false, length = 64, unique = true)
    private String credentialHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_credential_id")
    private Long replacedByCredentialId;

    public static DeviceCredential create(ChildDevice childDevice, String credentialHash, Instant issuedAt, Instant expiresAt) {
        return new DeviceCredential(null, childDevice, credentialHash, issuedAt, expiresAt, null, null);
    }

    public void replaceWith(Long credentialId, Instant revokedAt) {
        this.revokedAt = revokedAt;
        this.replacedByCredentialId = credentialId;
    }
}
