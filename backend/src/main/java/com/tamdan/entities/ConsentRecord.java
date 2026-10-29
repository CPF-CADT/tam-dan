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
@Table(name = "consent_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsentRecord {

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

    @Column(name = "document_version", nullable = false, length = 50)
    private String documentVersion;

    @Column(name = "actor_type", nullable = false, length = 30)
    private String actorType;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "acknowledged_at", nullable = false)
    private Instant acknowledgedAt;

    @Column(name = "permission_state", nullable = false, length = 30)
    private String permissionState;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "confirmed_by_device", nullable = false)
    private Boolean confirmedByDevice;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    public static ConsentRecord create(Family family, ChildProfile child, String documentVersion, String actorType, Long actorId, Instant acknowledgedAt, String permissionState) {
        return new ConsentRecord(null, family, child, documentVersion, actorType, actorId, acknowledgedAt, permissionState, acknowledgedAt, false, null);
    }

    public void confirmByDevice(Instant confirmedAt) {
        this.confirmedByDevice = true;
        this.confirmedAt = confirmedAt;
    }
}
