import 'package:flutter/material.dart';

import '../../../../core/constants/app_constants.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../model/child_models.dart';
import '../view_model/child_view_model.dart';

class ChildSetupScreen extends StatelessWidget {
  const ChildSetupScreen({super.key, required this.viewModel, this.onPaired});

  final ChildViewModel viewModel;
  final VoidCallback? onPaired;

  @override
  Widget build(BuildContext context) =>
      _ChildSetupScreen(viewModel: viewModel, onPaired: onPaired);
}

class _ChildSetupScreen extends StatefulWidget {
  const _ChildSetupScreen({required this.viewModel, this.onPaired});

  final ChildViewModel viewModel;
  final VoidCallback? onPaired;

  @override
  State<_ChildSetupScreen> createState() => _ChildSetupScreenState();
}

class _ChildSetupScreenState extends State<_ChildSetupScreen> {
  final _code = TextEditingController();
  final _name = TextEditingController(text: 'Child device');

  @override
  void dispose() {
    _code.dispose();
    _name.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: widget.viewModel,
    builder: (context, _) => Scaffold(
      appBar: AppBar(title: const Text('Set up this device')),
      body: ListView(
        padding: const EdgeInsets.all(AppTheme.gap),
        children: [
          const Text(
            'Pairing and privacy',
            style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: AppTheme.gap),
          const Text(
            'Tam Dan shows monitoring status transparently. It does not read messages, passwords, or private application content.',
          ),
          const SizedBox(height: AppTheme.gap),
          TextField(
            controller: _code,
            decoration: const InputDecoration(labelText: 'Parent pairing code'),
          ),
          const SizedBox(height: AppTheme.gap),
          TextField(
            controller: _name,
            decoration: const InputDecoration(labelText: 'Device name'),
          ),
          const SizedBox(height: AppTheme.gap),
          FilledButton(
            onPressed: widget.viewModel.pairingState == PairingState.submitting
                ? null
                : () => widget.viewModel.pair(
                    code: _code.text,
                    appVersion: AppConstants.childAppVersion,
                    displayName: _name.text,
                  ),
            child: Text(
              widget.viewModel.pairingState == PairingState.submitting
                  ? 'Pairing…'
                  : 'Pair device',
            ),
          ),
          if (widget.viewModel.pairingError != null)
            Padding(
              padding: const EdgeInsets.only(top: AppTheme.gap),
              child: Text(
                _pairingMessage(
                  widget.viewModel.pairingState,
                  widget.viewModel.pairingError!,
                ),
                style: TextStyle(color: Theme.of(context).colorScheme.error),
              ),
            ),
          if (widget.viewModel.pairingState == PairingState.success) ...[
            const Card(
              child: ListTile(
                leading: Icon(Icons.check),
                title: Text('Device paired'),
                subtitle: Text(
                  'Credential stored securely. Continue with consent and permissions.',
                ),
              ),
            ),
            FilledButton(
              onPressed: widget.onPaired,
              child: const Text('Continue'),
            ),
          ],
        ],
      ),
    ),
  );

  String _pairingMessage(PairingState state, String fallback) =>
      switch (state) {
        PairingState.invalid =>
          'This pairing code is invalid. Ask the parent for a new code.',
        PairingState.expired =>
          'This pairing code expired. Ask the parent for a new code.',
        PairingState.used => 'This pairing code was already used.',
        PairingState.offline => 'You are offline. Reconnect and try again.',
        _ => fallback,
      };
}
