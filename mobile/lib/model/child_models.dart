import 'dart:convert';

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

  Map<String, dynamic> toJson() => {
    'monitoring': monitoring.name,
    'sync': sync.name,
    'observedAt': observedAt.toIso8601String(),
    'policyVersion': policyVersion,
    'queuedEventCount': queuedEventCount,
  };

  static ChildHealth fromJson(Map<String, dynamic> json) => ChildHealth(
    monitoring: MonitoringState.values.byName(json['monitoring'] as String),
    sync: SyncState.values.byName(json['sync'] as String),
    observedAt: DateTime.parse(json['observedAt'] as String),
    policyVersion: json['policyVersion'] as String?,
    queuedEventCount: json['queuedEventCount'] as int? ?? 0,
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

  Map<String, dynamic> toJson() => {
    'id': id,
    'version': version,
    'target': target,
    'state': state.name,
  };

  static ChildPolicy fromJson(Map<String, dynamic> json) => ChildPolicy(
    id: json['id'] as int,
    version: json['version'] as int,
    target: json['target'] as String,
    state: ChildPolicyState.values.byName(json['state'] as String),
  );
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

  String toJson() => jsonEncode({
    'status': status.name,
    'health': health?.toJson(),
    'policies': policies.map((policy) => policy.toJson()).toList(),
    'consent': consent == null
        ? null
        : {
            'version': consent!.version,
            'decision': consent!.decision.name,
            'decidedAt': consent!.decidedAt.toIso8601String(),
            'confirmedByDevice': consent!.confirmedByDevice,
          },
    'permission': permission.name,
    'isLastKnown': isLastKnown,
  });

  static ChildStatusState fromJson(String value) {
    final json = jsonDecode(value) as Map<String, dynamic>;
    final health = json['health'] as Map<String, dynamic>?;
    final consent = json['consent'] as Map<String, dynamic>?;
    return ChildStatusState(
      status: ChildLoadStatus.values.byName(json['status'] as String),
      health: health == null ? null : ChildHealth.fromJson(health),
      policies: (json['policies'] as List<dynamic>)
          .cast<Map<String, dynamic>>()
          .map(ChildPolicy.fromJson)
          .toList(growable: false),
      consent: consent == null
          ? null
          : ChildConsent(
              version: consent['version'] as String,
              decision: ConsentDecision.values.byName(
                consent['decision'] as String,
              ),
              decidedAt: DateTime.parse(consent['decidedAt'] as String),
              confirmedByDevice: consent['confirmedByDevice'] as bool,
            ),
      permission: PermissionState.values.byName(json['permission'] as String),
      isLastKnown: json['isLastKnown'] as bool? ?? true,
    );
  }

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
