import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/segmented_tabs.dart';
import '../../../../model/child_models.dart';
import '../view_model/child_view_model.dart';

class ChildActivityScreen extends StatelessWidget {
  const ChildActivityScreen({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: viewModel,
    builder: (context, _) {
      final health = viewModel.state.health;
      return Scaffold(
        appBar: AppBar(title: const Text('Tam Dan')),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
          children: [
            const Text('Activity', style: AppTextStyles.title),
            const SizedBox(height: 12),
            SegmentedTabs<String>(
              items: const ['Today', '7 Days', '30 Days'],
              value: 'Today',
              onChanged: (_) {},
            ),
            const SizedBox(height: 24),
            const Text('Screen time today', style: AppTextStyles.title),
            const SizedBox(height: 8),
            Text(
              'Unavailable',
              style: AppTextStyles.display.copyWith(
                fontSize: 28,
                height: 34 / 28,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              _activityMessage(health),
              style: AppTextStyles.label.copyWith(color: AppColors.muted),
            ),
            const SizedBox(height: 20),
            const _UnavailableActivityCard(
              title: 'Screen-time history',
              message:
                  'This child app has health and synchronization status, but no supported activity-history data.',
            ),
            const SizedBox(height: 20),
            const Text('Most used apps', style: AppTextStyles.title),
            const SizedBox(height: 8),
            const _UnavailableActivityCard(
              title: 'App usage',
              message: 'No app-level usage history is available.',
            ),
          ],
        ),
      );
    },
  );

  static String _activityMessage(ChildHealth? health) {
    if (health == null) return 'Activity data is not available yet.';
    if (health.sync == SyncState.offline || health.sync == SyncState.stale) {
      return 'Activity history is unavailable while synchronization is not current.';
    }
    return 'Activity history is not part of the supported child data yet.';
  }
}

class _UnavailableActivityCard extends StatelessWidget {
  const _UnavailableActivityCard({required this.title, required this.message});

  final String title;
  final String message;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      leading: const Icon(Icons.info_outline),
      title: Text(title),
      subtitle: Text(message),
    ),
  );
}
