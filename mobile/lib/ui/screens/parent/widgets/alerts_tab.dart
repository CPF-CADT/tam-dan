part of 'package:mobile/ui/screens/parent/parent_screens.dart';

class AlertsTab extends StatelessWidget {
  const AlertsTab({super.key, required this.child, required this.viewModel});

  final Child child;
  final AlertViewModel viewModel;

  @override
  Widget build(BuildContext context) =>
      _AlertsTab(child: child, viewModel: viewModel);
}
