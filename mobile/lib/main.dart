import 'package:flutter/material.dart';

import 'core/network/api_client.dart';
import 'core/storage/secure_session_store.dart';
import 'core/theme/app_theme.dart';
import 'data/repositories/parent_repository.dart';
import 'ui/parent/parent_screens.dart';
import 'ui/parent/parent_view_models.dart';

void main() {
  final sessions = MemorySecureSessionStore();
  const baseUrl = String.fromEnvironment(
    'TAMDAN_API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080/api/v1',
  );
  final client = HttpApiClient(
    baseUri: Uri.parse(baseUrl),
    accessToken: sessions.readAccessToken,
  );
  final repository = HttpParentRepository(client, sessions);
  runApp(TamDanApp(repository: repository));
}

class TamDanApp extends StatefulWidget {
  const TamDanApp({super.key, required this.repository});
  final ParentRepository repository;
  @override
  State<TamDanApp> createState() => _TamDanAppState();
}

class _TamDanAppState extends State<TamDanApp> {
  late final AuthViewModel auth = AuthViewModel(widget.repository);
  late final ChildSelectionViewModel children = ChildSelectionViewModel(
    widget.repository,
  );
  late final DeviceViewModel devices = DeviceViewModel(widget.repository);
  late final UsageReportViewModel reports = UsageReportViewModel(
    widget.repository,
  );
  late final AlertViewModel alerts = AlertViewModel(widget.repository);
  late final PolicyViewModel policies = PolicyViewModel(widget.repository);
  bool authenticated = false;

  @override
  void dispose() {
    auth.dispose();
    children.dispose();
    devices.dispose();
    reports.dispose();
    alerts.dispose();
    policies.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'Tam Dan',
    theme: AppTheme.data(),
    home: authenticated
        ? ParentShell(
            children: children,
            devices: devices,
            reports: reports,
            alerts: alerts,
            policies: policies,
            onLogout: () async {
              await auth.logout();
              if (mounted) setState(() => authenticated = false);
            },
          )
        : AuthScreen(
            viewModel: auth,
            onAuthenticated: () => setState(() => authenticated = true),
          ),
  );
}
