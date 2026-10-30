import 'package:flutter/material.dart';

import '../child_screens.dart';
import '../activity/child_navigation_shell.dart';
import '../view_model/child_view_model.dart';
import 'enable_protection_screen.dart';
import 'permissions_screen.dart';
import 'setup_complete_screen.dart';
import 'vpn_setup_screen.dart';

class ChildSetupFlow extends StatefulWidget {
  const ChildSetupFlow({super.key, required this.viewModel});
  final ChildViewModel viewModel;
  @override
  State<ChildSetupFlow> createState() => _ChildSetupFlowState();
}

class _ChildSetupFlowState extends State<ChildSetupFlow> {
  int _step = 0;

  @override
  Widget build(BuildContext context) {
    if (_step == 0) {
      return ChildSetupScreen(
        viewModel: widget.viewModel,
        onPaired: () => setState(() => _step = 1),
      );
    }
    return switch (_step) {
      1 => ChildEnableProtectionScreen(
        viewModel: widget.viewModel,
        onContinue: () => setState(() => _step = 2),
      ),
      2 => ChildVpnSetupScreen(
        viewModel: widget.viewModel,
        onContinue: () => setState(() => _step = 3),
      ),
      3 => ChildPermissionsScreen(
        viewModel: widget.viewModel,
        onContinue: () async {
          await widget.viewModel.startMonitoring();
          if (mounted) setState(() => _step = 4);
        },
      ),
      4 => ChildSetupCompleteScreen(
        viewModel: widget.viewModel,
        onContinue: () => Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => ChildNavigationShell(viewModel: widget.viewModel),
          ),
        ),
      ),
      _ => ChildNavigationShell(viewModel: widget.viewModel),
    };
  }
}
