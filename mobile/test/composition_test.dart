import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/storage/secure_session_store.dart';
import 'package:mobile/data/repositories/child/child_repository.dart';
import 'package:mobile/data/repositories/parent/parent_repository.dart';
import 'package:mobile/model/child_models.dart';
import 'package:mobile/services/child_secure_store.dart';
import 'package:mobile/ui/screens/child/setup/child_pairing_screen.dart';
import 'package:mobile/main.dart';

class _NoopClient implements ApiClient {
  @override
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  }) async => {'data': {}};
}

class _ChildRepository implements ChildRepository {
  @override
  Future<ChildStatusState> completePairing({
    required String pairingCode,
    required String deviceIdentifier,
    required String platform,
    required String appVersion,
    required String displayName,
  }) => throw UnimplementedError();

  @override
  Future<ChildConsent> confirmDisclosure({required String version}) =>
      throw UnimplementedError();

  @override
  Future<ChildHealth> getHealth() => throw UnimplementedError();

  @override
  Future<List<ChildPolicy>> getPolicies() => throw UnimplementedError();

  @override
  Future<void> clearDevice() => throw UnimplementedError();
}

void main() {
  test('production composition uses platform-backed adapters', () {
    final composition = TamDanComposition.production(
      Uri.parse('https://api.example.test/api/v1'),
    );

    expect(composition.parentRepository, isA<HttpParentRepository>());
    expect(composition.childRepository, isA<HttpChildRepository>());
    expect(composition.childStore, isA<PlatformChildDeviceStore>());
  });

  testWidgets('child role is composed at startup through MVVM boundaries', (
    tester,
  ) async {
    final parent = HttpParentRepository(
      _NoopClient(),
      MemorySecureSessionStore(),
    );
    final store = MemoryChildDeviceStore();

    await tester.pumpWidget(
      TamDanApp(
        repository: parent,
        childMode: true,
        childRepository: _ChildRepository(),
        childStore: store,
      ),
    );
    await tester.pumpAndSettle();

    expect(find.byType(ChildSetupScreen), findsOneWidget);
    expect(find.text('Pair device'), findsOneWidget);
  });
}
