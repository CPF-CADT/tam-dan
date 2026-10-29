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
@Table(name = "safety_alert", uniqueConstraints = @UniqueConstraint(name = "ux_safety_alert_dedupe", columnNames = { "child_profile_id", "dedupe_key" }))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SafetyAlert {

    public enum State { OPEN, REVIEWED, DISMISSED }
    public enum Severity { LOW, MEDIUM, HIGH, UNKNOWN }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_profile_id", nullable = false)
    private ChildProfile childProfile;

    @Column(name = "dedupe_key", nullable = false, length = 150)
    private String dedupeKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private State state;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "detail", length = 2000)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    public static SafetyAlert create(ChildProfile child, String dedupeKey, Severity severity, String title, String detail, Instant now) {
        return new SafetyAlert(null, child, dedupeKey, State.OPEN, severity, title, detail, now, null, null);
    }

    public void review(String note, Instant now) { state = State.REVIEWED; reviewNote = note; reviewedAt = now; }
    public void dismiss(Instant now) { state = State.DISMISSED; reviewedAt = now; }
}
