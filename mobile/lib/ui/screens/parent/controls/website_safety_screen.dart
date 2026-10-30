import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../../../widgets/bottom_navigation.dart';
import '../view_model/parent_view_models.dart';
import 'block_domain_screen.dart';
import 'game_internet_screen.dart';
import 'parent_ui_tokens.dart';

class WebsiteSafetyScreen extends StatefulWidget {
  const WebsiteSafetyScreen({
    super.key,
    required this.child,
    required this.viewModel,
    this.onNavigate,
    this.showBottomNavigation = true,
  });

  final Child child;
  final PolicyViewModel viewModel;
  final ValueChanged<int>? onNavigate;
  final bool showBottomNavigation;

  @override
  State<WebsiteSafetyScreen> createState() => _WebsiteSafetyScreenState();
}

class _WebsiteSafetyScreenState extends State<WebsiteSafetyScreen> {
  int tab = 0;

  @override
  void initState() {
    super.initState();
    if (widget.viewModel.state.status == LoadStatus.idle) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) widget.viewModel.load(widget.child.id);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final policies =
        widget.viewModel.state.value
            ?.where((policy) => policy.type == PolicyType.domain)
            .toList(growable: false) ??
        const <Policy>[];
    final filtered = switch (tab) {
      1 =>
        policies
            .where(
              (p) =>
                  p.state == PolicyState.active ||
                  p.state == PolicyState.pendingDelivery,
            )
            .toList(),
      2 => policies.where((p) => p.state == PolicyState.paused).toList(),
      _ => policies,
    };
    return Scaffold(
      backgroundColor: ParentUiTokens.canvas,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () => widget.viewModel.load(widget.child.id),
          child: ListView(
            padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
            children: [
              const Text('Website safety', style: ParentUiTokens.title),
              Text(
                'Today • ${widget.child.displayName}',
                style: ParentUiTokens.body,
              ),
              const SizedBox(height: 16),
              _SegmentedTabs(
                labels: const ['Summary', 'Blocked', 'Allowed'],
                selected: tab,
                onChanged: (value) => setState(() => tab = value),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  _MetricCard(
                    value: '${policies.length}',
                    label: 'Tracked',
                    color: ParentUiTokens.paleMint,
                    foreground: ParentUiTokens.success,
                  ),
                  const SizedBox(width: 10),
                  _MetricCard(
                    value:
                        '${policies.where((p) => p.state == PolicyState.active || p.state == PolicyState.pendingDelivery).length}',
                    label: 'Blocked',
                    color: ParentUiTokens.warningSurface,
                    foreground: ParentUiTokens.warning,
                  ),
                  const SizedBox(width: 10),
                  _MetricCard(
                    value:
                        '${policies.where((p) => p.state == PolicyState.failed || p.state == PolicyState.unsupported).length}',
                    label: 'Needs review',
                    color: ParentUiTokens.errorSurface,
                    foreground: ParentUiTokens.error,
                  ),
                ],
              ),
              const SizedBox(height: 28),
              const Text('Recent websites', style: ParentUiTokens.section),
              const SizedBox(height: 12),
              if (widget.viewModel.state.status == LoadStatus.loading)
                const Center(
                  child: Padding(
                    padding: EdgeInsets.all(24),
                    child: CircularProgressIndicator(),
                  ),
                )
              else if (widget.viewModel.state.status == LoadStatus.offline)
                _MessageCard(
                  message:
                      'Website activity is unavailable offline. Try again when connected.',
                )
              else if (widget.viewModel.state.status == LoadStatus.failure)
                _MessageCard(
                  message:
                      widget.viewModel.state.message ??
                      'Website activity could not be loaded.',
                )
              else if (filtered.isEmpty)
                const _MessageCard(
                  message:
                      'No website policy data is available for this child yet.',
                )
              else
                ...filtered.map(_policyRow),
              const SizedBox(height: 20),
              FilledButton(
                onPressed: () async {
                  await Navigator.of(context).push<void>(
                    MaterialPageRoute(
                      builder: (_) => BlockDomainScreen(
                        child: widget.child,
                        viewModel: widget.viewModel,
                      ),
                    ),
                  );
                },
                style: FilledButton.styleFrom(
                  minimumSize: const Size.fromHeight(52),
                  backgroundColor: ParentUiTokens.primary,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                child: const Text('Block a website'),
              ),
              TextButton.icon(
                onPressed: () => Navigator.of(context).push<void>(
                  MaterialPageRoute(
                    builder: (_) => GameInternetScreen(
                      child: widget.child,
                      viewModel: widget.viewModel,
                    ),
                  ),
                ),
                icon: const Icon(Icons.sports_esports_outlined),
                label: const Text('Manage game internet'),
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: widget.showBottomNavigation
          ? _ParentBottomNavigation(selected: 2, onSelected: widget.onNavigate)
          : null,
    );
  }

  Widget _policyRow(Policy policy) {
    final status = switch (policy.state) {
      PolicyState.active ||
      PolicyState.pendingDelivery => ('Blocked', ParentUiTokens.warning),
      PolicyState.paused => ('Allowed', ParentUiTokens.success),
      PolicyState.unsupported => ('Unsupported', ParentUiTokens.warning),
      _ => ('Needs review', ParentUiTokens.error),
    };
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(ParentUiTokens.radius),
      ),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: status.$2.withValues(alpha: .12),
          child: Icon(Icons.public, color: status.$2, size: 20),
        ),
        title: Text(
          policy.target,
          style: const TextStyle(fontWeight: FontWeight.w700),
        ),
        subtitle: Text(
          policy.state == PolicyState.pendingDelivery
              ? 'Waiting for device confirmation'
              : status.$1,
          style: ParentUiTokens.body,
        ),
        trailing: Text(
          status.$1,
          style: TextStyle(
            color: status.$2,
            fontSize: 11,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }
}

class _SegmentedTabs extends StatelessWidget {
  const _SegmentedTabs({
    required this.labels,
    required this.selected,
    required this.onChanged,
  });
  final List<String> labels;
  final int selected;
  final ValueChanged<int> onChanged;
  @override
  Widget build(BuildContext context) => Container(
    height: 36,
    decoration: BoxDecoration(
      color: const Color(0xFFEBF2FA),
      borderRadius: BorderRadius.circular(18),
    ),
    child: Row(
      children: [
        for (var i = 0; i < labels.length; i++)
          Expanded(
            child: GestureDetector(
              onTap: () => onChanged(i),
              child: Container(
                decoration: BoxDecoration(
                  color: i == selected
                      ? ParentUiTokens.primary
                      : Colors.transparent,
                  borderRadius: BorderRadius.circular(18),
                ),
                alignment: Alignment.center,
                child: Text(
                  labels[i],
                  style: TextStyle(
                    color: i == selected ? Colors.white : ParentUiTokens.muted,
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ),
          ),
      ],
    ),
  );
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({
    required this.value,
    required this.label,
    required this.color,
    required this.foreground,
  });
  final String value;
  final String label;
  final Color color;
  final Color foreground;
  @override
  Widget build(BuildContext context) => Expanded(
    child: Container(
      height: 88,
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            value,
            style: TextStyle(
              color: foreground,
              fontSize: 27,
              fontWeight: FontWeight.w700,
            ),
          ),
          Text(
            label,
            style: TextStyle(
              color: foreground,
              fontSize: 11,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    ),
  );
}

class _MessageCard extends StatelessWidget {
  const _MessageCard({required this.message});
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

class _ParentBottomNavigation extends StatelessWidget {
  const _ParentBottomNavigation({required this.selected, this.onSelected});
  final int selected;
  final ValueChanged<int>? onSelected;
  @override
  Widget build(BuildContext context) => TamDanBottomNavigation(
    currentIndex: selected,
    onDestinationSelected: onSelected ?? (_) {},
  );
}
