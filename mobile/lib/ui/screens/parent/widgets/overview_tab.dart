part of 'package:mobile/ui/screens/parent/parent_screens.dart';

class OverviewTab extends StatelessWidget {
  const OverviewTab({
    super.key,
    required this.child,
    required this.devices,
    required this.reports,
  });

  final Child child;
  final DeviceViewModel devices;
  final UsageReportViewModel reports;

  @override
  Widget build(BuildContext context) =>
      _OverviewTab(child: child, devices: devices, reports: reports);
}
