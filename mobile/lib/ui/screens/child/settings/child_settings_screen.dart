import 'package:flutter/material.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/status_card.dart';
import '../../../../core/widgets/status_tone.dart';
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
            StatusCard(
              icon: Icons.verified_user_outlined,
              label: 'Protection status',
              value: _protectionValue(state.health),
              tone: _protectionTone(state.health),
            ),
            const SizedBox(height: AppSpacing.small),
            StatusCard(
              icon: Icons.sync,
              label: 'Last sync',
              value: _syncValue(state.health),
              tone: _syncTone(state.health),
            ),
            const SizedBox(height: AppSpacing.small),
            StatusCard(
              icon: Icons.security,
              label: 'Permissions',
              value: _permissionValue(state.permission),
              tone: state.permission == PermissionState.granted
                  ? StatusTone.success
                  : StatusTone.warning,
            ),
            const SizedBox(height: AppSpacing.small),
            StatusCard(
              icon: Icons.phone_android,
              label: 'Device info',
              value: device?.platform ?? 'Unavailable',
            ),
            const SizedBox(height: 20),
            const Text('Support', style: AppTextStyles.title),
            const StatusCard(
              icon: Icons.help_outline,
              label: 'Help and privacy',
              value: 'Learn about protection',
            ),
            const SizedBox(height: AppSpacing.small),
            const StatusCard(
              icon: Icons.info_outline,
              label: 'About Tam Dan',
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
  static StatusTone _protectionTone(ChildHealth? health) =>
      health?.monitoring == MonitoringState.active
      ? StatusTone.success
      : StatusTone.warning;

  static StatusTone _syncTone(ChildHealth? health) =>
      health?.sync == SyncState.current
      ? StatusTone.success
      : StatusTone.warning;
}
