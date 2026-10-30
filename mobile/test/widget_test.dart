import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/material.dart';

import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/storage/secure_session_store.dart';
import 'package:mobile/data/repositories/parent/parent_repository.dart';
import 'package:mobile/main.dart';
import 'package:mobile/ui/screens/parent/home/parent_home_screen.dart';

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

class _RawErrorClient implements ApiClient {
  @override
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  }) async => throw const ApiException(
    500,
    'org.postgresql.util.PSQLException: SELECT * FROM parent_sessions',
  );
}

void main() {
  testWidgets('auth UI does not render raw backend or SQL errors', (
    tester,
  ) async {
    final repository = HttpParentRepository(
      _RawErrorClient(),
      MemorySecureSessionStore(),
    );
    await tester.pumpWidget(TamDanApp(repository: repository));
    await tester.enterText(find.byType(TextField).at(0), 'parent');
    await tester.enterText(find.byType(TextField).at(1), 'password');
    await tester.tap(find.text('Sign in'));
    await tester.pumpAndSettle();

    expect(find.textContaining('SELECT'), findsNothing);
    expect(find.textContaining('postgresql'), findsNothing);
    expect(
      find.text('We couldn’t sign you in. Check your details and try again.'),
      findsOneWidget,
    );
  });

  testWidgets('auth UI keeps provider actions gated and password revealable', (
    tester,
  ) async {
    final repository = HttpParentRepository(
      _FakeClient(),
      MemorySecureSessionStore(),
    );
    await tester.pumpWidget(TamDanApp(repository: repository));

    expect(find.text('Continue with Google'), findsOneWidget);
    expect(find.text('Continue with Apple'), findsOneWidget);
    expect(find.text('Social sign-in is not configured yet.'), findsOneWidget);
    expect(
      tester.widget<TextField>(find.byType(TextField).at(1)).obscureText,
      isTrue,
    );

    await tester.tap(find.byTooltip('Show password'));
    await tester.pump();
    expect(
      tester.widget<TextField>(find.byType(TextField).at(1)).obscureText,
      isFalse,
    );

    await tester.tap(find.text('Continue with Google'));
    await tester.pump();
    expect(
      find.text('Google sign-in is not configured yet.'),
      findsNWidgets(2),
    );
  });

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
      expect(find.text('Alex'), findsNWidgets(2));
      expect(find.byType(ParentHomeScreen), findsOneWidget);
    },
  );
}
