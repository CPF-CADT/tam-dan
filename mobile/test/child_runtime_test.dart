import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/services/child_runtime.dart';

void main() {
  test(
    'unsupported runtime never reports VPN or monitoring as active',
    () async {
      final status = await const UnsupportedChildRuntimeAdapter().readStatus();

      expect(status.monitoring, MonitoringState.unsupported);
      expect(status.permission, PermissionState.unavailable);
      expect(status.vpn, ChildCapabilityState.unsupported);
    },
  );

  test('unsupported runtime permission requests remain safe no-ops', () async {
    const adapter = UnsupportedChildRuntimeAdapter();

    await adapter.requestPermission(ChildPermissionKind.usageAccess);
    final status = await adapter.startMonitoring();

    expect(status.vpn, ChildCapabilityState.unsupported);
    expect(status.monitoring, MonitoringState.unsupported);
  });
}
