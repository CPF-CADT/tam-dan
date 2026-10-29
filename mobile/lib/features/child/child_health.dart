import 'package:flutter/services.dart';

import 'child_models.dart';

abstract interface class ChildHealthProvider {
  Future<ChildHealth> readHealth();
}

class AndroidChildHealthProvider implements ChildHealthProvider {
  AndroidChildHealthProvider({MethodChannel? channel})
    : _channel = channel ?? const MethodChannel(_channelName);

  static const _channelName = 'com.tamdan.mobile/android_runtime';
  final MethodChannel _channel;

  @override
  Future<ChildHealth> readHealth() async {
    try {
      final value = await _channel.invokeMethod<Object?>('getStatus');
      if (value is! Map) return ChildHealth.unsupported(DateTime.now().toUtc());
      return ChildHealth(
        monitoring: _monitoring(value['state'] as String?),
        sync: _sync(value['sync'] as String?),
        observedAt: DateTime.now().toUtc(),
      );
    } on PlatformException {
      return ChildHealth.unsupported(DateTime.now().toUtc());
    } on MissingPluginException {
      return ChildHealth.unsupported(DateTime.now().toUtc());
    }
  }

  MonitoringState _monitoring(String? value) => switch (value) {
    'ACTIVE' => MonitoringState.active,
    'PENDING' => MonitoringState.permissionDenied,
    'STOPPED' => MonitoringState.inactive,
    'FAILED' => MonitoringState.failed,
    _ => MonitoringState.unsupported,
  };

  SyncState _sync(String? value) => switch (value) {
    'COMPLETE' => SyncState.current,
    'PENDING' || 'SYNCING' => SyncState.stale,
    'OFFLINE' => SyncState.offline,
    'FAILED' => SyncState.failed,
    _ => SyncState.unsupported,
  };
}

class UnsupportedChildHealthProvider implements ChildHealthProvider {
  const UnsupportedChildHealthProvider();

  @override
  Future<ChildHealth> readHealth() async =>
      ChildHealth.unsupported(DateTime.now().toUtc());
}
