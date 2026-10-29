import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:mobile/main.dart' as app;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Android runtime channel reports capabilities and status', (
    tester,
  ) async {
    app.main();
    await tester.pumpAndSettle();

    const channel = MethodChannel('com.tamdan.mobile/android_runtime');
    final capabilities = Map<Object?, Object?>.from(
      await channel.invokeMethod<Object?>('getCapabilities') as Map,
    );
    final status = Map<Object?, Object?>.from(
      await channel.invokeMethod<Object?>('getStatus') as Map,
    );

    expect(capabilities['vpnEnforcement'], 'UNSUPPORTED');
    expect(capabilities['domainPolicy'], 'UNSUPPORTED');
    expect(status['state'], 'STOPPED');
    await channel.invokeMethod<void>('clearDeviceContext');
  });
}
