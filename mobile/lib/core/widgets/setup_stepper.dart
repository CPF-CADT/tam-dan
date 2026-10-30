import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_spacing.dart';
import '../theme/app_text_styles.dart';

class SetupStepper extends StatelessWidget {
  const SetupStepper({
    super.key,
    required this.steps,
    required this.currentStep,
  });

  final List<String> steps;
  final int currentStep;

  @override
  Widget build(BuildContext context) => Semantics(
    label: 'Setup progress, step ${currentStep + 1} of ${steps.length}',
    child: Row(
      children: [
        for (var index = 0; index < steps.length; index++)
          Expanded(
            child: Column(
              children: [
                Row(
                  children: [
                    if (index > 0)
                      Expanded(
                        child: Divider(
                          color: index <= currentStep
                              ? AppColors.primary
                              : AppColors.divider,
                        ),
                      ),
                    Container(
                      width: 24,
                      height: 24,
                      decoration: BoxDecoration(
                        color: index <= currentStep
                            ? AppColors.primary
                            : AppColors.divider,
                        shape: BoxShape.circle,
                      ),
                      alignment: Alignment.center,
                      child: index < currentStep
                          ? const Icon(
                              Icons.check,
                              size: 14,
                              color: Colors.white,
                            )
                          : Text(
                              '${index + 1}',
                              style: AppTextStyles.label.copyWith(
                                color: index == currentStep
                                    ? Colors.white
                                    : AppColors.muted,
                              ),
                            ),
                    ),
                    if (index < steps.length - 1)
                      Expanded(
                        child: Divider(
                          color: index < currentStep
                              ? AppColors.primary
                              : AppColors.divider,
                        ),
                      ),
                  ],
                ),
                const SizedBox(height: AppSpacing.xSmall),
                Text(
                  steps[index],
                  style: AppTextStyles.label.copyWith(
                    color: index <= currentStep
                        ? AppColors.ink
                        : AppColors.muted,
                  ),
                  textAlign: TextAlign.center,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
      ],
    ),
  );
}
