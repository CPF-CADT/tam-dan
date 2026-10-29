import 'package:flutter/services.dart';

class DeviceIdentity {
  const DeviceIdentity({required this.identifier, required this.platform});

  final String identifier;
  final String platform;

  static DeviceIdentity fromPlatformResult(Object? value) {
    if (value is! Map) {
      throw StateError('Platform did not provide device identity');
    }
    final identifier = value['identifier'];
    final platform = value['platform'];
    if (identifier is! String || platform is! String) {
      throw StateError('Platform device identity is incomplete');
    }
    final normalizedIdentifier = identifier.trim();
    final normalizedPlatform = platform.trim().toUpperCase();
    if (!_isValidIdentifier(normalizedIdentifier) ||
        normalizedPlatform.isEmpty ||
        normalizedPlatform.length > 30) {
      throw StateError('Platform device identity is invalid');
    }
    return DeviceIdentity(
      identifier: normalizedIdentifier,
      platform: normalizedPlatform,
    );
  }

  static bool _isValidIdentifier(String value) =>
      value.isNotEmpty &&
      value.length <= 255 &&
      !RegExp(r'[\u0000-\u001f\u007f]').hasMatch(value) &&
      !{
        'flutter-device',
        'unknown',
        'test-device',
      }.contains(value.toLowerCase());
}

abstract interface class DeviceIdentityProvider {
  Future<DeviceIdentity> readIdentity();
}

class AndroidDeviceIdentityProvider implements DeviceIdentityProvider {
  AndroidDeviceIdentityProvider({MethodChannel? channel})
    : _channel = channel ?? const MethodChannel(_channelName);

  static const _channelName = 'com.tamdan.mobile/android_runtime';
  final MethodChannel _channel;

  @override
  Future<DeviceIdentity> readIdentity() async =>
      DeviceIdentity.fromPlatformResult(
        await _channel.invokeMethod<Object?>('getDeviceIdentity'),
      );
}
