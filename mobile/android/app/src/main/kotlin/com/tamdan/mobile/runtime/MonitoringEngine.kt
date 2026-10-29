package com.tamdan.mobile.runtime

interface PermissionGateway {
    fun hasRequiredPermissions(): Boolean
}

class MonitoringEngine(
    private val permissionGateway: PermissionGateway,
    private val capabilities: CapabilityMatrix = CapabilityMatrix(),
) {
    private var status = MonitoringStatus(
        state = MonitoringState.STOPPED,
        permission = PermissionState.REQUIRES_ACTION,
        sync = SyncState.IDLE,
        capabilities = capabilities,
    )

    fun startMonitoring(): Result<MonitoringStatus> {
        if (!permissionGateway.hasRequiredPermissions()) {
            status = status.copy(state = MonitoringState.PENDING, permission = PermissionState.REQUIRES_ACTION,
                reason = "required permissions are not granted")
            return Result.success(status)
        }
        status = status.copy(state = MonitoringState.ACTIVE, permission = PermissionState.GRANTED,
            sync = SyncState.PENDING, reason = null)
        return Result.success(status)
    }

    fun stopMonitoring(): Result<MonitoringStatus> {
        status = status.copy(state = MonitoringState.STOPPED, sync = SyncState.IDLE, reason = null)
        return Result.success(status)
    }

    fun onPermissionChanged(): MonitoringStatus {
        if (!permissionGateway.hasRequiredPermissions() && status.state == MonitoringState.ACTIVE) {
            status = status.copy(state = MonitoringState.FAILED, permission = PermissionState.REQUIRES_ACTION,
                reason = "required permission was removed")
        }
        return status
    }

    fun getCapabilities(): CapabilityMatrix = capabilities
    fun getStatus(): MonitoringStatus = status
}

class PolicyEnforcementEngine(
    private val capabilities: CapabilityMatrix,
) {
    private val states = mutableMapOf<Long, Pair<Long, EnforcementState>>()

    fun apply(policy: Policy): EnforcementResult {
        if (capabilities.forPolicy(policy.type) == CapabilityStatus.UNSUPPORTED) {
            return EnforcementResult(EnforcementState.UNSUPPORTED, "capability is not supported on this device")
        }
        states[policy.policyId] = policy.version to EnforcementState.PENDING_DELIVERY
        return EnforcementResult(EnforcementState.PENDING_DELIVERY)
    }

    fun acknowledge(policy: Policy): EnforcementResult {
        val pending = states[policy.policyId]
        if (pending?.first != policy.version || pending.second != EnforcementState.PENDING_DELIVERY) {
            return EnforcementResult(EnforcementState.FAILED, "acknowledgement does not match pending policy version")
        }
        states[policy.policyId] = policy.version to EnforcementState.ACTIVE
        return EnforcementResult(EnforcementState.ACTIVE)
    }

    fun remove(policyId: Long): EnforcementResult {
        val current = states[policyId] ?: return EnforcementResult(EnforcementState.REMOVED)
        states[policyId] = current.first to EnforcementState.REMOVED
        return EnforcementResult(EnforcementState.REMOVED)
    }

    fun getSupport(type: PolicyType): CapabilityStatus = capabilities.forPolicy(type)
}
