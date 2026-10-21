package com.tamdan.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "child_device")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private ChildProfile childProfile;

    @Column(name = "device_identifier_hash", nullable = false, length = 64, unique = true)
    private String deviceIdentifierHash;

    @Column(name = "platform", nullable = false, length = 30)
    private String platform;

    @Column(name = "app_version", nullable = false, length = 30)
    private String appVersion;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "capabilities_json", nullable = false, columnDefinition = "text")
    private String capabilitiesJson;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ChildDevice create(
        ChildProfile childProfile,
        String deviceIdentifierHash,
        String platform,
        String appVersion,
        String displayName,
        String capabilitiesJson,
        Instant now
    ) {
        return new ChildDevice(null, childProfile, deviceIdentifierHash, platform, appVersion, displayName, capabilitiesJson, true, null, null, now, now);
    }

    public void rename(String displayName) {
        this.displayName = displayName;
    }

    public void revoke(Instant revokedAt) {
        this.active = false;
        this.revokedAt = revokedAt;
    }

    @PrePersist
    private void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = createdAt;
        if (active == null) active = true;
    }

    @PreUpdate
    private void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
