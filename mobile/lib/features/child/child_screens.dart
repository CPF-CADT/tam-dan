import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';
import 'child_models.dart';
import 'child_view_model.dart';

class ChildSetupScreen extends StatefulWidget {
  const ChildSetupScreen({super.key, required this.viewModel});
  final ChildViewModel viewModel;

  @override
  State<ChildSetupScreen> createState() => _ChildSetupScreenState();
}

class _ChildSetupScreenState extends State<ChildSetupScreen> {
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
                    appVersion: '1.0.0',
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
          if (widget.viewModel.pairingState == PairingState.success)
            const Card(
              child: ListTile(
                leading: Icon(Icons.check),
                title: Text('Device paired'),
                subtitle: Text(
                  'Credential stored securely. Continue with consent and permissions.',
                ),
              ),
            ),
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

class ChildStatusScreen extends StatelessWidget {
  const ChildStatusScreen({super.key, required this.viewModel});
  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: viewModel,
    builder: (context, _) {
      final state = viewModel.state;
      return Scaffold(
        appBar: AppBar(
          title: Text(state.device?.displayName ?? 'Device status'),
        ),
        body: ListView(
          padding: const EdgeInsets.all(AppTheme.gap),
          children: [
            if (state.status == ChildLoadStatus.revoked)
              const _MessageCard(
                title: 'Device access revoked',
                message:
                    'This device is no longer paired. Pair it again with a new code.',
              ),
            if (state.isLastKnown)
              const _MessageCard(
                title: 'Offline',
                message:
                    'Showing the last known status. New activity is not confirmed until synchronization succeeds.',
              ),
            _statusCard(
              'Monitoring',
              state.health?.monitoring.name ?? 'unknown',
            ),
            _statusCard(
              'Synchronization',
              state.health?.sync.name ?? 'unknown',
            ),
            _statusCard('Permission', state.permission.name),
            const SizedBox(height: AppTheme.gap),
            const Text(
              'Active policies',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            if (state.policies.isEmpty)
              const ListTile(title: Text('No policy information available')),
            ...state.policies.map(
              (policy) => ListTile(
                title: Text(policy.target),
                subtitle: Text('Version ${policy.version}'),
                trailing: Text(policy.state.name),
              ),
            ),
            if (state.error != null)
              Text(
                state.error!,
                style: TextStyle(color: Theme.of(context).colorScheme.error),
              ),
            FilledButton(
              onPressed: viewModel.loadStatus,
              child: const Text('Refresh status'),
            ),
          ],
        ),
      );
    },
  );

  Widget _statusCard(String label, String value) => Card(
    child: ListTile(title: Text(label), trailing: Text(value)),
  );
}

class ChildConsentScreen extends StatelessWidget {
  const ChildConsentScreen({
    super.key,
    required this.viewModel,
    this.version = 'v1',
  });
  final ChildViewModel viewModel;
  final String version;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Privacy and consent')),
    body: Padding(
      padding: const EdgeInsets.all(AppTheme.gap),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            'What is shared',
            style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: AppTheme.gap),
          const Text(
            'The device reports approved activity and monitoring health to the parent account. Messages, passwords, and private content are not collected.',
          ),
          const Spacer(),
          FilledButton(
            onPressed: () => viewModel.confirmConsent(version),
            child: const Text('I understand and continue'),
          ),
        ],
      ),
    ),
  );
}

class _MessageCard extends StatelessWidget {
  const _MessageCard({required this.title, required this.message});
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(title: Text(title), subtitle: Text(message)),
  );
}
