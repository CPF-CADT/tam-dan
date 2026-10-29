package com.tamdan.mobile.runtime

import java.util.UUID

enum class MonitoringState { STOPPED, PENDING, ACTIVE, FAILED }
enum class PermissionState { GRANTED, REQUIRES_ACTION }
enum class SyncState { IDLE, PENDING, SYNCING, OFFLINE, COMPLETE, FAILED }
enum class CapabilityStatus { SUPPORTED, UNSUPPORTED }
enum class PolicyType { APPLICATION, GAME, DOMAIN, VPN }
enum class EnforcementState { PENDING_DELIVERY, ACTIVE, PAUSED, REMOVED, FAILED, UNSUPPORTED }

data class CapabilityMatrix(
    val activityObservation: CapabilityStatus = CapabilityStatus.SUPPORTED,
    val applicationPolicy: CapabilityStatus = CapabilityStatus.SUPPORTED,
    val gamePolicy: CapabilityStatus = CapabilityStatus.SUPPORTED,
    val domainObservation: CapabilityStatus = CapabilityStatus.UNSUPPORTED,
    val domainPolicy: CapabilityStatus = CapabilityStatus.UNSUPPORTED,
    val vpnEnforcement: CapabilityStatus = CapabilityStatus.UNSUPPORTED,
) {
    fun forPolicy(type: PolicyType): CapabilityStatus = when (type) {
        PolicyType.APPLICATION -> applicationPolicy
        PolicyType.GAME -> gamePolicy
        PolicyType.DOMAIN -> domainPolicy
        PolicyType.VPN -> vpnEnforcement
    }
}

data class MonitoringStatus(
    val state: MonitoringState,
    val permission: PermissionState,
    val sync: SyncState,
    val capabilities: CapabilityMatrix,
    val reason: String? = null,
)

data class DeviceContext(
    val deviceId: Long,
    val childId: Long,
    val accessToken: String,
    val refreshToken: String,
)

data class ActivityEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String,
    val occurredAtEpochMillis: Long,
    val packageName: String? = null,
    val domain: String? = null,
    val payload: String = "{}",
) {
    fun validate(nowMillis: Long = System.currentTimeMillis()): String? {
        if (!eventId.matches(Regex("[A-Za-z0-9_-]{1,128}"))) return "invalid event id"
        if (!eventType.matches(Regex("[A-Za-z0-9_.-]{1,64}"))) return "invalid event type"
        if (occurredAtEpochMillis <= 0L || occurredAtEpochMillis > nowMillis + 86_400_000L) {
            return "invalid event timestamp"
        }
        if (packageName != null && !packageName.matches(Regex("[A-Za-z0-9_.]{1,255}"))) {
            return "invalid package name"
        }
        if (domain != null && !domain.matches(Regex("[A-Za-z0-9.-]{1,253}"))) return "invalid domain"
        if (payload.length > MAX_PAYLOAD_CHARS) return "event payload too large"
        return null
    }

    companion object {
        const val MAX_PAYLOAD_CHARS = 4096
    }
}

data class Policy(
    val policyId: Long,
    val version: Long,
    val type: PolicyType,
)

data class EnforcementResult(
    val state: EnforcementState,
    val reason: String? = null,
)
