enum ChildStatus { active, deactivated }

enum DeviceStatus { pairingPending, active, revoked }

enum ReportFreshness { fresh, stale, delayed, unknown }

enum ReportCompleteness { complete, partial, insufficient, unknown }

enum RiskLevel { safe, risk, unknown }

enum AlertState { open, reviewed, dismissed }

enum AlertSeverity { low, medium, high, unknown }

enum PolicyType { application, game, domain }

enum PolicyState {
  draft,
  pendingDelivery,
  active,
  paused,
  removed,
  failed,
  unsupported,
}

enum PolicyDeliveryState { pending, partial, acknowledged, failed, unsupported }

ChildStatus childStatus(String? value) => ChildStatus.values.firstWhere(
  (item) => item.name.toUpperCase() == value,
  orElse: () => ChildStatus.active,
);
DeviceStatus deviceStatus(String? value) => DeviceStatus.values.firstWhere(
  (item) => item.name.toUpperCase() == value,
  orElse: () => DeviceStatus.pairingPending,
);
ReportFreshness reportFreshness(String? value) =>
    ReportFreshness.values.firstWhere(
      (item) => item.name.toUpperCase() == value,
      orElse: () => ReportFreshness.unknown,
    );
ReportCompleteness reportCompleteness(String? value) =>
    ReportCompleteness.values.firstWhere(
      (item) => item.name.toUpperCase() == value,
      orElse: () => ReportCompleteness.unknown,
    );
RiskLevel riskLevel(String? value) => RiskLevel.values.firstWhere(
  (item) => item.name.toUpperCase() == value,
  orElse: () => RiskLevel.unknown,
);
AlertState alertState(String? value) => AlertState.values.firstWhere(
  (item) => item.name.toUpperCase() == value,
  orElse: () => AlertState.open,
);
AlertSeverity alertSeverity(String? value) => AlertSeverity.values.firstWhere(
  (item) => item.name.toUpperCase() == value,
  orElse: () => AlertSeverity.unknown,
);
PolicyType policyType(String? value) => PolicyType.values.firstWhere(
  (item) => item.name.toUpperCase() == value,
  orElse: () => PolicyType.domain,
);
PolicyState policyState(String? value) => PolicyState.values.firstWhere(
  (item) => item.name.toUpperCase() == value,
  orElse: () => PolicyState.failed,
);

class Child {
  const Child({
    required this.id,
    required this.displayName,
    required this.status,
  });
  final int id;
  final String displayName;
  final ChildStatus status;
}

class Device {
  const Device({
    required this.id,
    required this.childId,
    required this.displayName,
    required this.status,
    this.platform,
  });
  final int id;
  final int childId;
  final String displayName;
  final DeviceStatus status;
  final String? platform;
}

class SourceDeviceCoverage {
  const SourceDeviceCoverage({
    required this.deviceId,
    required this.status,
    this.missingReason,
  });
  final int deviceId;
  final String status;
  final String? missingReason;
}

class UsageReport {
  const UsageReport({
    required this.childId,
    required this.from,
    required this.to,
    required this.generatedAt,
    required this.dataThrough,
    required this.freshness,
    required this.completeness,
    required this.coverage,
    required this.risk,
    this.unknownReason,
  });
  final int childId;
  final DateTime from;
  final DateTime to;
  final DateTime generatedAt;
  final DateTime dataThrough;
  final ReportFreshness freshness;
  final ReportCompleteness completeness;
  final List<SourceDeviceCoverage> coverage;
  final RiskLevel risk;
  final String? unknownReason;
}

class Alert {
  const Alert({
    required this.id,
    required this.childId,
    required this.state,
    required this.severity,
    required this.createdAt,
    this.title,
    this.detail,
  });
  final int id;
  final int childId;
  final AlertState state;
  final AlertSeverity severity;
  final DateTime createdAt;
  final String? title;
  final String? detail;
}

class Policy {
  const Policy({
    required this.id,
    required this.childId,
    required this.version,
    required this.type,
    required this.target,
    required this.state,
    required this.updatedAt,
  });
  final int id;
  final int childId;
  final int version;
  final PolicyType type;
  final String target;
  final PolicyState state;
  final DateTime updatedAt;
}
