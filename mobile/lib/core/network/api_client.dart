import 'dart:async';
import 'dart:convert';
import 'dart:io';

class ApiException implements Exception {
  const ApiException(this.statusCode, this.message, {this.code});

  final int statusCode;
  final String message;
  final String? code;

  bool get isUnauthorized => statusCode == 401;
  bool get isOffline => statusCode == 0;

  @override
  String toString() => 'ApiException($statusCode, $message)';
}

abstract interface class ApiClient {
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  });
}

class HttpApiClient implements ApiClient {
  HttpApiClient({required this._baseUri, required this._accessToken});

  final Uri _baseUri;
  final Future<String?> Function() _accessToken;

  @override
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  }) async {
    final uri = _baseUri.replace(
      path: '${_baseUri.path.replaceFirst(RegExp(r'\/$'), '')}$path',
      queryParameters: query?.map((key, value) => MapEntry(key, '$value')),
    );
    final client = HttpClient();
    try {
      final request = await client
          .openUrl(method, uri)
          .timeout(const Duration(seconds: 15));
      request.headers.contentType = ContentType.json;
      final token = await _accessToken();
      if (token != null && token.isNotEmpty) {
        request.headers.set(HttpHeaders.authorizationHeader, 'Bearer $token');
      }
      if (body != null) request.write(jsonEncode(body));
      final response = await request.close().timeout(
        const Duration(seconds: 15),
      );
      final text = await response.transform(utf8.decoder).join();
      final decoded = text.isEmpty ? <String, dynamic>{} : jsonDecode(text);
      if (response.statusCode < 200 || response.statusCode >= 300) {
        final problem = decoded is Map<String, dynamic>
            ? decoded
            : <String, dynamic>{};
        throw ApiException(
          response.statusCode,
          problem['detail'] as String? ??
              problem['title'] as String? ??
              'Request failed',
          code: problem['code'] as String?,
        );
      }
      if (decoded is! Map<String, dynamic>) {
        throw const ApiException(0, 'Unexpected response shape');
      }
      return decoded;
    } on ApiException {
      rethrow;
    } on TimeoutException {
      throw const ApiException(
        0,
        'The request timed out. Check your connection.',
      );
    } on SocketException {
      throw const ApiException(0, 'You appear to be offline.');
    } on FormatException {
      throw const ApiException(0, 'The server returned an invalid response.');
    } finally {
      client.close(force: true);
    }
  }
}
