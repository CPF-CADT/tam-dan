package com.tamdan.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "security_audit_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "actor_type", nullable = false, length = 30)
    private String actorType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "result", nullable = false, length = 30)
    private String result;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "ip_address_hash", length = 64)
    private String ipAddressHash;

    @Column(name = "user_agent_hash", length = 64)
    private String userAgentHash;

    @Column(name = "safe_metadata_json", nullable = false, columnDefinition = "text")
    private String safeMetadataJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public static SecurityAuditEvent create(
        String actorType, Long actorId, Long familyId, String action, String resourceType, Long resourceId,
        String result, String requestId, String ipAddressHash, String userAgentHash, String safeMetadataJson, Instant occurredAt
    ) {
        return new SecurityAuditEvent(null, actorType, actorId, familyId, action, resourceType, resourceId, result, requestId, ipAddressHash, userAgentHash, safeMetadataJson, occurredAt);
    }
}
