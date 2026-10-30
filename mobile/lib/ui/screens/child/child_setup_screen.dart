part of 'child_screens.dart';

class ChildSetupScreen extends StatelessWidget {
  const ChildSetupScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => _ChildSetupScreen(viewModel: viewModel);
}
