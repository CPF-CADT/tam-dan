import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../services/child_runtime.dart';
import '../view_model/child_view_model.dart';
import 'child_setup_widgets.dart';

class ChildVpnSetupScreen extends StatelessWidget {
  const ChildVpnSetupScreen({
    super.key,
    required this.onContinue,
    required this.viewModel,
  });
  final VoidCallback onContinue;
  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) {
    final supported =
        viewModel.runtimeStatus?.vpn == ChildCapabilityState.supported;
    return ChildSetupScaffold(
      child: Column(
        children: [
          const ChildSetupStepper(current: 1),
          const SizedBox(height: 44),
          const Icon(
            Icons.vpn_key_outlined,
            color: AppColors.primary,
            size: 68,
          ),
          const SizedBox(height: AppSpacing.xLarge),
          const Text(
            'Allow VPN connection',
            style: AppTextStyles.heading,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpacing.small),
          const Text(
            'Tam Dan uses a local VPN to check websites and apply online protections. It does not allow other apps to control your device.',
            textAlign: TextAlign.center,
            style: AppTextStyles.label,
          ),
          const SizedBox(height: AppSpacing.large),
          Container(
            padding: const EdgeInsets.all(AppSpacing.gap),
            decoration: BoxDecoration(
              color: AppColors.paleBlue,
              borderRadius: BorderRadius.circular(18),
            ),
            child: const Column(
              children: [
                _VpnGuarantee(
                  icon: Icons.public,
                  text: 'Checks website safety',
                ),
                _VpnGuarantee(
                  icon: Icons.block,
                  text: 'Blocks unsafe websites',
                ),
                _VpnGuarantee(
                  icon: Icons.sports_esports_outlined,
                  text: 'Helps apply game restrictions',
                ),
                _VpnGuarantee(
                  icon: Icons.lock_outline,
                  text: 'Keeps your data private',
                ),
              ],
            ),
          ),
          if (!supported) ...[
            const SizedBox(height: AppSpacing.small),
            const Text(
              'VPN enforcement is not supported on this device. You can continue, but Tam Dan will not claim a VPN connection.',
              style: TextStyle(color: AppColors.warning, fontSize: 11),
              textAlign: TextAlign.center,
            ),
          ],
          const Spacer(),
          ChildSetupButton(
            label: supported ? 'Allow VPN' : 'Continue to permissions',
            onPressed: onContinue,
          ),
          TextButton(onPressed: () {}, child: const Text('Learn more')),
        ],
      ),
    );
  }
}

class _VpnGuarantee extends StatelessWidget {
  const _VpnGuarantee({required this.icon, required this.text});
  final IconData icon;
  final String text;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 10),
    child: Row(
      children: [
        Icon(icon, color: AppColors.primary, size: 18),
        const SizedBox(width: 14),
        Text(text, style: AppTextStyles.label.copyWith(color: AppColors.ink)),
      ],
    ),
  );
}
