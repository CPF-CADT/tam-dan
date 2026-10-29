enum PairingState {
  idle,
  submitting,
  success,
  invalid,
  expired,
  used,
  offline,
  failed,
}

enum ChildLoadStatus {
  idle,
  loading,
  ready,
  offline,
  unauthorized,
  revoked,
  failed,
}

enum ConsentDecision { granted, withdrawn }

enum PermissionState { unknown, granted, denied, unavailable }

enum MonitoringState { active, inactive, permissionDenied, unsupported, failed }

enum SyncState { current, stale, offline, failed, unsupported }

enum ChildPolicyState { pending, active, failed, unsupported }

class ChildDeviceContext {
  const ChildDeviceContext({
    required this.deviceId,
    required this.childId,
    required this.displayName,
    required this.platform,
  });

  final int deviceId;
  final int childId;
  final String displayName;
  final String platform;
}

class ChildConsent {
  const ChildConsent({
    required this.version,
    required this.decision,
    required this.decidedAt,
    required this.confirmedByDevice,
  });

  final String version;
  final ConsentDecision decision;
  final DateTime decidedAt;
  final bool confirmedByDevice;
}

class ChildHealth {
  const ChildHealth({
    required this.monitoring,
    required this.sync,
    required this.observedAt,
    this.policyVersion,
    this.queuedEventCount = 0,
  });

  final MonitoringState monitoring;
  final SyncState sync;
  final DateTime observedAt;
  final String? policyVersion;
  final int queuedEventCount;

  const ChildHealth.unsupported(DateTime observedAt)
    : this(
        monitoring: MonitoringState.unsupported,
        sync: SyncState.unsupported,
        observedAt: observedAt,
      );
}

class ChildPolicy {
  const ChildPolicy({
    required this.id,
    required this.version,
    required this.target,
    required this.state,
  });

  final int id;
  final int version;
  final String target;
  final ChildPolicyState state;
}

class ChildStatusState {
  const ChildStatusState({
    this.status = ChildLoadStatus.idle,
    this.device,
    this.health,
    this.policies = const [],
    this.consent,
    this.permission = PermissionState.unknown,
    this.error,
    this.isLastKnown = false,
  });

  final ChildLoadStatus status;
  final ChildDeviceContext? device;
  final ChildHealth? health;
  final List<ChildPolicy> policies;
  final ChildConsent? consent;
  final PermissionState permission;
  final String? error;
  final bool isLastKnown;

  ChildStatusState copyWith({
    ChildLoadStatus? status,
    ChildDeviceContext? device,
    ChildHealth? health,
    List<ChildPolicy>? policies,
    ChildConsent? consent,
    PermissionState? permission,
    String? error,
    bool? isLastKnown,
    bool clearDevice = false,
    bool clearHealth = false,
    bool clearConsent = false,
  }) => ChildStatusState(
    status: status ?? this.status,
    device: clearDevice ? null : device ?? this.device,
    health: clearHealth ? null : health ?? this.health,
    policies: policies ?? this.policies,
    consent: clearConsent ? null : consent ?? this.consent,
    permission: permission ?? this.permission,
    error: error,
    isLastKnown: isLastKnown ?? this.isLastKnown,
  );
}
