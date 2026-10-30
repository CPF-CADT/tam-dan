import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../view_model/parent_view_models.dart';
import 'parent_visual_tokens.dart';

class ParentHomeScreen extends StatefulWidget {
  const ParentHomeScreen({
    super.key,
    required this.child,
    required this.devices,
    required this.reports,
    required this.alerts,
    required this.onOpenDevice,
    required this.onOpenActivity,
  });

  final Child child;
  final DeviceViewModel devices;
  final UsageReportViewModel reports;
  final AlertViewModel alerts;
  final VoidCallback onOpenDevice;
  final VoidCallback onOpenActivity;

  @override
  State<ParentHomeScreen> createState() => _ParentHomeScreenState();
}

class _ParentHomeScreenState extends State<ParentHomeScreen> {
  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void didUpdateWidget(covariant ParentHomeScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.child.id != widget.child.id) _load();
  }

  void _load() {
    widget.devices.load(widget.child.id);
    final now = DateTime.now().toUtc();
    widget.reports.load(
      widget.child.id,
      now.subtract(const Duration(days: 1)),
      now,
    );
    widget.alerts.load(widget.child.id);
  }

  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
    children: [
      Text(
        'Tam Dan',
        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
          color: ParentVisualTokens.ink,
          fontWeight: FontWeight.w800,
        ),
      ),
      const SizedBox(height: 2),
      const Text(
        'Parent control',
        style: TextStyle(color: ParentVisualTokens.muted, fontSize: 12),
      ),
      const SizedBox(height: 20),
      Text(
        'Good morning, Dara',
        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
          color: ParentVisualTokens.ink,
          fontWeight: FontWeight.w800,
        ),
      ),
      const SizedBox(height: 18),
      _ChildCard(
        child: widget.child,
        devices: widget.devices,
        onTap: widget.onOpenDevice,
      ),
      const SizedBox(height: 18),
      _MetricsCard(
        reports: widget.reports,
        alerts: widget.alerts,
        onActivity: widget.onOpenActivity,
      ),
      const SizedBox(height: 18),
      _AlertCard(viewModel: widget.alerts),
      const SizedBox(height: 18),
      _ProtectionCard(devices: widget.devices),
    ],
  );
}

class _ChildCard extends StatelessWidget {
  const _ChildCard({
    required this.child,
    required this.devices,
    required this.onTap,
  });
  final Child child;
  final DeviceViewModel devices;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final device = devices.state.value?.firstOrNull;
    return _TappableCard(
      onTap: onTap,
      child: Row(
        children: [
          const _Badge(
            icon: Icons.person_outline,
            color: ParentVisualTokens.primary,
            size: 64,
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  child.displayName,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: ParentVisualTokens.ink,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  device == null
                      ? 'Device status unavailable'
                      : '${device.displayName} • ${device.status.name}',
                  style: const TextStyle(
                    fontSize: 12,
                    color: ParentVisualTokens.muted,
                  ),
                ),
                const SizedBox(height: 8),
                _StatusPill(
                  label: device?.status == DeviceStatus.active
                      ? '● Active'
                      : 'Status unavailable',
                  active: device?.status == DeviceStatus.active,
                ),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: ParentVisualTokens.muted),
        ],
      ),
    );
  }
}

class _MetricsCard extends StatelessWidget {
  const _MetricsCard({
    required this.reports,
    required this.alerts,
    required this.onActivity,
  });
  final UsageReportViewModel reports;
  final AlertViewModel alerts;
  final VoidCallback onActivity;

  @override
  Widget build(BuildContext context) {
    final report = reports.state.value;
    final alertCount = alerts.state.value
        ?.where((alert) => alert.state == AlertState.open)
        .length;
    return _TappableCard(
      onTap: onActivity,
      child: Row(
        children: [
          _Metric(
            label: 'Report',
            value: report == null ? '—' : report.freshness.name,
            color: ParentVisualTokens.primary,
          ),
          _Metric(
            label: 'Alerts',
            value: alertCount?.toString() ?? '—',
            color: ParentVisualTokens.warning,
          ),
          const _Metric(
            label: 'Blocked',
            value: '—',
            color: ParentVisualTokens.error,
          ),
        ],
      ),
    );
  }
}

class _AlertCard extends StatelessWidget {
  const _AlertCard({required this.viewModel});
  final AlertViewModel viewModel;

  @override
  Widget build(BuildContext context) {
    final alerts = viewModel.state.value ?? const <Alert>[];
    final open = alerts
        .where((alert) => alert.state == AlertState.open)
        .toList();
    final detail =
        open.firstOrNull?.title ??
        (viewModel.state.status == LoadStatus.empty
            ? 'No alerts need review'
            : 'Alert details unavailable');
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: ParentVisualTokens.warningSurface,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          const _Badge(
            icon: Icons.warning_amber_rounded,
            color: ParentVisualTokens.warning,
            size: 46,
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${open.length} alerts need review',
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    color: ParentVisualTokens.ink,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  detail,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 11,
                    color: ParentVisualTokens.muted,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ProtectionCard extends StatelessWidget {
  const _ProtectionCard({required this.devices});
  final DeviceViewModel devices;

  @override
  Widget build(BuildContext context) {
    final unsupported = devices.state.status == LoadStatus.unsupported;
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: ParentVisualTokens.primary,
        borderRadius: BorderRadius.circular(22),
      ),
      child: Row(
        children: [
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'PROTECTION',
                  style: TextStyle(
                    color: ParentVisualTokens.paleBlue,
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                SizedBox(height: 8),
                Text(
                  'Status is reported by the device',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 17,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                SizedBox(height: 8),
                Text(
                  'VPN enforcement is not supported',
                  style: TextStyle(
                    color: ParentVisualTokens.paleBlue,
                    fontSize: 11,
                  ),
                ),
              ],
            ),
          ),
          _Badge(
            icon: unsupported ? Icons.help_outline : Icons.check,
            color: unsupported
                ? ParentVisualTokens.warning
                : ParentVisualTokens.paleMint,
            size: 48,
          ),
        ],
      ),
    );
  }
}

class _Metric extends StatelessWidget {
  const _Metric({
    required this.label,
    required this.value,
    required this.color,
  });
  final String label;
  final String value;
  final Color color;

  @override
  Widget build(BuildContext context) => Expanded(
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          value,
          style: TextStyle(
            color: color,
            fontSize: 22,
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: const TextStyle(color: ParentVisualTokens.muted, fontSize: 10),
        ),
      ],
    ),
  );
}

class _TappableCard extends StatelessWidget {
  const _TappableCard({required this.onTap, required this.child});
  final VoidCallback onTap;
  final Widget child;

  @override
  Widget build(BuildContext context) => Material(
    color: Colors.white,
    borderRadius: BorderRadius.circular(20),
    child: InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: Padding(padding: const EdgeInsets.all(18), child: child),
    ),
  );
}

class _Badge extends StatelessWidget {
  const _Badge({required this.icon, required this.color, required this.size});
  final IconData icon;
  final Color color;
  final double size;

  @override
  Widget build(BuildContext context) => CircleAvatar(
    radius: size / 2,
    backgroundColor: color.withOpacity(.18),
    child: Icon(icon, color: color, size: size * .42),
  );
}

class _StatusPill extends StatelessWidget {
  const _StatusPill({required this.label, required this.active});
  final String label;
  final bool active;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
    decoration: BoxDecoration(
      color: active
          ? ParentVisualTokens.paleMint
          : ParentVisualTokens.warningSurface,
      borderRadius: BorderRadius.circular(12),
    ),
    child: Text(
      label,
      style: TextStyle(
        color: active ? ParentVisualTokens.success : ParentVisualTokens.warning,
        fontSize: 10,
        fontWeight: FontWeight.w700,
      ),
    ),
  );
}
