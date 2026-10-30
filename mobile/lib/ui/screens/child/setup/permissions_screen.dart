import 'package:flutter/material.dart';

import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../model/child_models.dart';
import '../../../../services/child_runtime.dart';
import '../view_model/child_view_model.dart';
import 'child_setup_widgets.dart';

class ChildPermissionsScreen extends StatelessWidget {
  const ChildPermissionsScreen({
    super.key,
    required this.onContinue,
    required this.viewModel,
  });
  final VoidCallback onContinue;
  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: viewModel,
    builder: (context, _) {
      final permission =
          viewModel.runtimeStatus?.permission ?? PermissionState.unknown;
      return ChildSetupScaffold(
        child: Column(
          children: [
            const ChildSetupStepper(current: 2),
            const SizedBox(height: AppSpacing.gap),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Allow monitoring permissions',
                style: AppTextStyles.titleLarge,
              ),
            ),
            const SizedBox(height: AppSpacing.small),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'These permissions help Tam Dan work properly.',
                style: AppTextStyles.label,
              ),
            ),
            const SizedBox(height: AppSpacing.gap),
            ChildStatusRow(
              icon: Icons.view_list_outlined,
              title: 'Usage access',
              subtitle: 'See which apps are used and for how long.',
              action: _action(permission),
              onAction: () =>
                  viewModel.requestPermission(ChildPermissionKind.usageAccess),
              actionEnabled: permission != PermissionState.granted,
            ),
            const SizedBox(height: AppSpacing.small),
            const ChildStatusRow(
              icon: Icons.grid_view_outlined,
              title: 'Draw over other apps',
              subtitle: 'Show restrictions when needed.',
              action: 'Unavailable',
              actionEnabled: false,
            ),
            const SizedBox(height: AppSpacing.small),
            ChildStatusRow(
              icon: Icons.notifications_none,
              title: 'Notification access',
              subtitle: 'Detect important activity.',
              action: permission == PermissionState.granted
                  ? 'Granted'
                  : 'Unavailable',
              actionEnabled: false,
            ),
            const SizedBox(height: AppSpacing.small),
            const ChildStatusRow(
              icon: Icons.battery_full,
              title: 'Battery optimization',
              subtitle: 'Keep monitoring active in background.',
              action: 'Unavailable',
              actionEnabled: false,
            ),
            const Spacer(),
            ChildSetupButton(label: 'Continue', onPressed: onContinue),
          ],
        ),
      );
    },
  );

  String _action(PermissionState permission) => switch (permission) {
    PermissionState.granted => 'Granted',
    PermissionState.unavailable => 'Unavailable',
    _ => 'Grant',
  };
}
