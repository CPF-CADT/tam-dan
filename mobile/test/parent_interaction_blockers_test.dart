import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/data/repositories/parent/parent_repository.dart';
import 'package:mobile/model/parent_models.dart';
import 'package:mobile/ui/screens/parent/controls/app_rule_screen.dart';
import 'package:mobile/ui/screens/parent/device/child_overview_screen.dart';
import 'package:mobile/ui/screens/parent/home/parent_home_screen.dart';
import 'package:mobile/ui/screens/parent/parent_screens.dart';
import 'package:mobile/ui/screens/parent/view_model/parent_view_models.dart';

class _ParentRepository implements ParentRepository {
  final now = DateTime.utc(2026, 1, 1);
  int createPolicyCalls = 0;

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
    freshness: ReportFreshness.unknown,
    completeness: ReportCompleteness.unknown,
    coverage: const [],
    risk: RiskLevel.unknown,
  );

  @override
  Future<List<Alert>> listAlerts(
    int childId, {
    int page = 0,
    int pageSize = 20,
  }) async => [
    Alert(
      id: 1,
      childId: childId,
      state: AlertState.open,
      severity: AlertSeverity.high,
      createdAt: now,
      title: 'New alert',
    ),
  ];

  @override
  Future<Alert> reviewAlert(int alertId, {String? note}) async => Alert(
    id: alertId,
    childId: 1,
    state: AlertState.reviewed,
    severity: AlertSeverity.high,
    createdAt: now,
  );

  @override
  Future<Alert> dismissAlert(int alertId) async => reviewAlert(alertId);

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
  }) async {
    createPolicyCalls++;
    return Policy(
      id: 1,
      childId: childId,
      version: 1,
      type: type,
      target: target,
      state: PolicyState.pendingDelivery,
      updatedAt: now,
    );
  }

  @override
  Future<Policy> pausePolicy(int childId, int policyId) async =>
      createPolicy(childId, type: PolicyType.domain, target: 'example.com');
}

const _child = Child(id: 1, displayName: 'Alex', status: ChildStatus.active);

void main() {
  testWidgets('P01 alert card opens P09', (tester) async {
    final repository = _ParentRepository();
    final alerts = AlertViewModel(repository);
    final devices = DeviceViewModel(repository);
    final reports = UsageReportViewModel(repository);
    var opened = false;
    addTearDown(() {
      alerts.dispose();
      devices.dispose();
      reports.dispose();
    });

    await tester.pumpWidget(
      MaterialApp(
        home: ParentHomeScreen(
          child: _child,
          devices: devices,
          reports: reports,
          alerts: alerts,
          onOpenDevice: () {},
          onOpenActivity: () {},
          onOpenAlerts: () => opened = true,
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.textContaining('alerts need review'));

    expect(opened, isTrue);
  });

  testWidgets('P02 website safety routes through the parent shell to P06', (
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
    await tester.tap(find.text('Alex').last);
    await tester.pumpAndSettle();
    expect(find.text('Protection status unavailable'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Website safety'),
      300,
      scrollable: find.byType(Scrollable),
    );
    await tester.tap(find.text('Website safety'));
    await tester.pumpAndSettle();

    expect(find.text('Block a website'), findsOneWidget);
  });

  testWidgets(
    'P02 website safety link opens P06 and unknown status is honest',
    (tester) async {
      final repository = _ParentRepository();
      final devices = DeviceViewModel(repository);
      final reports = UsageReportViewModel(repository);
      final policies = PolicyViewModel(repository);
      var opened = false;
      addTearDown(() {
        devices.dispose();
        reports.dispose();
        policies.dispose();
      });

      await tester.pumpWidget(
        MaterialApp(
          home: ChildOverviewScreen(
            child: _child,
            devices: devices,
            reports: reports,
            policies: policies,
            onActivity: () {},
            onControls: () {},
            onWebsiteSafety: () => opened = true,
          ),
        ),
      );
      await tester.pumpAndSettle();
      await tester.scrollUntilVisible(
        find.text('Website safety'),
        300,
        scrollable: find.byType(Scrollable).first,
      );
      await tester.tap(find.text('Website safety'));

      expect(opened, isTrue);
      expect(find.text('Protection status unavailable'), findsOneWidget);
      expect(find.text('Protected now'), findsNothing);
    },
  );

  testWidgets('P05 does not save unsupported access modes', (tester) async {
    final repository = _ParentRepository();
    final policies = PolicyViewModel(repository);
    addTearDown(policies.dispose);

    await tester.pumpWidget(
      MaterialApp(
        home: AppRuleScreen(child: _child, viewModel: policies),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('Access modes are unavailable'), findsOneWidget);
    expect(find.textContaining('No policy will be saved'), findsOneWidget);

    expect(repository.createPolicyCalls, 0);
  });
}
