part of 'child_screens.dart';

class ChildSetupScreen extends StatelessWidget {
  const ChildSetupScreen({super.key, required this.viewModel, this.onPaired});

  final ChildViewModel viewModel;
  final VoidCallback? onPaired;

  @override
  Widget build(BuildContext context) =>
      _ChildSetupScreen(viewModel: viewModel, onPaired: onPaired);
}
