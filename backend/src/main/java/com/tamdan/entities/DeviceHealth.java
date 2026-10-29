package com.tamdan.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "device_health")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_device_id", nullable = false, unique = true)
    private ChildDevice childDevice;

    @Column(name = "schema_version", nullable = false, length = 30)
    private String schemaVersion;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    @Column(name = "monitoring_state", nullable = false, length = 30)
    private String monitoringState;

    @Column(name = "sync_state", nullable = false, length = 30)
    private String syncState;

    @Column(name = "policy_version", length = 100)
    private String policyVersion;

    @Column(name = "queued_event_count")
    private Integer queuedEventCount;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    public static DeviceHealth create(ChildDevice device, String schemaVersion, Instant reportedAt, String monitoringState, String syncState, String policyVersion, Integer queuedEventCount, Instant observedAt) {
        return new DeviceHealth(null, device, schemaVersion, reportedAt, monitoringState, syncState, policyVersion, queuedEventCount, observedAt);
    }

    public void update(String schemaVersion, Instant reportedAt, String monitoringState, String syncState, String policyVersion, Integer queuedEventCount, Instant observedAt) {
        this.schemaVersion = schemaVersion;
        this.reportedAt = reportedAt;
        this.monitoringState = monitoringState;
        this.syncState = syncState;
        this.policyVersion = policyVersion;
        this.queuedEventCount = queuedEventCount;
        this.observedAt = observedAt;
    }
}
