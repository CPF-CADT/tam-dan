part of 'child_screens.dart';

class ChildStatusScreen extends StatelessWidget {
  const ChildStatusScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) =>
      _ChildStatusScreen(viewModel: viewModel);
}
