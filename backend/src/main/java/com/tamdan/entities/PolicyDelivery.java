package com.tamdan.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "policy_delivery", uniqueConstraints = @UniqueConstraint(name = "ux_policy_delivery_policy_device", columnNames = { "policy_id", "child_device_id" }))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyDelivery {

    public enum State { PENDING, ACTIVE, FAILED, UNSUPPORTED }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private SafetyPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_device_id", nullable = false)
    private ChildDevice childDevice;

    @Column(name = "policy_version", nullable = false)
    private Long policyVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private State state;

    @Column(name = "acknowledged_version")
    private Long acknowledgedVersion;

    @Column(name = "reported_at")
    private Instant reportedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    public static PolicyDelivery pending(SafetyPolicy policy, ChildDevice device) {
        return new PolicyDelivery(null, policy, device, policy.getVersion(), State.PENDING, null, null, null);
    }

    public void acknowledge(Long acknowledgedVersion, State nextState, Instant reportedAt, String failureReason) {
        if (acknowledgedVersion < policyVersion) throw new IllegalArgumentException("Policy acknowledgement is stale");
        this.acknowledgedVersion = acknowledgedVersion;
        this.state = nextState;
        this.reportedAt = reportedAt;
        this.failureReason = failureReason;
    }
}
