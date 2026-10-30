import 'package:flutter/foundation.dart';

import '../../../../core/network/api_client.dart';
import '../../../../data/repositories/child/child_repository.dart';
import '../../../../model/child_models.dart';
import '../../../../services/child_runtime.dart';
import '../../../../services/device_identity.dart';

class ChildViewModel extends ChangeNotifier {
  ChildViewModel(
    this._repository,
    this._store, {
    DeviceIdentityProvider? identityProvider,
    ChildRuntimeAdapter? runtimeAdapter,
  }) : _identityProvider = identityProvider ?? AndroidDeviceIdentityProvider(),
       _runtimeAdapter = runtimeAdapter ?? AndroidChildRuntimeAdapter();
  final ChildRepository _repository;
  final ChildDeviceStore _store;
  final DeviceIdentityProvider _identityProvider;
  final ChildRuntimeAdapter _runtimeAdapter;
  PairingState pairingState = PairingState.idle;
  String? pairingError;
  ChildStatusState state = const ChildStatusState();
  ChildRuntimeStatus? runtimeStatus;
  bool runtimeLoading = false;

  Future<ChildDeviceContext?> readStoredDevice() => _store.readDevice();

  Future<void> refreshRuntimeStatus() async {
    runtimeLoading = true;
    notifyListeners();
    try {
      runtimeStatus = await _runtimeAdapter.readStatus();
    } finally {
      runtimeLoading = false;
      notifyListeners();
    }
  }

  Future<void> requestPermission(ChildPermissionKind kind) async {
    await _runtimeAdapter.requestPermission(kind);
    await refreshRuntimeStatus();
  }

  Future<void> startMonitoring() async {
    runtimeLoading = true;
    notifyListeners();
    try {
      runtimeStatus = await _runtimeAdapter.startMonitoring();
    } finally {
      runtimeLoading = false;
      notifyListeners();
    }
  }

  Future<void> pair({
    required String code,
    required String appVersion,
    required String displayName,
  }) async {
    pairingError = null;
    pairingState = PairingState.submitting;
    notifyListeners();
    try {
      final identity = await _identityProvider.readIdentity();
      state = await _repository.completePairing(
        pairingCode: code,
        deviceIdentifier: identity.identifier,
        platform: identity.platform,
        appVersion: appVersion,
        displayName: displayName,
      );
      pairingState = PairingState.success;
    } on ApiException catch (error) {
      pairingState = _pairingState(error);
      pairingError = error.message;
    } catch (error) {
      pairingState = PairingState.failed;
      pairingError = error.toString();
    }
    notifyListeners();
  }

  Future<void> confirmConsent(String version) async {
    try {
      final consent = await _repository.confirmDisclosure(version: version);
      state = state.copyWith(
        consent: consent,
        status: ChildLoadStatus.ready,
        error: null,
      );
    } on ApiException catch (error) {
      state = state.copyWith(status: _status(error), error: error.message);
    }
    notifyListeners();
  }

  Future<void> loadStatus() async {
    final stored = await _store.readLastKnownState();
    final device = await _store.readDevice();
    state = state.copyWith(
      status: ChildLoadStatus.loading,
      device: device,
      health: stored?.health,
      policies: stored?.policies ?? const [],
      consent: stored?.consent,
      isLastKnown: stored != null,
      error: null,
    );
    notifyListeners();
    try {
      final health = await _repository.getHealth();
      final policies = await _repository.getPolicies();
      state = state.copyWith(
        status: ChildLoadStatus.ready,
        device: device,
        health: health,
        policies: policies,
        isLastKnown: false,
        error: null,
      );
      await _store.writeLastKnownState(state);
    } on ApiException catch (error) {
      final revoked = _isRevoked(error);
      if (revoked) await _repository.clearDevice();
      state = state.copyWith(
        status: revoked ? ChildLoadStatus.revoked : _status(error),
        isLastKnown: stored != null,
        error: error.message,
        clearDevice: revoked,
      );
    } catch (error) {
      state = state.copyWith(
        status: ChildLoadStatus.failed,
        isLastKnown: stored != null,
        error: error.toString(),
      );
    }
    notifyListeners();
  }

  Future<void> recoverExpiredSession() async {
    if (state.status == ChildLoadStatus.unauthorized) await loadStatus();
  }

  Future<void> clearPairing() async {
    await _repository.clearDevice();
    state = const ChildStatusState();
    pairingState = PairingState.idle;
    notifyListeners();
  }

  PairingState _pairingState(ApiException error) {
    if (error.isOffline) return PairingState.offline;
    return switch (error.code?.toUpperCase()) {
      'PAIRING_CODE_INVALID' => PairingState.invalid,
      'PAIRING_CODE_EXPIRED' => PairingState.expired,
      'PAIRING_CODE_USED' => PairingState.used,
      _ => PairingState.failed,
    };
  }

  ChildLoadStatus _status(ApiException error) => error.isOffline
      ? ChildLoadStatus.offline
      : error.isUnauthorized
      ? ChildLoadStatus.unauthorized
      : ChildLoadStatus.failed;

  bool _isRevoked(ApiException error) =>
      error.isUnauthorized && error.code?.toUpperCase() != 'SESSION_EXPIRED';
}

class PermissionGuidanceViewModel extends ChangeNotifier {
  PermissionGuidanceViewModel(this._readStatus, this._requestPermission);
  final Future<PermissionState> Function() _readStatus;
  final Future<PermissionState> Function() _requestPermission;
  PermissionState state = PermissionState.unknown;
  String? error;

  Future<void> refresh() async {
    try {
      state = await _readStatus();
      error = null;
    } catch (value) {
      state = PermissionState.unavailable;
      error = value.toString();
    }
    notifyListeners();
  }

  Future<void> request() async {
    try {
      state = await _requestPermission();
      error = null;
    } catch (value) {
      state = PermissionState.unavailable;
      error = value.toString();
    }
    notifyListeners();
  }
}
