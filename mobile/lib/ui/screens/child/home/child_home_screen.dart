import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../model/child_models.dart';
import '../../../../services/child_runtime.dart';
import '../view_model/child_view_model.dart';

class ChildHomeScreen extends StatefulWidget {
  const ChildHomeScreen({super.key, required this.viewModel, this.onNavigate});

  final ChildViewModel viewModel;
  final ValueChanged<int>? onNavigate;

  @override
  State<ChildHomeScreen> createState() => _ChildHomeScreenState();
}

class _ChildHomeScreenState extends State<ChildHomeScreen> {
  @override
  void initState() {
    super.initState();
    widget.viewModel.refreshRuntimeStatus();
  }

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: widget.viewModel,
    builder: (context, _) {
      final runtime = widget.viewModel.runtimeStatus;
      final health = widget.viewModel.state.health;
      final monitoring = runtime?.monitoring ?? health?.monitoring;
      final sync = runtime?.sync ?? health?.sync;
      final permission =
          runtime?.permission ?? widget.viewModel.state.permission;
      final vpn = runtime?.vpn ?? ChildCapabilityState.unsupported;
      return Scaffold(
        backgroundColor: AppColors.canvas,
        body: SafeArea(
          child: Column(
            children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.fromLTRB(
                    AppSpacing.screen,
                    AppSpacing.small,
                    AppSpacing.screen,
                    AppSpacing.gap,
                  ),
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text('Tam Dan', style: AppTextStyles.heading),
                            Text(
                              'Child online safety',
                              style: AppTextStyles.label,
                            ),
                          ],
                        ),
                        const Icon(Icons.settings_outlined),
                      ],
                    ),
                    const SizedBox(height: 28),
                    _ProtectionHero(
                      active: monitoring == MonitoringState.active,
                    ),
                    const SizedBox(height: 24),
                    _StatusTile(
                      icon: Icons.check,
                      title: 'Monitoring permissions',
                      value: _permissionLabel(permission),
                      state: permission == PermissionState.granted,
                    ),
                    _StatusTile(
                      icon: Icons.vpn_key_outlined,
                      title: 'Local VPN',
                      value: vpn == ChildCapabilityState.supported
                          ? 'Connected'
                          : 'Unsupported on this device',
                      state: vpn == ChildCapabilityState.supported,
                    ),
                    _StatusTile(
                      icon: Icons.sync,
                      title: 'Policy sync',
                      value: _syncLabel(sync),
                      state: sync == SyncState.current,
                    ),
                    if (widget.viewModel.state.error != null)
                      Padding(
                        padding: const EdgeInsets.only(top: AppSpacing.gap),
                        child: Text(
                          'Status unavailable: ${widget.viewModel.state.error}',
                          style: TextStyle(
                            color: Theme.of(context).colorScheme.error,
                          ),
                        ),
                      ),
                    const SizedBox(height: AppSpacing.gap),
                    const Text('Quick access', style: AppTextStyles.title),
                    const SizedBox(height: AppSpacing.small),
                    _HomeCard(
                      icon: Icons.view_list_outlined,
                      title: 'Activity',
                      subtitle: 'See your activity status',
                      onTap: () => widget.onNavigate?.call(1),
                    ),
                    _HomeCard(
                      icon: Icons.grid_view_outlined,
                      title: 'Restrictions',
                      subtitle: 'View your current restrictions',
                      onTap: () => widget.onNavigate?.call(2),
                    ),
                    _HomeCard(
                      icon: Icons.settings_outlined,
                      title: 'Settings',
                      subtitle: 'Manage protection and device settings',
                      onTap: () => widget.onNavigate?.call(3),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      );
    },
  );

  String _permissionLabel(PermissionState value) => switch (value) {
    PermissionState.granted => 'Granted',
    PermissionState.denied => 'Required permissions are missing',
    PermissionState.unavailable => 'Unavailable on this device',
    PermissionState.unknown => 'Checking…',
  };

  String _syncLabel(SyncState? value) => switch (value) {
    SyncState.current => 'Up to date',
    SyncState.stale => 'Syncing',
    SyncState.offline => 'Offline',
    SyncState.failed => 'Needs attention',
    SyncState.unsupported => 'Unavailable',
    _ => 'Checking…',
  };
}

class _HomeCard extends StatelessWidget {
  const _HomeCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      leading: Icon(icon, color: AppColors.primary),
      title: Text(title, style: AppTextStyles.title),
      subtitle: Text(subtitle, style: AppTextStyles.label),
      trailing: const Icon(Icons.chevron_right),
      onTap: onTap,
    ),
  );
}

class _ProtectionHero extends StatelessWidget {
  const _ProtectionHero({required this.active});
  final bool active;

  @override
  Widget build(BuildContext context) => Column(
    children: [
      Container(
        width: 150,
        height: 150,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: LinearGradient(
            colors: active
                ? [AppColors.paleMint, Colors.white]
                : [AppColors.paleBlue, Colors.white],
          ),
        ),
        child: Center(
          child: Icon(
            active ? Icons.check : Icons.shield_outlined,
            color: AppColors.primary,
            size: 42,
          ),
        ),
      ),
      const SizedBox(height: 22),
      Text(
        active ? 'Protection Active' : 'Protection needs attention',
        style: AppTextStyles.heading,
      ),
      const SizedBox(height: AppSpacing.small),
      Text(
        active
            ? 'Your device is protected and connected to your parent’s account.'
            : 'Monitoring is not confirmed until required permissions are granted.',
        textAlign: TextAlign.center,
        style: AppTextStyles.label,
      ),
    ],
  );
}

class _StatusTile extends StatelessWidget {
  const _StatusTile({
    required this.icon,
    required this.title,
    required this.value,
    required this.state,
  });
  final IconData icon;
  final String title;
  final String value;
  final bool state;

  @override
  Widget build(BuildContext context) => Container(
    height: 68,
    margin: const EdgeInsets.only(bottom: 8),
    padding: const EdgeInsets.symmetric(horizontal: 14),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(17),
    ),
    child: Row(
      children: [
        CircleAvatar(
          radius: 20,
          backgroundColor: state
              ? AppColors.paleMint
              : AppColors.warningSurface,
          child: Icon(
            icon,
            color: state ? AppColors.success : AppColors.warning,
            size: 19,
          ),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: AppTextStyles.title.copyWith(fontSize: 14)),
              Text(value, style: AppTextStyles.label.copyWith(fontSize: 11)),
            ],
          ),
        ),
        Icon(Icons.chevron_right, color: AppColors.muted),
      ],
    ),
  );
}
