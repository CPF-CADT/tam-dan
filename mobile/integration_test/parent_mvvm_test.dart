import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:mobile/core/network/api_client.dart';
import 'package:mobile/core/storage/secure_session_store.dart';
import 'package:mobile/data/repositories/parent/parent_repository.dart';
import 'package:mobile/main.dart';
import 'package:mobile/ui/screens/parent/home/parent_home_screen.dart';

class _FakeParentClient implements ApiClient {
  @override
  Future<Map<String, dynamic>> send(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Map<String, dynamic>? body,
  }) async {
    if (path == '/auth/login') {
      return {
        'data': {
          'accessToken': 'integration-access',
          'refreshToken': 'integration-refresh',
        },
      };
    }
    if (path == '/families/me/children') {
      return {
        'data': [
          {'id': 1, 'displayName': 'Alex', 'status': 'ACTIVE'},
        ],
      };
    }
    if (path == '/families/me/children/1/devices') {
      return {'data': []};
    }
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
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('parent can sign in and load the family dashboard', (
    tester,
  ) async {
    final repository = HttpParentRepository(
      _FakeParentClient(),
      MemorySecureSessionStore(),
    );

    await tester.pumpWidget(TamDanApp(repository: repository));
    await tester.enterText(find.byType(TextField).at(0), 'parent');
    await tester.enterText(find.byType(TextField).at(1), 'password');
    await tester.tap(find.text('Sign in'));
    await tester.pumpAndSettle();

    expect(find.text('Alex'), findsNWidgets(2));
    expect(find.byType(ParentHomeScreen), findsOneWidget);
    final navigation = tester.widget<NavigationBar>(find.byType(NavigationBar));
    expect(
      navigation.destinations.cast<NavigationDestination>().map(
        (item) => item.label,
      ),
      ['Home', 'Activity', 'Controls', 'Settings'],
    );
  });
}
