import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/storage/secure_session_store.dart';
import 'package:mobile/data/repositories/parent_repository.dart';
import 'package:mobile/model/parent_models.dart';
import 'package:mobile/ui/parent/parent_view_models.dart';

class _FailingRepository implements ParentRepository {
  @override
  Future<void> login(String login, String password) async =>
      throw const ApiException(0, 'offline');
  @override
  Future<void> register({
    required String login,
    required String email,
    required String password,
    String? firstName,
    String? lastName,
  }) async {}
  @override
  Future<void> refreshSession() async =>
      throw const ApiException(401, 'expired');
  @override
  Future<void> logout() async {}
  @override
  Future<List<Child>> listChildren() async =>
      throw const ApiException(0, 'offline');
  @override
  Future<Child> createChild(String displayName) async =>
      const Child(id: 1, displayName: 'A', status: ChildStatus.active);
  @override
  Future<List<Device>> listDevices(int childId) async => const [];
  @override
  Future<Map<String, dynamic>> createPairingCode(int childId) async => {};
  @override
  Future<UsageReport> getUsageReport(
    int childId,
    DateTime from,
    DateTime to,
  ) async => throw const ApiException(0, 'offline');
  @override
  Future<List<Alert>> listAlerts(
    int childId, {
    int page = 0,
    int pageSize = 20,
  }) async => const [];
  @override
  Future<Alert> reviewAlert(int alertId, {String? note}) async =>
      throw UnimplementedError();
  @override
  Future<Alert> dismissAlert(int alertId) async => throw UnimplementedError();
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
  }) async => throw UnimplementedError();
  @override
  Future<Policy> pausePolicy(int childId, int policyId) async =>
      throw UnimplementedError();
}

void main() {
  test(
    'registration keeps the existing contract and exposes loading state',
    () async {
      final repository = _TrackingRepository();
      final auth = AuthViewModel(repository)..registerMode = true;

      final submission = auth.submit(
        'parent',
        'password',
        email: 'parent@example.com',
      );
      expect(auth.state.status, LoadStatus.loading);
      expect(repository.registered, isTrue);

      repository.complete();
      expect(await submission, isTrue);
      expect(auth.state.status, LoadStatus.ready);
    },
  );

  test('auth errors never expose raw backend or SQL text', () async {
    final auth = AuthViewModel(_SqlFailingRepository());

    expect(await auth.submit('parent', 'password'), isFalse);
    expect(
      auth.message,
      'We couldn’t sign you in. Check your details and try again.',
    );
    expect(auth.message, isNot(contains('SELECT')));
    expect(auth.message, isNot(contains('org.postgresql')));
  });

  test('ViewModels expose offline and unauthorized recovery states', () async {
    final auth = AuthViewModel(_FailingRepository());
    expect(await auth.submit('parent', 'password'), isFalse);
    expect(auth.state.status, LoadStatus.offline);
    await auth.recoverSession();
    expect(auth.state.status, LoadStatus.unauthorized);
    final children = ChildSelectionViewModel(_FailingRepository());
    await children.load();
    expect(children.state.status, LoadStatus.offline);
  });

  test(
    'logout clears secure session store through repository boundary',
    () async {
      final store = MemorySecureSessionStore();
      await store.writeTokens(accessToken: 'a', refreshToken: 'r');
      final repository = HttpParentRepository(_NoopClient(), store);
      await repository.logout();
      expect(await store.readAccessToken(), isNull);
      expect(await store.readRefreshToken(), isNull);
    },
  );
}

class _SqlFailingRepository extends _FailingRepository {
  @override
  Future<void> login(
    String login,
    String password,
  ) async => throw const ApiException(
    500,
    'org.postgresql.util.PSQLException: SELECT * FROM users WHERE password = ...',
  );
}

class _TrackingRepository extends _FailingRepository {
  final _registration = Completer<void>();
  bool registered = false;

  @override
  Future<void> register({
    required String login,
    required String email,
    required String password,
    String? firstName,
    String? lastName,
  }) async {
    registered = true;
    await _registration.future;
  }

  void complete() => _registration.complete();
}

class _NoopClient implements ApiClient {
  @override
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  }) async => {};
}
