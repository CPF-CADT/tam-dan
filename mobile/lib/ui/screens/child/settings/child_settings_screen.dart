import 'package:flutter/material.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../model/child_models.dart';
import '../view_model/child_view_model.dart';

class ChildSettingsScreen extends StatelessWidget {
  const ChildSettingsScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: viewModel,
    builder: (context, _) {
      final state = viewModel.state;
      final device = state.device;
      return Scaffold(
        appBar: AppBar(title: const Text('Tam Dan')),
        body: ListView(
          padding: const EdgeInsets.all(AppSpacing.gap),
          children: [
            const Text('Settings', style: AppTextStyles.title),
            const SizedBox(height: 16),
            _SettingsRow(
              icon: Icons.verified_user_outlined,
              title: 'Protection status',
              value: _protectionValue(state.health),
            ),
            _SettingsRow(
              icon: Icons.sync,
              title: 'Last sync',
              value: _syncValue(state.health),
            ),
            _SettingsRow(
              icon: Icons.security,
              title: 'Permissions',
              value: _permissionValue(state.permission),
            ),
            _SettingsRow(
              icon: Icons.phone_android,
              title: 'Device info',
              value: device?.platform ?? 'Unavailable',
            ),
            const SizedBox(height: 20),
            const Text('Support', style: AppTextStyles.title),
            const _SettingsRow(
              icon: Icons.help_outline,
              title: 'Help and privacy',
              value: 'Learn about protection',
            ),
            const _SettingsRow(
              icon: Icons.info_outline,
              title: 'About Tam Dan',
              value: 'Version ${AppConstants.childAppVersion}',
            ),
            const SizedBox(height: 8),
            OutlinedButton(
              onPressed: viewModel.loadStatus,
              child: const Text('Refresh status'),
            ),
          ],
        ),
      );
    },
  );

  static String _protectionValue(ChildHealth? health) =>
      switch (health?.monitoring) {
        MonitoringState.active => 'Active',
        MonitoringState.inactive => 'Inactive',
        MonitoringState.permissionDenied => 'Permission needed',
        MonitoringState.unsupported => 'Unsupported',
        MonitoringState.failed => 'Unavailable',
        null => 'Unavailable',
      };

  static String _syncValue(ChildHealth? health) => switch (health?.sync) {
    SyncState.current => 'Current',
    SyncState.stale => 'Stale',
    SyncState.offline => 'Offline',
    SyncState.failed => 'Unavailable',
    SyncState.unsupported => 'Unsupported',
    null => 'Unavailable',
  };

  static String _permissionValue(PermissionState permission) =>
      switch (permission) {
        PermissionState.granted => 'Granted',
        PermissionState.denied => 'Permission needed',
        PermissionState.unavailable => 'Unavailable',
        PermissionState.unknown => 'Not checked',
      };
}

class _SettingsRow extends StatelessWidget {
  const _SettingsRow({
    required this.icon,
    required this.title,
    required this.value,
  });

  final IconData icon;
  final String title;
  final String value;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      leading: Icon(icon),
      title: Text(title),
      subtitle: Text(value),
      trailing: const Icon(Icons.chevron_right),
    ),
  );
}
