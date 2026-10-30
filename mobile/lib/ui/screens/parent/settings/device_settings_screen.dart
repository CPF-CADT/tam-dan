import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../../../widgets/bottom_navigation.dart';
import '../controls/parent_ui_tokens.dart';
import '../view_model/parent_view_models.dart';

class DeviceSettingsScreen extends StatelessWidget {
  const DeviceSettingsScreen({
    super.key,
    required this.child,
    required this.devices,
    required this.onLogout,
    this.onNavigate,
    this.showBottomNavigation = true,
  });
  final Child child;
  final DeviceViewModel devices;
  final VoidCallback onLogout;
  final ValueChanged<int>? onNavigate;
  final bool showBottomNavigation;

  @override
  Widget build(BuildContext context) {
    if (devices.state.status == LoadStatus.idle) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        devices.load(child.id);
      });
    }
    final values = devices.state.value ?? const <Device>[];
    final device = values.where((item) => item.childId == child.id).firstOrNull;
    return Scaffold(
      backgroundColor: ParentUiTokens.canvas,
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
          children: [
            const Text('Settings', style: ParentUiTokens.title),
            Text('${child.displayName}’s device', style: ParentUiTokens.body),
            const SizedBox(height: 18),
            _SettingRow(
              icon: Icons.verified_user_outlined,
              title: 'Protection status',
              subtitle: device?.status == DeviceStatus.active
                  ? 'Active'
                  : 'Not confirmed by device',
              color: device?.status == DeviceStatus.active
                  ? ParentUiTokens.success
                  : ParentUiTokens.warning,
            ),
            _SettingRow(
              icon: Icons.sync,
              title: 'Last sync',
              subtitle: device == null
                  ? 'Unavailable'
                  : 'Use device status for current sync',
              color: ParentUiTokens.primary,
            ),
            _SettingRow(
              icon: Icons.security_outlined,
              title: 'Permissions',
              subtitle: 'Parent cannot inspect child platform permissions here',
              color: ParentUiTokens.warning,
            ),
            _SettingRow(
              icon: Icons.phone_android_outlined,
              title: 'Device info',
              subtitle: device?.platform ?? 'Unavailable',
              color: ParentUiTokens.primary,
            ),
            const SizedBox(height: 20),
            const Text('Parent account', style: ParentUiTokens.section),
            _SettingRow(
              icon: Icons.family_restroom,
              title: 'Family members',
              subtitle: '1 child connected',
              color: ParentUiTokens.primary,
            ),
            _SettingRow(
              icon: Icons.notifications_none,
              title: 'Notifications',
              subtitle: 'Managed by your device settings',
              color: ParentUiTokens.primary,
            ),
            _SettingRow(
              icon: Icons.privacy_tip_outlined,
              title: 'Privacy',
              subtitle: 'Review data and alert handling',
              color: ParentUiTokens.primary,
            ),
            _SettingRow(
              icon: Icons.help_outline,
              title: 'Support',
              subtitle: 'Help is not connected in this build',
              color: ParentUiTokens.muted,
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: onLogout,
              icon: const Icon(Icons.logout),
              label: const Text('Sign out'),
            ),
          ],
        ),
      ),
      bottomNavigationBar: showBottomNavigation
          ? TamDanBottomNavigation(
              currentIndex: 3,
              onDestinationSelected: onNavigate ?? (_) {},
            )
          : null,
    );
  }
}

class _SettingRow extends StatelessWidget {
  const _SettingRow({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.color,
  });
  final IconData icon;
  final String title;
  final String subtitle;
  final Color color;
  @override
  Widget build(BuildContext context) => Card(
    elevation: 0,
    margin: const EdgeInsets.only(bottom: 8),
    child: ListTile(
      leading: CircleAvatar(
        backgroundColor: color.withValues(alpha: .12),
        child: Icon(icon, color: color, size: 20),
      ),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
      subtitle: Text(subtitle, style: ParentUiTokens.body),
      trailing: const Icon(Icons.chevron_right, color: ParentUiTokens.muted),
    ),
  );
}
