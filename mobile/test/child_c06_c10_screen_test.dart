import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/data/repositories/child/child_repository.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/ui/screens/child/activity/child_activity_screen.dart';
import 'package:mobile/ui/screens/child/activity/child_navigation_shell.dart';
import 'package:mobile/ui/screens/child/restrictions/child_game_restricted_screen.dart';
import 'package:mobile/ui/screens/child/restrictions/child_restrictions_screen.dart';
import 'package:mobile/ui/screens/child/restrictions/child_website_safety_screen.dart';
import 'package:mobile/ui/screens/child/settings/child_settings_screen.dart';
import 'package:mobile/ui/screens/child/view_model/child_view_model.dart';

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
  Future<List<ChildPolicy>> getPolicies() async => const [
    ChildPolicy(
      id: 1,
      version: 2,
      target: 'Example game',
      state: ChildPolicyState.active,
    ),
  ];

  @override
  Future<void> clearDevice() async {}
}

void main() {
  late ChildViewModel viewModel;

  setUp(() {
    viewModel = ChildViewModel(_ChildRepository(), MemoryChildDeviceStore());
    viewModel.state = ChildStatusState(
      status: ChildLoadStatus.ready,
      device: ChildDeviceContext(
        deviceId: 1,
        childId: 1,
        displayName: 'Child device',
        platform: 'ANDROID',
      ),
      health: ChildHealth(
        monitoring: MonitoringState.active,
        sync: SyncState.current,
        observedAt: DateTime.utc(2026, 1, 1),
      ),
      policies: [
        ChildPolicy(
          id: 1,
          version: 2,
          target: 'Example game',
          state: ChildPolicyState.active,
        ),
      ],
    );
  });

  tearDown(() => viewModel.dispose());

  testWidgets('C06-C10 public screens render from existing child state', (
    tester,
  ) async {
    await tester.pumpWidget(
      MaterialApp(home: ChildActivityScreen(viewModel: viewModel)),
    );
    expect(find.text('Activity'), findsOneWidget);
    expect(find.text('Screen time today'), findsOneWidget);

    await tester.pumpWidget(
      MaterialApp(home: ChildRestrictionsScreen(viewModel: viewModel)),
    );
    expect(find.text('Active restrictions'), findsOneWidget);
    expect(find.text('Example game'), findsOneWidget);

    await tester.pumpWidget(
      MaterialApp(home: ChildWebsiteSafetyScreen(viewModel: viewModel)),
    );
    expect(find.text('Website safety'), findsOneWidget);
    expect(find.text('Website activity unavailable'), findsOneWidget);

    await tester.pumpWidget(
      MaterialApp(
        home: ChildGameRestrictedScreen(
          viewModel: viewModel,
          policy: viewModel.state.policies.single,
        ),
      ),
    );
    expect(find.text('Game restricted'), findsOneWidget);

    await tester.pumpWidget(
      MaterialApp(home: ChildSettingsScreen(viewModel: viewModel)),
    );
    expect(find.text('Settings'), findsOneWidget);
    expect(find.text('Protection status'), findsOneWidget);
  });

  testWidgets('child shell exposes four destinations', (tester) async {
    await tester.pumpWidget(
      MaterialApp(home: ChildNavigationShell(viewModel: viewModel)),
    );

    final navigation = tester.widget<NavigationBar>(find.byType(NavigationBar));
    expect(navigation.destinations, hasLength(4));
    expect(
      navigation.destinations.cast<NavigationDestination>().map(
        (item) => item.label,
      ),
      ['Home', 'Activity', 'Restrictions', 'Settings'],
    );

    await tester.tap(find.byType(NavigationDestination).at(1));
    await tester.pump();
    expect(find.text('Screen time today'), findsOneWidget);
  });

  testWidgets('home quick-access cards navigate to child destinations', (
    tester,
  ) async {
    await tester.pumpWidget(
      MaterialApp(home: ChildNavigationShell(viewModel: viewModel)),
    );

    await tester.tap(find.text('Restrictions'));
    await tester.pump();
    await tester.tap(find.text('Home'));
    await tester.pump();
    await tester.tap(find.text('Activity').first);
    await tester.pump();
    expect(find.text('Screen time today'), findsOneWidget);
  });

  testWidgets(
    'restriction tabs switch between app, website, and game content',
    (tester) async {
      await tester.pumpWidget(
        MaterialApp(home: ChildRestrictionsScreen(viewModel: viewModel)),
      );
      expect(find.text('Daily app limits'), findsOneWidget);
      expect(find.text('Website activity'), findsNothing);

      await tester.tap(find.text('Website'));
      await tester.pump();
      expect(find.text('Website activity'), findsOneWidget);
      expect(find.text('Daily app limits'), findsNothing);

      await tester.tap(find.text('Games'));
      await tester.pump();
      expect(find.text('Game restrictions'), findsOneWidget);
      expect(find.text('Example game'), findsOneWidget);
    },
  );

  testWidgets('website safety tabs switch their unavailable state', (
    tester,
  ) async {
    await tester.pumpWidget(
      MaterialApp(home: ChildWebsiteSafetyScreen(viewModel: viewModel)),
    );
    expect(find.text('Website activity unavailable'), findsOneWidget);

    await tester.tap(find.text('Blocked').first);
    await tester.pump();
    expect(find.text('Blocked websites unavailable'), findsOneWidget);
    expect(find.text('Website activity unavailable'), findsNothing);
  });
}
