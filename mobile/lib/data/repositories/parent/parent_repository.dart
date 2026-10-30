import '../../../core/network/api_client.dart';
import '../../../core/storage/secure_session_store.dart';
import '../../../model/parent_models.dart';
import '../../dtos/parent_dtos.dart';

abstract interface class ParentRepository {
  Future<void> login(String login, String password);
  Future<void> register({
    required String login,
    required String email,
    required String password,
    String? firstName,
    String? lastName,
  });
  Future<void> refreshSession();
  Future<void> logout();
  Future<List<Child>> listChildren();
  Future<Child> createChild(String displayName);
  Future<List<Device>> listDevices(int childId);
  Future<Map<String, dynamic>> createPairingCode(int childId);
  Future<UsageReport> getUsageReport(int childId, DateTime from, DateTime to);
  Future<List<Alert>> listAlerts(
    int childId, {
    int page = 0,
    int pageSize = 20,
  });
  Future<Alert> reviewAlert(int alertId, {String? note});
  Future<Alert> dismissAlert(int alertId);
  Future<List<Policy>> listPolicies(
    int childId, {
    int page = 0,
    int pageSize = 20,
  });
  Future<Policy> createPolicy(
    int childId, {
    required PolicyType type,
    required String target,
  });
  Future<Policy> pausePolicy(int childId, int policyId);
}

class HttpParentRepository implements ParentRepository {
  HttpParentRepository(this._api, this._sessions);
  final ApiClient _api;
  final SecureSessionStore _sessions;

  String _requiredText(String value, String field, int maxLength) {
    final normalized = value.trim();
    if (normalized.isEmpty ||
        normalized.length > maxLength ||
        normalized.contains(RegExp(r'[\u0000-\u001f\u007f]'))) {
      throw ArgumentError('$field is invalid');
    }
    return normalized;
  }

  int _requiredId(int id, String field) {
    if (id < 1) throw ArgumentError('$field must be positive');
    return id;
  }

  @override
  Future<void> login(String login, String password) async {
    final response = await _api.send(
      'POST',
      '/auth/login',
      body: {
        'login': _requiredText(login, 'login', 254),
        'password': _requiredText(password, 'password', 100),
      },
    );
    final auth = AuthDto.fromEnvelope(response);
    await _sessions.writeTokens(
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
    );
  }

  @override
  Future<void> register({
    required String login,
    required String email,
    required String password,
    String? firstName,
    String? lastName,
  }) async {
    final response = await _api.send(
      'POST',
      '/auth/register',
      body: {
        'login': _requiredText(login, 'login', 50),
        'email': _requiredText(email, 'email', 254),
        'password': _requiredText(password, 'password', 100),
        if (firstName != null)
          'firstName': _requiredText(firstName, 'firstName', 50),
        if (lastName != null)
          'lastName': _requiredText(lastName, 'lastName', 50),
      },
    );
    final auth = AuthDto.fromEnvelope(response);
    await _sessions.writeTokens(
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
    );
  }

  @override
  Future<void> refreshSession() async {
    final refresh = await _sessions.readRefreshToken();
    if (refresh == null || refresh.isEmpty) {
      throw const ApiException(401, 'Session expired');
    }
    final response = await _api.send(
      'POST',
      '/auth/refresh',
      body: {'refreshToken': refresh},
    );
    final auth = AuthDto.fromEnvelope(response);
    await _sessions.writeTokens(
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
    );
  }

  @override
  Future<void> logout() async {
    final refresh = await _sessions.readRefreshToken();
    try {
      if (refresh != null && refresh.isNotEmpty) {
        await _api.send(
          'POST',
          '/auth/logout',
          body: {'refreshToken': refresh},
        );
      }
    } finally {
      await _sessions.clear();
    }
  }

  @override
  Future<List<Child>> listChildren() async {
    final response = await _api.send('GET', '/families/me/children');
    final items = response['data'] as List<dynamic>;
    return items
        .map(
          (item) => ChildDto.fromJson(item as Map<String, dynamic>).toModel(),
        )
        .toList(growable: false);
  }

  @override
  Future<Child> createChild(String displayName) async {
    final response = await _api.send(
      'POST',
      '/families/me/children',
      body: {'displayName': _requiredText(displayName, 'displayName', 100)},
    );
    return ChildDto.fromJson(_data(response)).toModel();
  }

  @override
  Future<List<Device>> listDevices(int childId) async {
    final id = _requiredId(childId, 'childId');
    final response = await _api.send(
      'GET',
      '/families/me/children/$id/devices',
    );
    final items = response['data'] as List<dynamic>;
    return items
        .map(
          (item) => DeviceDto.fromJson(item as Map<String, dynamic>).toModel(),
        )
        .toList(growable: false);
  }

  @override
  Future<Map<String, dynamic>> createPairingCode(int childId) async {
    final response = await _api.send(
      'POST',
      '/families/me/children/${_requiredId(childId, 'childId')}/pairing-codes',
    );
    return _data(response);
  }

  @override
  Future<UsageReport> getUsageReport(
    int childId,
    DateTime from,
    DateTime to,
  ) async {
    if (!from.isBefore(to)) throw ArgumentError('from must be before to');
    final response = await _api.send(
      'GET',
      '/families/me/children/${_requiredId(childId, 'childId')}/reports/usage',
      query: {
        'from': from.toUtc().toIso8601String(),
        'to': to.toUtc().toIso8601String(),
      },
    );
    return ReportDto(response['data'] as Map<String, dynamic>).toModel();
  }

  @override
  Future<List<Alert>> listAlerts(
    int childId, {
    int page = 0,
    int pageSize = 20,
  }) async {
    if (page < 0 || pageSize < 1 || pageSize > 100) {
      throw ArgumentError('invalid page');
    }
    final response = await _api.send(
      'GET',
      '/families/me/children/${_requiredId(childId, 'childId')}/alerts',
      query: {'page': page, 'pageSize': pageSize},
    );
    final data = response['data'] as Map<String, dynamic>;
    return (data['items'] as List<dynamic>)
        .map((item) => AlertDto(item as Map<String, dynamic>).toModel())
        .toList(growable: false);
  }

  @override
  Future<Alert> reviewAlert(int alertId, {String? note}) async {
    final response = await _api.send(
      'POST',
      '/alerts/${_requiredId(alertId, 'alertId')}/review',
      body: {
        'state': 'REVIEWED',
        if (note != null) 'note': _requiredText(note, 'note', 500),
      },
    );
    return AlertDto(response['data'] as Map<String, dynamic>).toModel();
  }

  @override
  Future<Alert> dismissAlert(int alertId) async {
    final response = await _api.send(
      'POST',
      '/alerts/${_requiredId(alertId, 'alertId')}/dismissal',
    );
    return AlertDto(response['data'] as Map<String, dynamic>).toModel();
  }

  @override
  Future<List<Policy>> listPolicies(
    int childId, {
    int page = 0,
    int pageSize = 20,
  }) async {
    if (page < 0 || pageSize < 1 || pageSize > 100) {
      throw ArgumentError('invalid page');
    }
    final response = await _api.send(
      'GET',
      '/families/me/children/${_requiredId(childId, 'childId')}/policies',
      query: {'page': page, 'pageSize': pageSize},
    );
    final data = response['data'] as Map<String, dynamic>;
    return (data['items'] as List<dynamic>)
        .map((item) => PolicyDto(item as Map<String, dynamic>).toModel())
        .toList(growable: false);
  }

  @override
  Future<Policy> createPolicy(
    int childId, {
    required PolicyType type,
    required String target,
  }) async {
    final response = await _api.send(
      'POST',
      '/families/me/children/${_requiredId(childId, 'childId')}/policies',
      body: {
        'type': type.name.toUpperCase(),
        'target': _requiredText(target, 'target', 255),
      },
    );
    return PolicyDto(response['data'] as Map<String, dynamic>).toModel();
  }

  @override
  Future<Policy> pausePolicy(int childId, int policyId) async {
    final response = await _api.send(
      'POST',
      '/families/me/children/${_requiredId(childId, 'childId')}/policies/${_requiredId(policyId, 'policyId')}/pause',
    );
    return PolicyDto(response['data'] as Map<String, dynamic>).toModel();
  }
}

Map<String, dynamic> _data(Map<String, dynamic> envelope) =>
    envelope['data'] as Map<String, dynamic>;
