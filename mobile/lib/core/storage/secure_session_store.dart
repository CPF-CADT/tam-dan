import 'package:flutter/services.dart';

abstract interface class SecureSessionStore {
  Future<String?> readAccessToken();
  Future<String?> readRefreshToken();
  Future<void> writeTokens({
    required String accessToken,
    required String refreshToken,
  });
  Future<void> clear();
}

class PlatformSecureSessionStore implements SecureSessionStore {
  PlatformSecureSessionStore({MethodChannel? channel})
    : _channel = channel ?? const MethodChannel(_channelName);

  static const _channelName = 'com.tamdan.mobile/android_runtime';
  final MethodChannel _channel;

  @override
  Future<String?> readAccessToken() async {
    final value = await _channel.invokeMethod<Object?>('readSession');
    return value is Map ? value['accessToken'] as String? : null;
  }

  @override
  Future<String?> readRefreshToken() async {
    final value = await _channel.invokeMethod<Object?>('readSession');
    return value is Map ? value['refreshToken'] as String? : null;
  }

  @override
  Future<void> writeTokens({
    required String accessToken,
    required String refreshToken,
  }) => _channel.invokeMethod<void>('writeSession', {
    'accessToken': accessToken,
    'refreshToken': refreshToken,
  });

  @override
  Future<void> clear() => _channel.invokeMethod<void>('clearSession');
}

/// In-memory storage is test-only; production composition uses the platform adapter.
class MemorySecureSessionStore implements SecureSessionStore {
  String? _accessToken;
  String? _refreshToken;

  @override
  Future<void> clear() async {
    _accessToken = null;
    _refreshToken = null;
  }

  @override
  Future<String?> readAccessToken() async => _accessToken;

  @override
  Future<String?> readRefreshToken() async => _refreshToken;

  @override
  Future<void> writeTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    _accessToken = accessToken;
    _refreshToken = refreshToken;
  }
}
