import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/material.dart';

import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/storage/secure_session_store.dart';
import 'package:mobile/data/repositories/parent_repository.dart';
import 'package:mobile/main.dart';

class _FakeClient implements ApiClient {
  @override
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  }) async {
    if (path == '/auth/login') {
      return {
        'data': {'accessToken': 'access', 'refreshToken': 'refresh'},
      };
    }
    if (path == '/families/me/children') {
      return {
        'data': [
          {'id': 1, 'displayName': 'Alex', 'status': 'ACTIVE'},
        ],
      };
    }
    if (path == '/families/me/children/1/devices') return {'data': []};
    if (path.contains('/reports/usage')) {
      final now = DateTime.now().toUtc().toIso8601String();
      return {
        'data': {
          'childId': 1,
          'from': now,
          'to': now,
          'generatedAt': now,
          'dataThrough': now,
          'freshness': 'FRESH',
          'completeness': 'COMPLETE',
          'sourceDeviceCoverage': [],
          'risk': 'UNKNOWN',
        },
      };
    }
    if (path.contains('/alerts')) {
      return {
        'data': {'items': [], 'page': 0, 'pageSize': 20, 'total': 0},
      };
    }
    if (path.contains('/policies')) {
      return {
        'data': {'items': [], 'page': 0, 'pageSize': 20, 'total': 0},
      };
    }
    return {'data': {}};
  }
}

void main() {
  testWidgets(
    'parent authentication and family dashboard render through ViewModels',
    (tester) async {
      final store = MemorySecureSessionStore();
      final repository = HttpParentRepository(_FakeClient(), store);
      await tester.pumpWidget(TamDanApp(repository: repository));
      await tester.enterText(find.byType(TextField).at(0), 'parent');
      await tester.enterText(find.byType(TextField).at(1), 'password');
      await tester.tap(find.text('Sign in'));
      await tester.pumpAndSettle();
      expect(find.text('Alex'), findsOneWidget);
      expect(find.text('Devices'), findsOneWidget);
    },
  );
}
