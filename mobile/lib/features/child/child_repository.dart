import '../../core/network/api_client.dart';
import 'child_models.dart';

abstract interface class ChildDeviceStore {
  Future<String?> readCredential();
  Future<ChildDeviceContext?> readDevice();
  Future<ChildStatusState?> readLastKnownState();
  Future<void> writePairing({
    required String credential,
    required ChildDeviceContext device,
  });
  Future<void> writeLastKnownState(ChildStatusState state);
  Future<void> clearProtectedState();
}

class MemoryChildDeviceStore implements ChildDeviceStore {
  String? _credential;
  ChildDeviceContext? _device;
  ChildStatusState? _lastKnown;

  @override
  Future<String?> readCredential() async => _credential;

  @override
  Future<ChildDeviceContext?> readDevice() async => _device;

  @override
  Future<ChildStatusState?> readLastKnownState() async => _lastKnown;

  @override
  Future<void> writePairing({
    required String credential,
    required ChildDeviceContext device,
  }) async {
    _credential = credential;
    _device = device;
  }

  @override
  Future<void> writeLastKnownState(ChildStatusState state) async =>
      _lastKnown = state;

  @override
  Future<void> clearProtectedState() async {
    _credential = null;
    _device = null;
    _lastKnown = null;
  }
}

abstract interface class ChildRepository {
  Future<ChildStatusState> completePairing({
    required String pairingCode,
    required String deviceIdentifier,
    required String platform,
    required String appVersion,
    required String displayName,
  });
  Future<ChildConsent> confirmDisclosure({required String version});
  Future<ChildHealth> getHealth();
  Future<List<ChildPolicy>> getPolicies();
  Future<void> clearDevice();
}

class HttpChildRepository implements ChildRepository {
  HttpChildRepository(this._api, this._store);
  final ApiClient _api;
  final ChildDeviceStore _store;

  String _text(String value, String field, int max) {
    final normalized = value.trim();
    if (normalized.isEmpty ||
        normalized.length > max ||
        RegExp(r'[\u0000-\u001f\u007f]').hasMatch(normalized)) {
      throw ArgumentError('$field is invalid');
    }
    return normalized;
  }

  @override
  Future<ChildStatusState> completePairing({
    required String pairingCode,
    required String deviceIdentifier,
    required String platform,
    required String appVersion,
    required String displayName,
  }) async {
    final response = await _api.send(
      'POST',
      '/pairing/complete',
      body: {
        'pairingCode': _text(pairingCode, 'pairingCode', 20),
        'deviceIdentifier': _text(deviceIdentifier, 'deviceIdentifier', 255),
        'platform': _text(platform, 'platform', 30),
        'appVersion': _text(appVersion, 'appVersion', 30),
        'displayName': _text(displayName, 'displayName', 100),
        'capabilities': <String, bool>{},
      },
    );
    final data = response['data'] as Map<String, dynamic>;
    final device = data['device'] as Map<String, dynamic>;
    final credential = data['credential'] as String;
    if (credential.isEmpty) {
      throw const ApiException(0, 'Pairing did not issue a credential');
    }
    final context = ChildDeviceContext(
      deviceId: device['id'] as int,
      childId: device['childId'] as int,
      displayName: device['displayName'] as String,
      platform: device['platform'] as String? ?? platform,
    );
    await _store.writePairing(credential: credential, device: context);
    return ChildStatusState(status: ChildLoadStatus.ready, device: context);
  }

  Future<Map<String, String>> _deviceHeaders() async {
    final credential = await _store.readCredential();
    if (credential == null || credential.isEmpty) {
      throw const ApiException(401, 'Device is not paired');
    }
    return {'X-Device-Credential': credential};
  }

  Future<Map<String, dynamic>> _deviceSend(
    String method,
    String path, {
    Map<String, dynamic>? body,
  }) async {
    final headers = await _deviceHeaders();
    final client = _api;
    if (client is HeaderApiClient) {
      final headerClient = client as HeaderApiClient;
      return headerClient.sendWithHeaders(
        method,
        path,
        body: body,
        headers: headers,
      );
    }
    throw const ApiException(
      0,
      'Device transport does not support credentials',
    );
  }

  @override
  Future<ChildConsent> confirmDisclosure({required String version}) async {
    final normalized = _text(version, 'version', 30);
    await _deviceSend(
      'POST',
      '/devices/me/consent',
      body: {
        'version': normalized,
        'confirmedAt': DateTime.now().toUtc().toIso8601String(),
      },
    );
    return ChildConsent(
      version: normalized,
      decision: ConsentDecision.granted,
      decidedAt: DateTime.now().toUtc(),
      confirmedByDevice: true,
    );
  }

  @override
  Future<ChildHealth> getHealth() async {
    final response = await _deviceSend('GET', '/devices/me/health');
    final data = response['data'] as Map<String, dynamic>;
    return ChildHealth(
      monitoring: _monitoring(data['monitoringState'] as String?),
      sync: _sync(data['syncState'] as String?),
      observedAt: DateTime.parse(data['observedAt'] as String).toUtc(),
      policyVersion: data['policyVersion'] as String?,
      queuedEventCount: data['queuedEventCount'] as int? ?? 0,
    );
  }

  @override
  Future<List<ChildPolicy>> getPolicies() async {
    final response = await _deviceSend('GET', '/devices/me/policies');
    final items = response['data'] as List<dynamic>;
    return items
        .map((item) {
          final json = item as Map<String, dynamic>;
          return ChildPolicy(
            id: json['id'] as int,
            version: json['version'] as int,
            target: json['target'] as String,
            state: _policyState(json['state'] as String?),
          );
        })
        .toList(growable: false);
  }

  @override
  Future<void> clearDevice() => _store.clearProtectedState();

  MonitoringState _monitoring(String? value) => switch (value) {
    'ACTIVE' => MonitoringState.active,
    'INACTIVE' => MonitoringState.inactive,
    'PERMISSION_DENIED' => MonitoringState.permissionDenied,
    'UNSUPPORTED' => MonitoringState.unsupported,
    _ => MonitoringState.failed,
  };

  SyncState _sync(String? value) => switch (value) {
    'CURRENT' => SyncState.current,
    'STALE' => SyncState.stale,
    'OFFLINE' => SyncState.offline,
    _ => SyncState.failed,
  };

  ChildPolicyState _policyState(String? value) => switch (value) {
    'PENDING' => ChildPolicyState.pending,
    'ACTIVE' => ChildPolicyState.active,
    'UNSUPPORTED' => ChildPolicyState.unsupported,
    _ => ChildPolicyState.failed,
  };
}
