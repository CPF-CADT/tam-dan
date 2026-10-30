part of 'parent_screens.dart';

class ParentShell extends StatelessWidget {
  const ParentShell({
    super.key,
    required this.children,
    required this.devices,
    required this.reports,
    required this.alerts,
    required this.policies,
    required this.onLogout,
  });

  final ChildSelectionViewModel children;
  final DeviceViewModel devices;
  final UsageReportViewModel reports;
  final AlertViewModel alerts;
  final PolicyViewModel policies;
  final VoidCallback onLogout;

  @override
  Widget build(BuildContext context) => _ParentShell(
    children: children,
    devices: devices,
    reports: reports,
    alerts: alerts,
    policies: policies,
    onLogout: onLogout,
  );
}
