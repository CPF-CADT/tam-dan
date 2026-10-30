import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../view_model/child_view_model.dart';
import 'child_setup_widgets.dart';

class ChildEnableProtectionScreen extends StatelessWidget {
  const ChildEnableProtectionScreen({
    super.key,
    required this.onContinue,
    required this.viewModel,
  });
  final VoidCallback onContinue;
  final ChildViewModel viewModel;

  @override
  Widget build(BuildContext context) => ChildSetupScaffold(
    child: Column(
      children: [
        Container(
          width: 100,
          height: 100,
          decoration: const BoxDecoration(
            shape: BoxShape.circle,
            color: AppColors.paleBlue,
          ),
          child: const Icon(
            Icons.sentiment_satisfied_alt,
            color: AppColors.primary,
            size: 44,
          ),
        ),
        const SizedBox(height: AppSpacing.xLarge),
        const Text(
          'Keep you safe online',
          style: AppTextStyles.heading,
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: AppSpacing.small),
        const Text(
          'Turn on protection to enable monitoring and a secure local VPN connection.',
          textAlign: TextAlign.center,
          style: AppTextStyles.label,
        ),
        const SizedBox(height: 50),
        const _Capability(
          icon: Icons.view_list_outlined,
          title: 'Monitor app usage',
          detail: 'Helps your parent understand app time.',
        ),
        const _Capability(
          icon: Icons.public,
          title: 'Check website safety',
          detail: 'Blocks unsafe websites.',
        ),
        const _Capability(
          icon: Icons.check,
          title: 'Apply restrictions',
          detail: 'Follows your parent’s rules.',
        ),
        const Spacer(),
        ChildSetupButton(label: 'Turn on Protection', onPressed: onContinue),
        const SizedBox(height: AppSpacing.small),
        const Text(
          'Required permissions are explained before access.',
          style: AppTextStyles.label,
          textAlign: TextAlign.center,
        ),
      ],
    ),
  );
}

class _Capability extends StatelessWidget {
  const _Capability({
    required this.icon,
    required this.title,
    required this.detail,
  });
  final IconData icon;
  final String title;
  final String detail;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: AppSpacing.gap),
    child: Row(
      children: [
        CircleAvatar(
          radius: 21,
          backgroundColor: AppColors.paleBlue,
          child: Icon(icon, color: AppColors.primary, size: 20),
        ),
        const SizedBox(width: AppSpacing.small),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: AppTextStyles.title.copyWith(fontSize: 14)),
              Text(detail, style: AppTextStyles.label.copyWith(fontSize: 11)),
            ],
          ),
        ),
      ],
    ),
  );
}
