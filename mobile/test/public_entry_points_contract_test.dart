import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/data/repositories/child/child_repository.dart';
import 'package:mobile/data/repositories/parent/parent_repository.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/model/parent_models.dart';
import 'package:mobile/services/device_identity.dart';
import 'package:mobile/ui/screens/child/child_screens.dart';
import 'package:mobile/ui/screens/child/view_model/child_view_model.dart';
import 'package:mobile/ui/screens/parent/parent_screens.dart';
import 'package:mobile/ui/screens/parent/view_model/parent_view_models.dart';

class _ParentRepository implements ParentRepository {
  final now = DateTime.utc(2026, 1, 1);

  @override
  Future<void> login(String login, String password) async {}

  @override
  Future<void> register({
    required String login,
    required String email,
    required String password,
    String? firstName,
    String? lastName,
  }) async {}

  @override
  Future<void> refreshSession() async {}

  @override
  Future<void> logout() async {}

  @override
  Future<List<Child>> listChildren() async => const [
    Child(id: 1, displayName: 'Alex', status: ChildStatus.active),
  ];

  @override
  Future<Child> createChild(String displayName) async =>
      const Child(id: 2, displayName: 'New child', status: ChildStatus.active);

  @override
  Future<List<Device>> listDevices(int childId) async => const [];

  @override
  Future<Map<String, dynamic>> createPairingCode(int childId) async => {};

  @override
  Future<UsageReport> getUsageReport(
    int childId,
    DateTime from,
    DateTime to,
  ) async => UsageReport(
    childId: childId,
    from: from,
    to: to,
    generatedAt: now,
    dataThrough: now,
    freshness: ReportFreshness.fresh,
    completeness: ReportCompleteness.complete,
    coverage: const [],
    risk: RiskLevel.unknown,
  );

  @override
  Future<List<Alert>> listAlerts(
    int childId, {
    int page = 0,
    int pageSize = 20,
  }) async => const [];

  @override
  Future<Alert> reviewAlert(int alertId, {String? note}) async => Alert(
    id: alertId,
    childId: 1,
    state: AlertState.reviewed,
    severity: AlertSeverity.low,
    createdAt: now,
  );

  @override
  Future<Alert> dismissAlert(int alertId) async => Alert(
    id: alertId,
    childId: 1,
    state: AlertState.dismissed,
    severity: AlertSeverity.low,
    createdAt: now,
  );

  @override
  Future<List<Policy>> listPolicies(
    int childId, {
    int page = 0,
    int pageSize = 20,
  }) async => const [];

  @override
  Future<Policy> createPolicy(
    int childId, {
    required PolicyType type,
    required String target,
  }) async => Policy(
    id: 1,
    childId: childId,
    version: 1,
    type: type,
    target: target,
    state: PolicyState.pendingDelivery,
    updatedAt: now,
  );

  @override
  Future<Policy> pausePolicy(int childId, int policyId) async =>
      createPolicy(childId, type: PolicyType.domain, target: 'example.com');
}

class _ChildRepository implements ChildRepository {
  @override
  Future<ChildStatusState> completePairing({
    required String pairingCode,
    required String deviceIdentifier,
    required String platform,
    required String appVersion,
    required String displayName,
  }) async => const ChildStatusState();

  @override
  Future<ChildConsent> confirmDisclosure({required String version}) async =>
      ChildConsent(
        version: version,
        decision: ConsentDecision.granted,
        decidedAt: DateTime.utc(2026, 1, 1),
        confirmedByDevice: true,
      );

  @override
  Future<ChildHealth> getHealth() async => ChildHealth(
    monitoring: MonitoringState.active,
    sync: SyncState.current,
    observedAt: DateTime.utc(2026, 1, 1),
  );

  @override
  Future<List<ChildPolicy>> getPolicies() async => const [];

  @override
  Future<void> clearDevice() async {}
}

class _IdentityProvider implements DeviceIdentityProvider {
  @override
  Future<DeviceIdentity> readIdentity() async =>
      const DeviceIdentity(identifier: 'test-device', platform: 'ANDROID');
}

void main() {
  testWidgets('parent shell composes overview, alerts, and policies tabs', (
    tester,
  ) async {
    final repository = _ParentRepository();
    final children = ChildSelectionViewModel(repository);
    final devices = DeviceViewModel(repository);
    final reports = UsageReportViewModel(repository);
    final alerts = AlertViewModel(repository);
    final policies = PolicyViewModel(repository);
    addTearDown(() {
      children.dispose();
      devices.dispose();
      reports.dispose();
      alerts.dispose();
      policies.dispose();
    });

    await tester.pumpWidget(
      MaterialApp(
        home: ParentShell(
          children: children,
          devices: devices,
          reports: reports,
          alerts: alerts,
          policies: policies,
          onLogout: () {},
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('Overview'), findsWidgets);
    expect(find.text('Devices'), findsOneWidget);

    await tester.tap(find.text('Alerts').last);
    await tester.pumpAndSettle();
    expect(find.text('Alerts'), findsWidgets);

    await tester.tap(find.text('Policies').last);
    await tester.pumpAndSettle();
    expect(find.text('Policies'), findsWidgets);
  });

  testWidgets('child setup, status, and consent entry points compose', (
    tester,
  ) async {
    final store = MemoryChildDeviceStore();
    final viewModel = ChildViewModel(
      _ChildRepository(),
      store,
      identityProvider: _IdentityProvider(),
    );
    addTearDown(viewModel.dispose);

    await tester.pumpWidget(
      MaterialApp(home: ChildSetupScreen(viewModel: viewModel)),
    );
    expect(find.text('Pair device'), findsOneWidget);

    await tester.pumpWidget(
      MaterialApp(home: ChildStatusScreen(viewModel: viewModel)),
    );
    expect(find.text('Monitoring'), findsOneWidget);
    expect(find.text('Refresh status'), findsOneWidget);

    await tester.pumpWidget(
      MaterialApp(home: ChildConsentScreen(viewModel: viewModel)),
    );
    expect(find.text('I understand and continue'), findsOneWidget);
  });
}
