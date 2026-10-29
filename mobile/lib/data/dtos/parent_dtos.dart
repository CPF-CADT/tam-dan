import '../../model/parent_models.dart';

DateTime _date(Object? value) => DateTime.parse(value as String).toUtc();
Map<String, dynamic> _data(Map<String, dynamic> envelope) =>
    envelope['data'] as Map<String, dynamic>;

class AuthDto {
  const AuthDto({required this.accessToken, required this.refreshToken});
  final String accessToken;
  final String refreshToken;
  factory AuthDto.fromEnvelope(Map<String, dynamic> json) {
    final data = _data(json);
    return AuthDto(
      accessToken: data['accessToken'] as String,
      refreshToken: data['refreshToken'] as String,
    );
  }
}

class ChildDto {
  const ChildDto({
    required this.id,
    required this.displayName,
    required this.status,
  });
  final int id;
  final String displayName;
  final String status;
  factory ChildDto.fromJson(Map<String, dynamic> json) => ChildDto(
    id: json['id'] as int,
    displayName: json['displayName'] as String,
    status: json['status'] as String,
  );
  Child toModel() =>
      Child(id: id, displayName: displayName, status: childStatus(status));
}

class DeviceDto {
  const DeviceDto({
    required this.id,
    required this.childId,
    required this.displayName,
    required this.status,
    this.platform,
  });
  final int id;
  final int childId;
  final String displayName;
  final String status;
  final String? platform;
  factory DeviceDto.fromJson(Map<String, dynamic> json) => DeviceDto(
    id: json['id'] as int,
    childId: json['childId'] as int,
    displayName: json['displayName'] as String,
    status: json['status'] as String,
    platform: json['platform'] as String?,
  );
  Device toModel() => Device(
    id: id,
    childId: childId,
    displayName: displayName,
    status: deviceStatus(status),
    platform: platform,
  );
}

class ReportDto {
  const ReportDto(this.json);
  final Map<String, dynamic> json;
  UsageReport toModel() {
    final coverage =
        (json['sourceDeviceCoverage'] as List<dynamic>? ?? const [])
            .cast<Map<String, dynamic>>();
    return UsageReport(
      childId: json['childId'] as int,
      from: _date(json['from']),
      to: _date(json['to']),
      generatedAt: _date(json['generatedAt']),
      dataThrough: _date(json['dataThrough']),
      freshness: reportFreshness(json['freshness'] as String?),
      completeness: reportCompleteness(json['completeness'] as String?),
      coverage: coverage
          .map(
            (item) => SourceDeviceCoverage(
              deviceId: item['deviceId'] as int,
              status: item['status'] as String,
              missingReason: item['missingReason'] as String?,
            ),
          )
          .toList(growable: false),
      risk: riskLevel(json['risk'] as String?),
      unknownReason: json['unknownReason'] as String?,
    );
  }
}

class AlertDto {
  const AlertDto(this.json);
  final Map<String, dynamic> json;
  Alert toModel() => Alert(
    id: json['id'] as int,
    childId: json['childId'] as int,
    state: alertState(json['state'] as String?),
    severity: alertSeverity(json['severity'] as String?),
    createdAt: _date(json['createdAt']),
    title: json['title'] as String?,
    detail: json['detail'] as String?,
  );
}

class PolicyDto {
  const PolicyDto(this.json);
  final Map<String, dynamic> json;
  Policy toModel() => Policy(
    id: json['id'] as int,
    childId: json['childId'] as int,
    version: json['version'] as int,
    type: policyType(json['type'] as String?),
    target: json['target'] as String,
    state: policyState(json['state'] as String?),
    updatedAt: _date(json['updatedAt']),
  );
}
