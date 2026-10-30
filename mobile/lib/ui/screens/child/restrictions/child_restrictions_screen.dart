import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../model/child_models.dart';
import '../view_model/child_view_model.dart';
import 'child_game_restricted_screen.dart';
import 'child_website_safety_screen.dart';

class ChildRestrictionsScreen extends StatelessWidget {
  const ChildRestrictionsScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: viewModel,
    builder: (context, _) => Scaffold(
      appBar: AppBar(title: const Text('Tam Dan')),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.gap),
        children: [
          const Text('Active restrictions', style: AppTextStyles.title),
          const SizedBox(height: 12),
          const _RestrictionTabs(),
          const SizedBox(height: 24),
          const Text('Daily app limits', style: AppTextStyles.title),
          const SizedBox(height: 8),
          if (viewModel.state.policies.isEmpty)
            const _EmptyRestrictionCard()
          else
            ...viewModel.state.policies.map(
              (policy) => _PolicyRow(
                policy: policy,
                onTap: policy.target.toLowerCase().contains('game')
                    ? () => Navigator.of(context).push(
                        MaterialPageRoute<void>(
                          builder: (_) => ChildGameRestrictedScreen(
                            viewModel: viewModel,
                            policy: policy,
                          ),
                        ),
                      )
                    : null,
              ),
            ),
          const SizedBox(height: 20),
          const Text('Website safety', style: AppTextStyles.title),
          const SizedBox(height: 8),
          Card(
            child: ListTile(
              leading: const Icon(Icons.language),
              title: const Text('Website activity'),
              subtitle: const Text('Open the supported safety summary'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => Navigator.of(context).push(
                MaterialPageRoute<void>(
                  builder: (_) =>
                      ChildWebsiteSafetyScreen(viewModel: viewModel),
                ),
              ),
            ),
          ),
        ],
      ),
    ),
  );
}

class _RestrictionTabs extends StatelessWidget {
  const _RestrictionTabs();

  @override
  Widget build(BuildContext context) => SegmentedButton<String>(
    segments: [
      ButtonSegment(value: 'Apps', label: Text('Apps')),
      ButtonSegment(value: 'Website', label: Text('Website')),
      ButtonSegment(value: 'Games', label: Text('Games')),
    ],
    selected: {'Apps'},
    onSelectionChanged: _ignoreSelection,
    showSelectedIcon: false,
  );

  static void _ignoreSelection(Set<String> _) {}
}

class _PolicyRow extends StatelessWidget {
  const _PolicyRow({required this.policy, this.onTap});

  final ChildPolicy policy;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      leading: const Icon(Icons.apps),
      title: Text(policy.target),
      subtitle: Text('Policy version ${policy.version}'),
      trailing: Text(policy.state.name),
      onTap: onTap,
    ),
  );
}

class _EmptyRestrictionCard extends StatelessWidget {
  const _EmptyRestrictionCard();

  @override
  Widget build(BuildContext context) => const Card(
    child: ListTile(
      leading: Icon(Icons.info_outline),
      title: Text('No restriction data'),
      subtitle: Text(
        'Restrictions will appear here when the paired device receives supported policy data.',
      ),
    ),
  );
}
