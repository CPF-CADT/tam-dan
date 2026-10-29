import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/features/child/child_models.dart';
import 'package:mobile/features/child/child_repository.dart';
import 'package:mobile/features/child/child_view_model.dart';
import 'package:mobile/features/child/device_identity.dart';

class _TestDeviceIdentityProvider implements DeviceIdentityProvider {
  const _TestDeviceIdentityProvider();

  @override
  Future<DeviceIdentity> readIdentity() async =>
      const DeviceIdentity(identifier: 'test-device-42', platform: 'ANDROID');
}

class _ChildRepository implements ChildRepository {
  ApiException? pairingError;
  ApiException? statusError;
  String? pairedIdentifier;
  String? pairedPlatform;
  final store = MemoryChildDeviceStore();
  final device = const ChildDeviceContext(
    deviceId: 7,
    childId: 3,
    displayName: 'Tablet',
    platform: 'ANDROID',
  );
  final health = ChildHealth(
    monitoring: MonitoringState.active,
    sync: SyncState.current,
    observedAt: DateTime.utc(2026, 1, 1),
  );

  @override
  Future<ChildStatusState> completePairing({
    required String pairingCode,
    required String deviceIdentifier,
    required String platform,
    required String appVersion,
    required String displayName,
  }) async {
    pairedIdentifier = deviceIdentifier;
    pairedPlatform = platform;
    if (pairingError != null) throw pairingError!;
    await store.writePairing(credential: 'device-secret', device: device);
    return ChildStatusState(status: ChildLoadStatus.ready, device: device);
  }

  @override
  Future<ChildConsent> confirmDisclosure({required String version}) async =>
      ChildConsent(
        version: version,
        decision: ConsentDecision.granted,
        decidedAt: DateTime.utc(2026, 1, 1),
        confirmedByDevice: true,
      );

  @override
  Future<ChildHealth> getHealth() async {
    if (statusError != null) throw statusError!;
    return health;
  }

  @override
  Future<List<ChildPolicy>> getPolicies() async => const [
    ChildPolicy(
      id: 1,
      version: 2,
      target: 'example.com',
      state: ChildPolicyState.active,
    ),
  ];

  @override
  Future<void> clearDevice() => store.clearProtectedState();
}

void main() {
  test('rejects the legacy placeholder from a platform identity result', () {
    expect(
      () => DeviceIdentity.fromPlatformResult({
        'identifier': 'flutter-device',
        'platform': 'ANDROID',
      }),
      throwsStateError,
    );
  });

  test(
    'maps invalid, expired, used, offline, and success pairing states',
    () async {
      for (final entry in <String, PairingState>{
        'PAIRING_CODE_INVALID': PairingState.invalid,
        'PAIRING_CODE_EXPIRED': PairingState.expired,
        'PAIRING_CODE_USED': PairingState.used,
      }.entries) {
        final repository = _ChildRepository()
          ..pairingError = ApiException(400, entry.key, code: entry.key);
        final viewModel = ChildViewModel(
          repository,
          repository.store,
          identityProvider: const _TestDeviceIdentityProvider(),
        );
        await viewModel.pair(
          code: 'code',
          appVersion: '1',
          displayName: 'Tablet',
        );
        expect(viewModel.pairingState, entry.value);
      }
      final offline = _ChildRepository()
        ..pairingError = const ApiException(0, 'offline');
      final offlineModel = ChildViewModel(
        offline,
        offline.store,
        identityProvider: const _TestDeviceIdentityProvider(),
      );
      await offlineModel.pair(
        code: 'code',
        appVersion: '1',
        displayName: 'Tablet',
      );
      expect(offlineModel.pairingState, PairingState.offline);
      final success = _ChildRepository();
      final successModel = ChildViewModel(
        success,
        success.store,
        identityProvider: const _TestDeviceIdentityProvider(),
      );
      await successModel.pair(
        code: 'code',
        appVersion: '1',
        displayName: 'Tablet',
      );
      expect(successModel.pairingState, PairingState.success);
      expect(await success.store.readCredential(), 'device-secret');
      expect(success.pairedIdentifier, 'test-device-42');
      expect(success.pairedIdentifier, isNot('flutter-device'));
      expect(success.pairedPlatform, 'ANDROID');
    },
  );

  test('loads status and retains last-known state while offline', () async {
    final repository = _ChildRepository();
    final viewModel = ChildViewModel(
      repository,
      repository.store,
      identityProvider: const _TestDeviceIdentityProvider(),
    );
    await viewModel.pair(code: 'code', appVersion: '1', displayName: 'Tablet');
    await viewModel.loadStatus();
    expect(viewModel.state.status, ChildLoadStatus.ready);
    expect(viewModel.state.policies.single.target, 'example.com');
    repository.statusError = const ApiException(0, 'offline');
    await viewModel.loadStatus();
    expect(viewModel.state.status, ChildLoadStatus.offline);
    expect(viewModel.state.isLastKnown, isTrue);
    expect(viewModel.state.health?.monitoring, MonitoringState.active);
  });

  test('clears protected device state on revoked credential', () async {
    final repository = _ChildRepository();
    final viewModel = ChildViewModel(
      repository,
      repository.store,
      identityProvider: const _TestDeviceIdentityProvider(),
    );
    await viewModel.pair(code: 'code', appVersion: '1', displayName: 'Tablet');
    repository.statusError = const ApiException(401, 'revoked');
    await viewModel.loadStatus();
    expect(viewModel.state.status, ChildLoadStatus.revoked);
    expect(await repository.store.readCredential(), isNull);
    expect(viewModel.state.device, isNull);
  });

  test(
    'expired session remains recoverable without deleting device context',
    () async {
      final repository = _ChildRepository();
      final viewModel = ChildViewModel(
        repository,
        repository.store,
        identityProvider: const _TestDeviceIdentityProvider(),
      );
      await viewModel.pair(
        code: 'code',
        appVersion: '1',
        displayName: 'Tablet',
      );
      repository.statusError = const ApiException(
        401,
        'session expired',
        code: 'SESSION_EXPIRED',
      );
      await viewModel.loadStatus();
      expect(viewModel.state.status, ChildLoadStatus.unauthorized);
      expect(await repository.store.readCredential(), 'device-secret');
      expect(viewModel.state.device, isNotNull);
    },
  );

  test('permission guidance exposes denied and granted states', () async {
    var permission = PermissionState.denied;
    final viewModel = PermissionGuidanceViewModel(
      () async => permission,
      () async {
        permission = PermissionState.granted;
        return permission;
      },
    );
    await viewModel.refresh();
    expect(viewModel.state, PermissionState.denied);
    await viewModel.request();
    expect(viewModel.state, PermissionState.granted);
  });
}
