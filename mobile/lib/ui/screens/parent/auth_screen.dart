part of 'parent_screens.dart';

class AuthScreen extends StatelessWidget {
  const AuthScreen({
    super.key,
    required this.viewModel,
    required this.onAuthenticated,
  });

  final AuthViewModel viewModel;
  final VoidCallback onAuthenticated;

  @override
  Widget build(BuildContext context) =>
      _AuthScreen(viewModel: viewModel, onAuthenticated: onAuthenticated);
}
