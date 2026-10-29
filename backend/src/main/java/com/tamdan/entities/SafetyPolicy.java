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
@Table(name = "safety_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SafetyPolicy {

    public enum Type { APPLICATION, GAME, DOMAIN }
    public enum State { DRAFT, PENDING_DELIVERY, ACTIVE, PAUSED, REMOVED, FAILED, UNSUPPORTED }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private ChildProfile childProfile;

    @Column(name = "version", nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Type type;

    @Column(name = "target", nullable = false, length = 255)
    private String target;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 30)
    private State state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static SafetyPolicy create(ChildProfile child, Type type, String target, Instant now) {
        return new SafetyPolicy(null, child, 1L, type, target, State.DRAFT, now, now);
    }

    public void update(Type nextType, String nextTarget) {
        version++;
        type = nextType;
        target = nextTarget;
        state = State.DRAFT;
    }

    public void markPendingDelivery() { state = State.PENDING_DELIVERY; }
    public void pause() { state = State.PAUSED; }
    public void remove() { state = State.REMOVED; }
    public void markFailed() { state = State.FAILED; }
    public void markUnsupported() { state = State.UNSUPPORTED; }

    public void activate() { state = State.ACTIVE; }

    @PrePersist
    private void initializeTimestamps() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    private void updateTimestamp() { updatedAt = Instant.now(); }
}
