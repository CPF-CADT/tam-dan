import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../home/parent_visual_tokens.dart';
import '../view_model/parent_view_models.dart';

class ChildOverviewScreen extends StatefulWidget {
  const ChildOverviewScreen({
    super.key,
    required this.child,
    required this.devices,
    required this.reports,
    required this.policies,
    required this.onActivity,
    required this.onControls,
  });
  final Child child;
  final DeviceViewModel devices;
  final UsageReportViewModel reports;
  final PolicyViewModel policies;
  final VoidCallback onActivity;
  final VoidCallback onControls;

  @override
  State<ChildOverviewScreen> createState() => _ChildOverviewScreenState();
}

class _ChildOverviewScreenState extends State<ChildOverviewScreen> {
  @override
  void initState() {
    super.initState();
    widget.devices.load(widget.child.id);
    widget.policies.load(widget.child.id);
  }

  @override
  Widget build(BuildContext context) {
    final device = widget.devices.state.value?.firstOrNull;
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
      children: [
        Row(
          children: [
            IconButton(
              onPressed: widget.onActivity,
              icon: const Icon(Icons.chevron_left),
              tooltip: 'Back',
            ),
            Expanded(
              child: Text(
                '${widget.child.displayName}’s device',
                style: const TextStyle(
                  fontSize: 23,
                  fontWeight: FontWeight.w800,
                  color: ParentVisualTokens.ink,
                ),
              ),
            ),
          ],
        ),
        Text(
          device?.platform == null
              ? 'Device information unavailable'
              : '${device!.displayName} • ${device.platform}',
          style: const TextStyle(color: ParentVisualTokens.muted, fontSize: 12),
        ),
        const SizedBox(height: 20),
        Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: ParentVisualTokens.paleBlue,
            borderRadius: BorderRadius.circular(24),
          ),
          child: Column(
            children: [
              const _LargeStatus(icon: Icons.check, label: 'Protected now'),
              const SizedBox(height: 14),
              Text(
                device == null
                    ? 'Device status is unavailable'
                    : device.status.name,
                style: const TextStyle(
                  color: ParentVisualTokens.primary,
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 22),
        const Text(
          'Quick view',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: ParentVisualTokens.ink,
          ),
        ),
        const SizedBox(height: 10),
        _QuickLink(
          icon: Icons.bar_chart,
          title: 'Activity today',
          detail: _reportDetail(widget.reports),
          onTap: widget.onActivity,
        ),
        _QuickLink(
          icon: Icons.tune,
          title: 'Safety controls',
          detail:
              '${widget.policies.state.value?.length ?? 0} rules reported by API',
          onTap: widget.onControls,
        ),
        _QuickLink(
          icon: Icons.language,
          title: 'Website safety',
          detail: 'Domain enforcement is unsupported',
          onTap: null,
        ),
        const SizedBox(height: 10),
        Card(
          child: ListTile(
            title: const Text('Last policy sync'),
            subtitle: const Text(
              'Reported sync time is not available in this contract',
            ),
            trailing: const Text(
              'UNKNOWN',
              style: TextStyle(
                color: ParentVisualTokens.warning,
                fontSize: 10,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        ),
      ],
    );
  }

  String _reportDetail(UsageReportViewModel vm) => vm.state.value == null
      ? 'Activity history unavailable'
      : 'Report ${vm.state.value!.freshness.name}';
}

class _LargeStatus extends StatelessWidget {
  const _LargeStatus({required this.icon, required this.label});
  final IconData icon;
  final String label;
  @override
  Widget build(BuildContext context) => CircleAvatar(
    radius: 40,
    backgroundColor: ParentVisualTokens.primary,
    child: Icon(icon, color: Colors.white, size: 34),
  );
}

class _QuickLink extends StatelessWidget {
  const _QuickLink({
    required this.icon,
    required this.title,
    required this.detail,
    required this.onTap,
  });
  final IconData icon;
  final String title;
  final String detail;
  final VoidCallback? onTap;
  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      enabled: onTap != null,
      onTap: onTap,
      leading: CircleAvatar(
        backgroundColor: ParentVisualTokens.paleBlue,
        child: Icon(icon, color: ParentVisualTokens.primary),
      ),
      title: Text(title),
      subtitle: Text(detail),
      trailing: const Icon(Icons.chevron_right),
    ),
  );
}
