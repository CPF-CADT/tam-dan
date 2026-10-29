import 'package:flutter/material.dart';

import 'core/network/api_client.dart';
import 'core/storage/secure_session_store.dart';
import 'core/theme/app_theme.dart';
import 'data/repositories/parent_repository.dart';
import 'features/child/child_repository.dart';
import 'features/child/child_secure_store.dart';
import 'features/child/child_screens.dart';
import 'features/child/child_view_model.dart';
import 'features/child/device_identity.dart';
import 'ui/parent/parent_screens.dart';
import 'ui/parent/parent_view_models.dart';

void main() {
  const baseUrl = String.fromEnvironment(
    'TAMDAN_API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080/api/v1',
  );
  final composition = TamDanComposition.production(Uri.parse(baseUrl));
  const childMode =
      String.fromEnvironment('TAMDAN_APP_ROLE', defaultValue: 'parent') ==
      'child';
  runApp(
    TamDanApp(
      repository: composition.parentRepository,
      childMode: childMode,
      childRepository: composition.childRepository,
      childStore: composition.childStore,
    ),
  );
}

class TamDanComposition {
  const TamDanComposition({
    required this.parentRepository,
    required this.childRepository,
    required this.childStore,
  });

  final ParentRepository parentRepository;
  final ChildRepository childRepository;
  final ChildDeviceStore childStore;

  factory TamDanComposition.production(Uri baseUri) {
    final sessions = PlatformSecureSessionStore();
    final client = HttpApiClient(
      baseUri: baseUri,
      accessToken: sessions.readAccessToken,
    );
    final childStore = PlatformChildDeviceStore();
    return TamDanComposition(
      parentRepository: HttpParentRepository(client, sessions),
      childRepository: HttpChildRepository(client, childStore),
      childStore: childStore,
    );
  }
}

class TamDanApp extends StatefulWidget {
  const TamDanApp({
    super.key,
    required this.repository,
    this.childMode = false,
    this.childRepository,
    this.childStore,
    this.childIdentityProvider,
  });
  final ParentRepository repository;
  final bool childMode;
  final ChildRepository? childRepository;
  final ChildDeviceStore? childStore;
  final DeviceIdentityProvider? childIdentityProvider;
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
  late final ChildViewModel? child = widget.childMode
      ? ChildViewModel(
          widget.childRepository!,
          widget.childStore!,
          identityProvider: widget.childIdentityProvider,
        )
      : null;
  bool authenticated = false;

  @override
  void dispose() {
    auth.dispose();
    children.dispose();
    devices.dispose();
    reports.dispose();
    alerts.dispose();
    policies.dispose();
    child?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'Tam Dan',
    theme: AppTheme.data(),
    home: widget.childMode
        ? ChildApp(viewModel: child!)
        : authenticated
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

class ChildApp extends StatefulWidget {
  const ChildApp({super.key, required this.viewModel});
  final ChildViewModel viewModel;

  @override
  State<ChildApp> createState() => _ChildAppState();
}

class _ChildAppState extends State<ChildApp> {
  late final Future<bool> _hasPairing = _readPairing();

  Future<bool> _readPairing() async {
    final device = await widget.viewModel.readStoredDevice();
    if (device != null) await widget.viewModel.loadStatus();
    return device != null;
  }

  @override
  Widget build(BuildContext context) => FutureBuilder<bool>(
    future: _hasPairing,
    builder: (context, snapshot) {
      if (!snapshot.hasData) {
        return const Scaffold(body: Center(child: CircularProgressIndicator()));
      }
      return snapshot.data!
          ? ChildStatusScreen(viewModel: widget.viewModel)
          : ChildSetupScreen(viewModel: widget.viewModel);
    },
  );
}
