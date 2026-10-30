import 'package:flutter/services.dart';

import '../model/child_models.dart';

enum ChildPermissionKind { usageAccess, drawOverApps, notifications, battery }

enum ChildCapabilityState { supported, unsupported }

class ChildRuntimeStatus {
  const ChildRuntimeStatus({
    required this.monitoring,
    required this.sync,
    required this.permission,
    required this.vpn,
  });

  final MonitoringState monitoring;
  final SyncState sync;
  final PermissionState permission;
  final ChildCapabilityState vpn;
}

abstract interface class ChildRuntimeAdapter {
  Future<ChildRuntimeStatus> readStatus();
  Future<void> requestPermission(ChildPermissionKind kind);
  Future<ChildRuntimeStatus> startMonitoring();
}

class AndroidChildRuntimeAdapter implements ChildRuntimeAdapter {
  AndroidChildRuntimeAdapter({MethodChannel? channel})
    : _channel = channel ?? const MethodChannel(_channelName);

  static const _channelName = 'com.tamdan.mobile/android_runtime';
  final MethodChannel _channel;

  @override
  Future<ChildRuntimeStatus> readStatus() async =>
      _readStatus(await _channel.invokeMethod<Object?>('getStatus'));

  @override
  Future<void> requestPermission(ChildPermissionKind kind) async {
    if (kind == ChildPermissionKind.usageAccess) {
      await _channel.invokeMethod<void>('openUsageAccessSettings');
    }
  }

  @override
  Future<ChildRuntimeStatus> startMonitoring() async =>
      _readStatus(await _channel.invokeMethod<Object?>('startMonitoring'));

  ChildRuntimeStatus _readStatus(Object? value) {
    if (value is! Map) return _unsupported();
    final capabilities = value['capabilities'];
    final vpn =
        capabilities is Map && capabilities['vpnEnforcement'] == 'SUPPORTED';
    return ChildRuntimeStatus(
      monitoring: switch (value['state'] as String?) {
        'ACTIVE' => MonitoringState.active,
        'PENDING' => MonitoringState.permissionDenied,
        'STOPPED' => MonitoringState.inactive,
        'FAILED' => MonitoringState.failed,
        _ => MonitoringState.unsupported,
      },
      sync: switch (value['sync'] as String?) {
        'COMPLETE' => SyncState.current,
        'PENDING' || 'SYNCING' => SyncState.stale,
        'OFFLINE' => SyncState.offline,
        'FAILED' => SyncState.failed,
        _ => SyncState.unsupported,
      },
      permission: value['permission'] == 'GRANTED'
          ? PermissionState.granted
          : PermissionState.denied,
      vpn: vpn
          ? ChildCapabilityState.supported
          : ChildCapabilityState.unsupported,
    );
  }

  ChildRuntimeStatus _unsupported() => const ChildRuntimeStatus(
    monitoring: MonitoringState.unsupported,
    sync: SyncState.unsupported,
    permission: PermissionState.unavailable,
    vpn: ChildCapabilityState.unsupported,
  );
}

class UnsupportedChildRuntimeAdapter implements ChildRuntimeAdapter {
  const UnsupportedChildRuntimeAdapter();

  @override
  Future<ChildRuntimeStatus> readStatus() async => const ChildRuntimeStatus(
    monitoring: MonitoringState.unsupported,
    sync: SyncState.unsupported,
    permission: PermissionState.unavailable,
    vpn: ChildCapabilityState.unsupported,
  );

  @override
  Future<void> requestPermission(ChildPermissionKind kind) async {}

  @override
  Future<ChildRuntimeStatus> startMonitoring() => readStatus();
}
