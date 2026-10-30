part of 'child_screens.dart';

class ChildConsentScreen extends StatelessWidget {
  const ChildConsentScreen({
    super.key,
    required this.viewModel,
    this.version = 'v1',
  });

  final ChildViewModel viewModel;
  final String version;

  @override
  Widget build(BuildContext context) =>
      _ChildConsentScreen(viewModel: viewModel, version: version);
}
