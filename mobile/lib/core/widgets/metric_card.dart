import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radii.dart';
import '../theme/app_spacing.dart';
import '../theme/app_text_styles.dart';

class MetricCard extends StatelessWidget {
  const MetricCard({
    super.key,
    required this.label,
    required this.value,
    this.detail,
    this.tone = MetricTone.info,
    this.icon,
  });

  final String label;
  final String value;
  final String? detail;
  final MetricTone tone;
  final IconData? icon;

  @override
  Widget build(BuildContext context) => Semantics(
    label: [label, value, ?detail].join(', '),
    child: Container(
      padding: const EdgeInsets.all(AppSpacing.gap),
      decoration: BoxDecoration(
        color: tone.background,
        borderRadius: BorderRadius.circular(AppRadii.card),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // The pattern keeps the nullable field promoted for the widget.
          // ignore: use_null_aware_elements
          if (icon case final icon?)
            Icon(icon, color: tone.foreground, size: 18),
          Text(
            value,
            style: AppTextStyles.heading.copyWith(color: tone.foreground),
          ),
          const SizedBox(height: AppSpacing.xSmall),
          Text(
            label,
            style: AppTextStyles.label.copyWith(color: AppColors.ink),
          ),
          if (detail != null) ...[
            const SizedBox(height: AppSpacing.xSmall),
            Text(detail!, style: AppTextStyles.label),
          ],
        ],
      ),
    ),
  );
}

enum MetricTone { info, success, warning, error }

extension on MetricTone {
  Color get background => switch (this) {
    MetricTone.info => AppColors.paleBlue,
    MetricTone.success => AppColors.paleMint,
    MetricTone.warning => AppColors.warningSurface,
    MetricTone.error => AppColors.errorSurface,
  };

  Color get foreground => switch (this) {
    MetricTone.info => AppColors.primary,
    MetricTone.success => AppColors.success,
    MetricTone.warning => AppColors.warning,
    MetricTone.error => AppColors.error,
  };
}
