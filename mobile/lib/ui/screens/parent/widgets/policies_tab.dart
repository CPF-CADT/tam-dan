part of 'package:mobile/ui/screens/parent/parent_screens.dart';

class PoliciesTab extends StatelessWidget {
  const PoliciesTab({super.key, required this.child, required this.viewModel});

  final Child child;
  final PolicyViewModel viewModel;

  @override
  Widget build(BuildContext context) =>
      _PoliciesTab(child: child, viewModel: viewModel);
}
