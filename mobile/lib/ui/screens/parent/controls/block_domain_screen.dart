import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../view_model/parent_view_models.dart';
import 'parent_ui_tokens.dart';

class BlockDomainScreen extends StatefulWidget {
  const BlockDomainScreen({
    super.key,
    required this.child,
    required this.viewModel,
  });
  final Child child;
  final PolicyViewModel viewModel;
  @override
  State<BlockDomainScreen> createState() => _BlockDomainScreenState();
}

class _BlockDomainScreenState extends State<BlockDomainScreen> {
  final controller = TextEditingController();
  int duration = 0;
  String? error;
  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    final domain = controller.text.trim().toLowerCase();
    final valid = RegExp(
      r'^(?=.{1,255}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}$',
    ).hasMatch(domain);
    if (!valid) {
      setState(() => error = 'Enter a valid domain such as example.com.');
      return;
    }
    await widget.viewModel.create(widget.child.id, PolicyType.domain, domain);
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: ParentUiTokens.canvas,
    appBar: AppBar(
      title: const Text('Block a website'),
      backgroundColor: ParentUiTokens.canvas,
      foregroundColor: ParentUiTokens.ink,
    ),
    body: ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
      children: [
        Text(
          'Add a domain to ${widget.child.displayName}’s blocked list',
          style: ParentUiTokens.body,
        ),
        const SizedBox(height: 36),
        const Text('Website or domain', style: ParentUiTokens.section),
        const SizedBox(height: 8),
        TextField(
          controller: controller,
          keyboardType: TextInputType.url,
          decoration: InputDecoration(
            hintText: 'example.com',
            errorText: error,
            filled: true,
            fillColor: Colors.white,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(14),
              borderSide: const BorderSide(color: ParentUiTokens.divider),
            ),
          ),
          onChanged: (_) {
            if (error != null) setState(() => error = null);
          },
        ),
        const SizedBox(height: 28),
        const Text('Choose duration', style: ParentUiTokens.section),
        const SizedBox(height: 8),
        for (var i = 0; i < 3; i++)
          Card(
            elevation: 0,
            child: RadioListTile<int>(
              value: i,
              groupValue: duration,
              onChanged: i == 0
                  ? (value) => setState(() => duration = value ?? duration)
                  : null,
              title: Text(
                const ['Until I allow it', 'For today', 'Custom schedule'][i],
              ),
              subtitle: Text(
                i == 0
                    ? 'No expiry'
                    : 'Not supported by the current policy API',
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 8),
            ),
          ),
        Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: ParentUiTokens.paleBlue,
            borderRadius: BorderRadius.circular(17),
          ),
          child: const Text(
            'This policy is submitted to the child device for confirmation. Browser and in-app enforcement may be unsupported.',
            style: TextStyle(color: ParentUiTokens.primary, fontSize: 12),
          ),
        ),
        const SizedBox(height: 32),
        FilledButton(
          onPressed: widget.viewModel.state.isBusy ? null : _save,
          style: FilledButton.styleFrom(
            minimumSize: const Size.fromHeight(52),
            backgroundColor: ParentUiTokens.primary,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(14),
            ),
          ),
          child: const Text('Block domain'),
        ),
      ],
    ),
  );
}
