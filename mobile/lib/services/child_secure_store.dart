import 'package:flutter/services.dart';

import '../data/repositories/child/child_repository.dart';
import '../model/child_models.dart';

class PlatformChildDeviceStore implements ChildDeviceStore {
  PlatformChildDeviceStore({MethodChannel? channel})
    : _channel = channel ?? const MethodChannel(_channelName);

  static const _channelName = 'com.tamdan.mobile/android_runtime';
  final MethodChannel _channel;

  @override
  Future<String?> readCredential() async {
    final value = await _channel.invokeMethod<Object?>('readChildPairing');
    return value is Map ? value['credential'] as String? : null;
  }

  @override
  Future<ChildDeviceContext?> readDevice() async {
    final value = await _channel.invokeMethod<Object?>('readChildPairing');
    if (value is! Map) return null;
    return ChildDeviceContext(
      deviceId: value['deviceId'] as int,
      childId: value['childId'] as int,
      displayName: value['displayName'] as String,
      platform: value['platform'] as String,
    );
  }

  @override
  Future<ChildStatusState?> readLastKnownState() async {
    final value = await _channel.invokeMethod<Object?>('readChildState');
    return value is String ? ChildStatusState.fromJson(value) : null;
  }

  @override
  Future<void> writePairing({
    required String credential,
    required ChildDeviceContext device,
  }) async {
    await _channel.invokeMethod<void>('writeChildPairing', {
      'credential': credential,
      'deviceId': device.deviceId,
      'childId': device.childId,
      'displayName': device.displayName,
      'platform': device.platform,
    });
  }

  @override
  Future<void> writeLastKnownState(ChildStatusState state) =>
      _channel.invokeMethod<void>('writeChildState', state.toJson());

  @override
  Future<void> clearProtectedState() =>
      _channel.invokeMethod<void>('clearChildDeviceContext');
}
