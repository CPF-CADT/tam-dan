import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/services/child_health.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets(
    'child reads local Android status without an unsupported API call',
    (tester) async {
      final health = await AndroidChildHealthProvider().readHealth();

      expect(
        health.monitoring,
        isIn(<MonitoringState>[
          MonitoringState.active,
          MonitoringState.inactive,
          MonitoringState.permissionDenied,
          MonitoringState.failed,
          MonitoringState.unsupported,
        ]),
      );
      expect(
        health.sync,
        isIn(<SyncState>[
          SyncState.current,
          SyncState.stale,
          SyncState.offline,
          SyncState.failed,
          SyncState.unsupported,
        ]),
      );
    },
  );
}
