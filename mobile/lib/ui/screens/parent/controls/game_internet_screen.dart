import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../../../widgets/bottom_navigation.dart';
import '../view_model/parent_view_models.dart';
import 'parent_ui_tokens.dart';

class GameInternetScreen extends StatelessWidget {
  const GameInternetScreen({
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
  Widget build(BuildContext context) {
    final games =
        viewModel.state.value
            ?.where((policy) => policy.type == PolicyType.game)
            .toList(growable: false) ??
        const <Policy>[];
    return Scaffold(
      backgroundColor: ParentUiTokens.canvas,
      appBar: AppBar(
        title: const Text('Game internet'),
        backgroundColor: ParentUiTokens.canvas,
        foregroundColor: ParentUiTokens.ink,
      ),
      body: RefreshIndicator(
        onRefresh: () => viewModel.load(child.id),
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
          children: [
            Text(
              'Manage game access for ${child.displayName}',
              style: ParentUiTokens.body,
            ),
            const SizedBox(height: 24),
            if (viewModel.state.status == LoadStatus.loading)
              const Center(child: CircularProgressIndicator()),
            if (viewModel.state.status == LoadStatus.offline)
              const _GameMessage('Game rules are unavailable offline.'),
            if (viewModel.state.status == LoadStatus.failure)
              _GameMessage(
                viewModel.state.message ?? 'Game rules could not be loaded.',
              ),
            if (games.isEmpty &&
                viewModel.state.status != LoadStatus.loading &&
                viewModel.state.status != LoadStatus.failure &&
                viewModel.state.status != LoadStatus.offline)
              const _GameMessage(
                'No game rules are configured yet. The current API does not expose a game catalog.',
              ),
            for (final policy in games)
              Card(
                elevation: 0,
                margin: const EdgeInsets.only(bottom: 10),
                child: ListTile(
                  title: Text(
                    policy.target,
                    style: const TextStyle(fontWeight: FontWeight.w700),
                  ),
                  subtitle: Text(
                    policy.state == PolicyState.pendingDelivery
                        ? 'Waiting for device confirmation'
                        : policy.state.name,
                  ),
                  trailing: policy.state == PolicyState.active
                      ? TextButton(
                          onPressed: () => viewModel.pause(child.id, policy.id),
                          child: const Text('Pause'),
                        )
                      : Text(policy.state.name),
                ),
              ),
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: ParentUiTokens.warningSurface,
                borderRadius: BorderRadius.circular(17),
              ),
              child: const Text(
                'Allow, schedule, and pause are shown only when represented by an existing policy. Game enforcement remains unsupported until the child device confirms it.',
                style: TextStyle(color: ParentUiTokens.warning, fontSize: 12),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: showBottomNavigation
          ? TamDanBottomNavigation(
              currentIndex: 2,
              onDestinationSelected: onNavigate ?? (_) {},
            )
          : null,
    );
  }
}

class _GameMessage extends StatelessWidget {
  const _GameMessage(this.message);
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
