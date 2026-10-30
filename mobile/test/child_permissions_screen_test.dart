import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/data/repositories/child/child_repository.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/services/child_runtime.dart';
import 'package:mobile/ui/screens/child/setup/permissions_screen.dart';
import 'package:mobile/ui/screens/child/view_model/child_view_model.dart';

class _Repository implements ChildRepository {
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
      throw UnimplementedError();

  @override
  Future<ChildHealth> getHealth() async =>
      ChildHealth.unsupported(DateTime.utc(2026));

  @override
  Future<List<ChildPolicy>> getPolicies() async => const [];

  @override
  Future<void> clearDevice() async {}
}

class _Runtime implements ChildRuntimeAdapter {
  ChildRuntimeStatus status = const ChildRuntimeStatus(
    monitoring: MonitoringState.permissionDenied,
    sync: SyncState.current,
    permission: PermissionState.denied,
    vpn: ChildCapabilityState.unsupported,
  );

  @override
  Future<ChildRuntimeStatus> readStatus() async => status;

  @override
  Future<void> requestPermission(ChildPermissionKind kind) async {}

  @override
  Future<ChildRuntimeStatus> startMonitoring() async => status;
}

void main() {
  testWidgets('permission rows show truthful unsupported states and retry', (
    tester,
  ) async {
    final runtime = _Runtime();
    final repository = _Repository();
    final viewModel = ChildViewModel(
      repository,
      MemoryChildDeviceStore(),
      runtimeAdapter: runtime,
    )..runtimeStatus = runtime.status;
    addTearDown(viewModel.dispose);

    await tester.pumpWidget(
      MaterialApp(
        home: ChildPermissionsScreen(viewModel: viewModel, onContinue: () {}),
      ),
    );

    expect(find.text('Grant'), findsOneWidget);
    expect(find.text('Unavailable'), findsNWidgets(3));
    expect(find.text('Granted'), findsNothing);

    await tester.tap(find.text('Grant'));
    await tester.pump();
    expect(find.text('Grant'), findsOneWidget);
    expect(find.text('Granted'), findsNothing);
  });
}
