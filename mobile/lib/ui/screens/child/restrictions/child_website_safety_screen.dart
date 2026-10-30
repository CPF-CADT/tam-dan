import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/segmented_tabs.dart';
import '../view_model/child_view_model.dart';

class ChildWebsiteSafetyScreen extends StatefulWidget {
  const ChildWebsiteSafetyScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  State<ChildWebsiteSafetyScreen> createState() =>
      _ChildWebsiteSafetyScreenState();
}

class _ChildWebsiteSafetyScreenState extends State<ChildWebsiteSafetyScreen> {
  String _selectedTab = 'Summary';

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: widget.viewModel,
    builder: (context, _) => Scaffold(
      appBar: AppBar(title: const Text('Website safety')),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.gap),
        children: [
          _WebsiteTabs(
            selected: _selectedTab,
            onChanged: (value) => setState(() => _selectedTab = value),
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Expanded(child: _SafetyMetric(label: 'Safe')),
              SizedBox(width: 10),
              Expanded(child: _SafetyMetric(label: 'Caution')),
              SizedBox(width: 10),
              Expanded(child: _SafetyMetric(label: 'Blocked')),
            ],
          ),
          SizedBox(height: 24),
          Text(
            _selectedTab == 'Summary'
                ? 'Recent activity'
                : '$_selectedTab websites',
            style: AppTextStyles.title,
          ),
          const SizedBox(height: 8),
          _UnavailableWebsiteCard(tab: _selectedTab),
        ],
      ),
    ),
  );
}

class _WebsiteTabs extends StatelessWidget {
  const _WebsiteTabs({required this.selected, required this.onChanged});

  final String selected;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) => SegmentedTabs<String>(
    items: const ['Summary', 'Blocked', 'Allowed'],
    value: selected,
    onChanged: onChanged,
  );
}

class _UnavailableWebsiteCard extends StatelessWidget {
  const _UnavailableWebsiteCard({required this.tab});

  final String tab;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      leading: const Icon(Icons.info_outline),
      title: Text(
        tab == 'Summary'
            ? 'Website activity unavailable'
            : '$tab websites unavailable',
      ),
      subtitle: const Text(
        'The current child data contract does not provide website events or safety counts.',
      ),
    ),
  );
}

class _SafetyMetric extends StatelessWidget {
  const _SafetyMetric({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Column(
        children: [
          const Text('—', style: TextStyle(fontSize: 26)),
          Text(label),
          const Text('Unavailable', style: TextStyle(fontSize: 11)),
        ],
      ),
    ),
  );
}
