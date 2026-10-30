import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../model/child_models.dart';
import '../../../../services/child_runtime.dart';
import '../view_model/child_view_model.dart';
import 'child_setup_widgets.dart';

class ChildSetupCompleteScreen extends StatelessWidget {
  const ChildSetupCompleteScreen({
    super.key,
    required this.onContinue,
    required this.viewModel,
  });
  final VoidCallback onContinue;
  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) {
    final status = viewModel.runtimeStatus;
    final monitoring = status?.monitoring == MonitoringState.active;
    return ChildSetupScaffold(
      child: Column(
        children: [
          const ChildSetupStepper(current: 3),
          const SizedBox(height: 40),
          const CircleAvatar(
            radius: 60,
            backgroundColor: AppColors.paleMint,
            child: Icon(Icons.check, color: AppColors.success, size: 46),
          ),
          const SizedBox(height: AppSpacing.xLarge),
          const Text(
            'Protection is ready!',
            style: AppTextStyles.heading,
            textAlign: TextAlign.center,
          ),
          Text(
            monitoring
                ? 'Your device is connected to your parent’s account and protected.'
                : 'Monitoring is not confirmed until the required permissions are granted.',
            textAlign: TextAlign.center,
            style: AppTextStyles.label,
          ),
          const SizedBox(height: AppSpacing.large),
          ChildStatusRow(
            icon: Icons.vpn_key_outlined,
            title: 'VPN connection',
            subtitle: status?.vpn == ChildCapabilityState.supported
                ? 'Connected'
                : 'Unsupported on this device',
            action: '›',
            actionEnabled: false,
          ),
          const SizedBox(height: AppSpacing.small),
          ChildStatusRow(
            icon: Icons.grid_view_outlined,
            title: 'Permissions',
            subtitle: status?.permission == PermissionState.granted
                ? 'Granted'
                : 'Needs attention',
            action: '›',
            actionEnabled: false,
          ),
          const SizedBox(height: AppSpacing.small),
          const ChildStatusRow(
            icon: Icons.sync,
            title: 'Policy sync',
            subtitle: 'Waiting for first sync',
            action: '›',
            actionEnabled: false,
          ),
          const Spacer(),
          ChildSetupButton(label: 'Go to Home', onPressed: onContinue),
        ],
      ),
    );
  }
}
