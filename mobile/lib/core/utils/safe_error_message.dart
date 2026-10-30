import '../network/api_client.dart';

/// Converts internal, backend, and platform failures into bounded UI text.
String safeErrorMessage(Object error) {
  if (error is ApiException) {
    if (error.isOffline) {
      return 'You are offline. Reconnect and try again.';
    }
    if (error.isUnauthorized) {
      return 'Your session has expired. Pair this device again.';
    }
    return switch (error.code?.toUpperCase()) {
      'PAIRING_CODE_INVALID' => 'This pairing code is invalid.',
      'PAIRING_CODE_EXPIRED' => 'This pairing code has expired.',
      'PAIRING_CODE_USED' => 'This pairing code was already used.',
      _ => 'Something went wrong. Please try again.',
    };
  }
  return 'Something went wrong. Please try again.';
}
