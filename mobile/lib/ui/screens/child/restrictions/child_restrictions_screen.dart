import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../model/child_models.dart';
import '../view_model/child_view_model.dart';
import 'child_game_restricted_screen.dart';
import 'child_website_safety_screen.dart';

class ChildRestrictionsScreen extends StatefulWidget {
  const ChildRestrictionsScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  State<ChildRestrictionsScreen> createState() =>
      _ChildRestrictionsScreenState();
}

class _ChildRestrictionsScreenState extends State<ChildRestrictionsScreen> {
  String _selectedTab = 'Apps';

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: widget.viewModel,
    builder: (context, _) => Scaffold(
      appBar: AppBar(title: const Text('Tam Dan')),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.gap),
        children: [
          const Text('Active restrictions', style: AppTextStyles.title),
          const SizedBox(height: 12),
          _RestrictionTabs(
            selected: _selectedTab,
            onChanged: (value) => setState(() => _selectedTab = value),
          ),
          const SizedBox(height: 24),
          _tabContent(context),
        ],
      ),
    ),
  );

  Widget _tabContent(BuildContext context) {
    if (_selectedTab == 'Website') {
      return Card(
        child: ListTile(
          leading: const Icon(Icons.language),
          title: const Text('Website activity'),
          subtitle: const Text('Open the supported safety summary'),
          trailing: const Icon(Icons.chevron_right),
          onTap: () => Navigator.of(context).push(
            MaterialPageRoute<void>(
              builder: (_) =>
                  ChildWebsiteSafetyScreen(viewModel: widget.viewModel),
            ),
          ),
        ),
      );
    }
    final policies = widget.viewModel.state.policies
        .where((policy) {
          final isGame = policy.target.toLowerCase().contains('game');
          return _selectedTab == 'Games' ? isGame : true;
        })
        .toList(growable: false);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          _selectedTab == 'Games' ? 'Game restrictions' : 'Daily app limits',
          style: AppTextStyles.title,
        ),
        const SizedBox(height: 8),
        if (policies.isEmpty)
          const _EmptyRestrictionCard()
        else
          ...policies.map(
            (policy) => _PolicyRow(
              policy: policy,
              onTap: _selectedTab == 'Games'
                  ? () => Navigator.of(context).push(
                      MaterialPageRoute<void>(
                        builder: (_) => ChildGameRestrictedScreen(
                          viewModel: widget.viewModel,
                          policy: policy,
                        ),
                      ),
                    )
                  : null,
            ),
          ),
      ],
    );
  }
}

class _RestrictionTabs extends StatelessWidget {
  const _RestrictionTabs({required this.selected, required this.onChanged});

  final String selected;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) => SegmentedButton<String>(
    segments: [
      ButtonSegment(value: 'Apps', label: Text('Apps')),
      ButtonSegment(value: 'Website', label: Text('Website')),
      ButtonSegment(value: 'Games', label: Text('Games')),
    ],
    selected: {selected},
    onSelectionChanged: (values) => onChanged(values.first),
    showSelectedIcon: false,
  );
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
