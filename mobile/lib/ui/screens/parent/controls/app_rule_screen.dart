import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../home/parent_visual_tokens.dart';
import '../view_model/parent_view_models.dart';

class AppRuleScreen extends StatefulWidget {
  const AppRuleScreen({
    super.key,
    required this.child,
    required this.viewModel,
  });
  final Child child;
  final PolicyViewModel viewModel;
  @override
  State<AppRuleScreen> createState() => _AppRuleScreenState();
}

class _AppRuleScreenState extends State<AppRuleScreen> {
  String access = 'limit';

  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
    children: [
      Row(
        children: [
          IconButton(
            onPressed: () => Navigator.of(context).pop(),
            icon: const Icon(Icons.chevron_left),
            tooltip: 'Back',
          ),
          const Text(
            'YouTube',
            style: TextStyle(
              fontSize: 23,
              fontWeight: FontWeight.w800,
              color: ParentVisualTokens.ink,
            ),
          ),
        ],
      ),
      const Text(
        'App rule',
        style: TextStyle(color: ParentVisualTokens.muted, fontSize: 12),
      ),
      const SizedBox(height: 18),
      Card(
        child: ListTile(
          leading: const CircleAvatar(
            backgroundColor: ParentVisualTokens.errorSurface,
            child: Icon(Icons.play_arrow, color: ParentVisualTokens.error),
          ),
          title: const Text('YouTube'),
          subtitle: const Text(
            'App target is sent through the existing policy API.',
          ),
        ),
      ),
      const SizedBox(height: 18),
      const Text(
        'Access',
        style: TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.w700,
          color: ParentVisualTokens.ink,
        ),
      ),
      Card(
        child: RadioGroup<String>(
          groupValue: access,
          onChanged: (_) {},
          child: Column(
            children: [
              RadioListTile<String>(
                value: 'allow',
                enabled: false,
                title: const Text('Allow'),
                subtitle: const Text('Full access'),
              ),
              RadioListTile<String>(
                value: 'limit',
                enabled: false,
                title: const Text('Limit'),
                subtitle: const Text(
                  'Policy target only; duration is not supported by the API',
                ),
              ),
              RadioListTile<String>(
                value: 'block',
                enabled: false,
                title: const Text('Block'),
                subtitle: const Text('No access'),
              ),
            ],
          ),
        ),
      ),
      const SizedBox(height: 18),
      const Text(
        'Schedule',
        style: TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.w700,
          color: ParentVisualTokens.ink,
        ),
      ),
      Card(
        child: SwitchListTile(
          value: false,
          onChanged: null,
          title: const Text('School days'),
          subtitle: const Text(
            'Scheduling is unavailable in the current contract.',
          ),
        ),
      ),
      const SizedBox(height: 12),
      Card(
        color: ParentVisualTokens.warningSurface,
        child: const ListTile(
          leading: Icon(Icons.info_outline, color: ParentVisualTokens.warning),
          title: Text('Access modes are unavailable'),
          subtitle: Text(
            'The current policy API does not represent Allow, Limit, or Block for an app. No policy will be saved.',
          ),
        ),
      ),
      FilledButton(onPressed: null, child: const Text('Save app rule')),
    ],
  );
}
