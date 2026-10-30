import 'package:flutter/material.dart';

import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../view_model/child_view_model.dart';

class ChildWebsiteSafetyScreen extends StatelessWidget {
  const ChildWebsiteSafetyScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: viewModel,
    builder: (context, _) => Scaffold(
      appBar: AppBar(title: const Text('Website safety')),
      body: ListView(
        padding: const EdgeInsets.all(AppSpacing.gap),
        children: [
          _WebsiteTabs(),
          SizedBox(height: 24),
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
          Text('Recent activity', style: AppTextStyles.title),
          SizedBox(height: 8),
          Card(
            child: ListTile(
              leading: Icon(Icons.info_outline),
              title: Text('Website activity unavailable'),
              subtitle: Text(
                'The current child data contract does not provide website events or safety counts.',
              ),
            ),
          ),
        ],
      ),
    ),
  );
}

class _WebsiteTabs extends StatelessWidget {
  const _WebsiteTabs();

  @override
  Widget build(BuildContext context) => SegmentedButton<String>(
    segments: [
      ButtonSegment(value: 'Summary', label: Text('Summary')),
      ButtonSegment(value: 'Blocked', label: Text('Blocked')),
      ButtonSegment(value: 'Allowed', label: Text('Allowed')),
    ],
    selected: {'Summary'},
    onSelectionChanged: _ignoreSelection,
    showSelectedIcon: false,
  );

  static void _ignoreSelection(Set<String> _) {}
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
