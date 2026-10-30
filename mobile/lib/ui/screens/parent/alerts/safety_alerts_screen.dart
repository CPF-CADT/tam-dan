import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../controls/parent_ui_tokens.dart';
import '../view_model/parent_view_models.dart';

class SafetyAlertsScreen extends StatelessWidget {
  const SafetyAlertsScreen({
    super.key,
    required this.child,
    required this.viewModel,
    this.onNavigate,
    this.showBottomNavigation = true,
  });
  final Child child;
  final AlertViewModel viewModel;
  final ValueChanged<int>? onNavigate;
  final bool showBottomNavigation;

  @override
  Widget build(BuildContext context) {
    final alerts = viewModel.state.value ?? const <Alert>[];
    return Scaffold(
      backgroundColor: ParentUiTokens.canvas,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () => viewModel.load(child.id),
          child: ListView(
            padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
            children: [
              const Text('Safety alerts', style: ParentUiTokens.title),
              Text(
                'Recent signals for ${child.displayName}',
                style: ParentUiTokens.body,
              ),
              const SizedBox(height: 22),
              if (viewModel.state.status == LoadStatus.loading)
                const Center(child: CircularProgressIndicator()),
              if (viewModel.state.status == LoadStatus.offline)
                const _AlertMessage(
                  'Alerts are unavailable offline. Try again when connected.',
                ),
              if (viewModel.state.status == LoadStatus.failure)
                _AlertMessage(
                  viewModel.state.message ?? 'Alerts could not be loaded.',
                ),
              for (final severity in const [
                AlertSeverity.high,
                AlertSeverity.medium,
                AlertSeverity.low,
              ]) ...[
                _SeverityHeader(
                  severity: severity,
                  count: alerts
                      .where((alert) => alert.severity == severity)
                      .length,
                ),
                ...alerts
                    .where((alert) => alert.severity == severity)
                    .map(
                      (alert) => _AlertTile(
                        alert: alert,
                        onReview: () => viewModel.review(alert.id),
                      ),
                    ),
              ],
              if (alerts.isEmpty && viewModel.state.status == LoadStatus.empty)
                const _AlertMessage('No safety alerts have been reported.'),
              const SizedBox(height: 12),
              const _AlertMessage(
                'Privacy note: alerts summarize safety signals from connected devices. They do not expose private content beyond the available alert detail.',
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: showBottomNavigation
          ? NavigationBar(
              selectedIndex: 1,
              onDestinationSelected: onNavigate,
              destinations: const [
                NavigationDestination(
                  icon: Icon(Icons.home_outlined),
                  label: 'Home',
                ),
                NavigationDestination(
                  icon: Icon(Icons.bar_chart_outlined),
                  label: 'Activity',
                ),
                NavigationDestination(
                  icon: Icon(Icons.tune),
                  label: 'Controls',
                ),
                NavigationDestination(
                  icon: Icon(Icons.settings_outlined),
                  label: 'Settings',
                ),
              ],
            )
          : null,
    );
  }
}

class _SeverityHeader extends StatelessWidget {
  const _SeverityHeader({required this.severity, required this.count});
  final AlertSeverity severity;
  final int count;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(top: 12, bottom: 8),
    child: Row(
      children: [
        Text(
          severity.name[0].toUpperCase() + severity.name.substring(1),
          style: ParentUiTokens.section,
        ),
        const SizedBox(width: 8),
        Text('$count', style: ParentUiTokens.body),
      ],
    ),
  );
}

class _AlertTile extends StatelessWidget {
  const _AlertTile({required this.alert, required this.onReview});
  final Alert alert;
  final VoidCallback onReview;
  @override
  Widget build(BuildContext context) {
    final color = alert.severity == AlertSeverity.high
        ? ParentUiTokens.error
        : alert.severity == AlertSeverity.medium
        ? ParentUiTokens.warning
        : ParentUiTokens.primary;
    return Card(
      elevation: 0,
      child: ListTile(
        leading: Icon(Icons.warning_amber_rounded, color: color),
        title: Text(
          alert.title ?? 'Safety alert',
          style: const TextStyle(fontWeight: FontWeight.w700),
        ),
        subtitle: Text(
          alert.detail ?? 'Review the available evidence.',
          style: ParentUiTokens.body,
        ),
        trailing: alert.state == AlertState.open
            ? TextButton(onPressed: onReview, child: const Text('Review'))
            : Text(alert.state.name),
      ),
    );
  }
}

class _AlertMessage extends StatelessWidget {
  const _AlertMessage(this.message);
  final String message;
  @override
  Widget build(BuildContext context) => Card(
    elevation: 0,
    child: Padding(
      padding: const EdgeInsets.all(18),
      child: Text(message, style: ParentUiTokens.body),
    ),
  );
}
