import 'package:flutter/material.dart';

import '../../../../core/theme/app_theme.dart';
import '../../../../model/child_models.dart';
import '../view_model/child_view_model.dart';

class ChildGameRestrictedScreen extends StatelessWidget {
  const ChildGameRestrictedScreen({
    super.key,
    required this.viewModel,
    required this.policy,
  });

  final ChildViewModel viewModel;
  final ChildPolicy policy;

  @override
  Widget build(BuildContext context) {
    final enforceable =
        viewModel.state.health?.monitoring == MonitoringState.active;
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          tooltip: 'Back',
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.all(AppTheme.gap),
        children: [
          const SizedBox(height: 20),
          const Icon(Icons.games_outlined, size: 88),
          const SizedBox(height: 24),
          const Text(
            'Game restricted',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          Text(
            'Your parent has restricted internet access for ${policy.target}.',
            textAlign: TextAlign.center,
            style: const TextStyle(color: AppTheme.authMuted),
          ),
          const SizedBox(height: 28),
          Card(
            color: Theme.of(context).colorScheme.errorContainer,
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _RestrictionNotice(
                    icon: Icons.public_off,
                    text: 'Online features are blocked',
                  ),
                  const _RestrictionNotice(
                    icon: Icons.gamepad_outlined,
                    text: 'You may still play offline',
                  ),
                  _RestrictionNotice(
                    icon: Icons.schedule,
                    text: enforceable
                        ? 'The paired device reports monitoring active'
                        : 'Current enforcement cannot be verified on this device',
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 28),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }
}

class _RestrictionNotice extends StatelessWidget {
  const _RestrictionNotice({required this.icon, required this.text});

  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 8),
    child: Row(
      children: [
        Icon(icon, color: Theme.of(context).colorScheme.error),
        const SizedBox(width: 12),
        Expanded(child: Text(text)),
      ],
    ),
  );
}
