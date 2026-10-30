import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';

class ChildSetupScaffold extends StatelessWidget {
  const ChildSetupScaffold({
    super.key,
    required this.child,
    this.showBack = true,
  });
  final Widget child;
  final bool showBack;

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: AppColors.canvas,
    body: SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(
          AppSpacing.screen,
          AppSpacing.small,
          AppSpacing.screen,
          AppSpacing.screen,
        ),
        child: Column(
          children: [
            Row(
              children: [
                if (showBack)
                  IconButton(
                    tooltip: 'Back',
                    onPressed: () => Navigator.maybePop(context),
                    icon: const Icon(Icons.chevron_left, size: 32),
                    padding: EdgeInsets.zero,
                    constraints: const BoxConstraints(
                      minWidth: 40,
                      minHeight: 48,
                    ),
                  )
                else
                  const SizedBox(width: 40),
                const Text('Tam Dan', style: AppTextStyles.title),
              ],
            ),
            const SizedBox(height: AppSpacing.gap),
            Expanded(child: child),
          ],
        ),
      ),
    ),
  );
}
