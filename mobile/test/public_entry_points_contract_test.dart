import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/data/repositories/child/child_repository.dart';
import 'package:mobile/data/repositories/parent/parent_repository.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/model/parent_models.dart';
import 'package:mobile/services/device_identity.dart';
import 'package:mobile/ui/screens/child/activity/child_activity_screen.dart';
import 'package:mobile/ui/screens/child/home/child_home_screen.dart';
import 'package:mobile/ui/screens/child/restrictions/child_game_restricted_screen.dart';
import 'package:mobile/ui/screens/child/restrictions/child_restrictions_screen.dart';
import 'package:mobile/ui/screens/child/restrictions/child_website_safety_screen.dart';
import 'package:mobile/ui/screens/child/settings/child_settings_screen.dart';
import 'package:mobile/ui/screens/child/setup/enable_protection_screen.dart';
import 'package:mobile/ui/screens/child/setup/permissions_screen.dart';
import 'package:mobile/ui/screens/child/setup/setup_complete_screen.dart';
import 'package:mobile/ui/screens/child/setup/vpn_setup_screen.dart';
import 'package:mobile/ui/screens/child/view_model/child_view_model.dart';
import 'package:mobile/ui/screens/parent/activity/activity_summary_screen.dart';
import 'package:mobile/ui/screens/parent/alerts/safety_alerts_screen.dart';
import 'package:mobile/ui/screens/parent/controls/app_rule_screen.dart';
import 'package:mobile/ui/screens/parent/controls/block_domain_screen.dart';
import 'package:mobile/ui/screens/parent/controls/controls_hub_screen.dart';
import 'package:mobile/ui/screens/parent/controls/game_internet_screen.dart';
import 'package:mobile/ui/screens/parent/controls/website_safety_screen.dart';
import 'package:mobile/ui/screens/parent/device/child_overview_screen.dart';
import 'package:mobile/ui/screens/parent/home/parent_home_screen.dart';
import 'package:mobile/ui/screens/parent/parent_screens.dart';
import 'package:mobile/ui/screens/parent/settings/device_settings_screen.dart';
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
  testWidgets('parent shell exposes the four Figma destinations', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1;
    addTearDown(() {
      tester.view.resetPhysicalSize();
      tester.view.resetDevicePixelRatio();
    });
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
    final navigation = tester.widget<NavigationBar>(find.byType(NavigationBar));
    expect(navigation.destinations, hasLength(4));
    expect(
      navigation.destinations.cast<NavigationDestination>().map(
        (item) => item.label,
      ),
      ['Home', 'Activity', 'Controls', 'Settings'],
    );

    await tester.tap(find.byType(NavigationDestination).at(1));
    await tester.pump();
    expect(find.byType(ActivitySummaryScreen), findsOneWidget);

    await tester.tap(find.byType(NavigationDestination).at(2));
    await tester.pump();
    expect(find.byType(ControlsHubScreen), findsOneWidget);

    await tester.tap(find.byType(NavigationDestination).at(3));
    await tester.pump();
    expect(find.byType(DeviceSettingsScreen), findsOneWidget);
  });

  testWidgets('all twenty Figma screen entry points compose', (tester) async {
    tester.view.physicalSize = const Size(500, 1200);
    tester.view.devicePixelRatio = 1;
    addTearDown(() {
      tester.view.resetPhysicalSize();
      tester.view.resetDevicePixelRatio();
    });
    final parentChild = const Child(
      id: 1,
      displayName: 'Alex',
      status: ChildStatus.active,
    );
    final children = ChildSelectionViewModel(_ParentRepository());
    final devices = DeviceViewModel(_ParentRepository());
    final reports = UsageReportViewModel(_ParentRepository());
    final alerts = AlertViewModel(_ParentRepository());
    final policies = PolicyViewModel(_ParentRepository());
    addTearDown(() {
      children.dispose();
      devices.dispose();
      reports.dispose();
      alerts.dispose();
      policies.dispose();
    });

    final parentScreens = <Widget>[
      ParentHomeScreen(
        child: parentChild,
        devices: devices,
        reports: reports,
        alerts: alerts,
        onOpenDevice: () {},
        onOpenActivity: () {},
        onOpenAlerts: () {},
      ),
      ChildOverviewScreen(
        child: parentChild,
        devices: devices,
        reports: reports,
        policies: policies,
        onActivity: () {},
        onControls: () {},
        onWebsiteSafety: () {},
      ),
      ActivitySummaryScreen(child: parentChild, viewModel: reports),
      ControlsHubScreen(child: parentChild, viewModel: policies),
      AppRuleScreen(child: parentChild, viewModel: policies),
      WebsiteSafetyScreen(child: parentChild, viewModel: policies),
      BlockDomainScreen(child: parentChild, viewModel: policies),
      GameInternetScreen(child: parentChild, viewModel: policies),
      SafetyAlertsScreen(child: parentChild, viewModel: alerts),
      DeviceSettingsScreen(
        child: parentChild,
        devices: devices,
        onLogout: () {},
        showBottomNavigation: false,
      ),
    ];

    for (final screen in parentScreens) {
      await tester.pumpWidget(MaterialApp(home: screen));
      expect(find.byWidget(screen), findsOneWidget);
    }

    final store = MemoryChildDeviceStore();
    final viewModel = ChildViewModel(
      _ChildRepository(),
      store,
      identityProvider: _IdentityProvider(),
    );
    addTearDown(viewModel.dispose);

    final childPolicy = const ChildPolicy(
      id: 1,
      version: 1,
      target: 'Example game',
      state: ChildPolicyState.active,
    );
    final childScreens = <Widget>[
      ChildHomeScreen(viewModel: viewModel),
      ChildEnableProtectionScreen(viewModel: viewModel, onContinue: () {}),
      ChildVpnSetupScreen(viewModel: viewModel, onContinue: () {}),
      ChildPermissionsScreen(viewModel: viewModel, onContinue: () {}),
      ChildSetupCompleteScreen(viewModel: viewModel, onContinue: () {}),
      ChildActivityScreen(viewModel: viewModel),
      ChildRestrictionsScreen(viewModel: viewModel),
      ChildWebsiteSafetyScreen(viewModel: viewModel),
      ChildGameRestrictedScreen(viewModel: viewModel, policy: childPolicy),
      ChildSettingsScreen(viewModel: viewModel),
    ];

    for (final screen in childScreens) {
      await tester.pumpWidget(MaterialApp(home: screen));
      expect(find.byWidget(screen), findsOneWidget);
    }
  });
}
