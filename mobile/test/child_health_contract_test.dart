import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/data/repositories/child/child_repository.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/services/child_health.dart';

class _RecordingHeaderClient implements ApiClient, HeaderApiClient {
  final calls = <String>[];

  @override
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  }) async {
    calls.add('$method $path');
    throw StateError('unexpected API call');
  }

  @override
  Future<Map<String, dynamic>> sendWithHeaders(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
    Map<String, String>? headers,
  }) => send(method, path, query: query, body: body);
}

class _FixedHealthProvider implements ChildHealthProvider {
  @override
  Future<ChildHealth> readHealth() async => ChildHealth(
    monitoring: MonitoringState.active,
    sync: SyncState.current,
    observedAt: DateTime.utc(2026, 1, 1),
  );
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test(
    'child health does not call the unsupported device health route',
    () async {
      final client = _RecordingHeaderClient();
      final repository = HttpChildRepository(
        client,
        MemoryChildDeviceStore(),
        healthProvider: _FixedHealthProvider(),
      );

      final health = await repository.getHealth();

      expect(health.monitoring, MonitoringState.active);
      expect(client.calls, isEmpty);
    },
  );

  test('unsupported platform status is rendered as unsupported', () async {
    const channel = MethodChannel('com.tamdan.mobile/android_runtime');
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
          throw MissingPluginException('runtime unavailable');
        });
    addTearDown(
      () => TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(channel, null),
    );

    final health = await AndroidChildHealthProvider(
      channel: channel,
    ).readHealth();

    expect(health.monitoring, MonitoringState.unsupported);
    expect(health.sync, SyncState.unsupported);
  });
}
