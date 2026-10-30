import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../home/parent_visual_tokens.dart';
import '../view_model/parent_view_models.dart';
import 'app_rule_screen.dart';
import 'game_internet_screen.dart';
import 'website_safety_screen.dart';

class ControlsHubScreen extends StatefulWidget {
  const ControlsHubScreen({
    super.key,
    required this.child,
    required this.viewModel,
  });
  final Child child;
  final PolicyViewModel viewModel;
  @override
  State<ControlsHubScreen> createState() => _ControlsHubScreenState();
}

class _ControlsHubScreenState extends State<ControlsHubScreen> {
  @override
  void initState() {
    super.initState();
    widget.viewModel.load(widget.child.id);
  }

  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
    children: [
      const Text(
        'Controls',
        style: TextStyle(
          fontSize: 23,
          fontWeight: FontWeight.w800,
          color: ParentVisualTokens.ink,
        ),
      ),
      const Text(
        'Choose what Lina can access',
        style: TextStyle(color: ParentVisualTokens.muted, fontSize: 12),
      ),
      const SizedBox(height: 20),
      _ControlLink(
        icon: Icons.apps,
        title: 'Apps',
        detail: 'Limits, blocks, and schedules',
        onTap: () => Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) =>
                AppRuleScreen(child: widget.child, viewModel: widget.viewModel),
          ),
        ),
      ),
      _ControlLink(
        icon: Icons.language,
        title: 'Websites',
        detail: 'Review website policies and block a domain',
        onTap: () => Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => WebsiteSafetyScreen(
              child: widget.child,
              viewModel: widget.viewModel,
            ),
          ),
        ),
      ),
      _ControlLink(
        icon: Icons.sports_esports_outlined,
        title: 'Online games',
        detail: 'View game policies and confirmed state',
        onTap: () => Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => GameInternetScreen(
              child: widget.child,
              viewModel: widget.viewModel,
            ),
          ),
        ),
      ),
      _ControlLink(
        icon: Icons.schedule,
        title: 'Schedules',
        detail: 'Schedule fields are not in the current policy contract',
        onTap: null,
      ),
      const SizedBox(height: 18),
      const Text(
        'Active rules',
        style: TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.w700,
          color: ParentVisualTokens.ink,
        ),
      ),
      const SizedBox(height: 8),
      _RulesState(viewModel: widget.viewModel),
    ],
  );
}

class _ControlLink extends StatelessWidget {
  const _ControlLink({
    required this.icon,
    required this.title,
    required this.detail,
    required this.onTap,
  });
  final IconData icon;
  final String title;
  final String detail;
  final VoidCallback? onTap;
  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      enabled: onTap != null,
      onTap: onTap,
      leading: CircleAvatar(
        backgroundColor: ParentVisualTokens.paleBlue,
        child: Icon(icon, color: ParentVisualTokens.primary),
      ),
      title: Text(title),
      subtitle: Text(detail),
      trailing: const Icon(Icons.chevron_right),
    ),
  );
}

class _RulesState extends StatelessWidget {
  const _RulesState({required this.viewModel});
  final PolicyViewModel viewModel;
  @override
  Widget build(BuildContext context) {
    final state = viewModel.state;
    if (state.status == LoadStatus.loading)
      return const Center(child: CircularProgressIndicator());
    if (state.status == LoadStatus.failure ||
        state.status == LoadStatus.offline)
      return Text(
        state.message ?? 'Rules could not be loaded.',
        style: const TextStyle(color: ParentVisualTokens.error),
      );
    final rules = state.value ?? const <Policy>[];
    if (rules.isEmpty)
      return const Card(
        child: ListTile(
          title: Text('No active rules'),
          subtitle: Text('Rules will appear here after the API confirms them.'),
        ),
      );
    return Column(
      children: [
        for (final rule in rules)
          Card(
            child: ListTile(
              title: Text(rule.target),
              subtitle: Text('${rule.type.name} • ${rule.state.name}'),
              trailing: Text(
                rule.state == PolicyState.active
                    ? 'ACTIVE'
                    : rule.state.name.toUpperCase(),
                style: const TextStyle(
                  fontSize: 10,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ),
      ],
    );
  }
}
