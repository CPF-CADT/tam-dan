import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_spacing.dart';
import '../../core/theme/app_text_styles.dart';
import '../../core/widgets/icon_badge.dart';
import '../../core/widgets/status_tone.dart';

class ActivityPolicyRow extends StatelessWidget {
  const ActivityPolicyRow({
    super.key,
    required this.title,
    required this.subtitle,
    this.icon = Icons.apps_outlined,
    this.tone = StatusTone.info,
    this.trailing,
    this.onTap,
    this.progress,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final StatusTone tone;
  final Widget? trailing;
  final VoidCallback? onTap;
  final double? progress;

  @override
  Widget build(BuildContext context) {
    final content = Padding(
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.small),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          IconBadge(icon: icon, tone: tone),
          const SizedBox(width: AppSpacing.small),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: AppTextStyles.title),
                const SizedBox(height: 2),
                Text(subtitle, style: AppTextStyles.label),
                if (progress != null) ...[
                  const SizedBox(height: AppSpacing.xSmall),
                  LinearProgressIndicator(
                    value: progress!.clamp(0, 1),
                    minHeight: 4,
                    borderRadius: BorderRadius.circular(4),
                    backgroundColor: AppColors.divider,
                    color: tone.foreground,
                  ),
                ],
              ],
            ),
          ),
          if (trailing != null) ...[
            const SizedBox(width: AppSpacing.small),
            trailing!,
          ] else if (onTap != null)
            const Padding(
              padding: EdgeInsets.only(top: 8),
              child: Icon(Icons.chevron_right, color: AppColors.muted),
            ),
        ],
      ),
    );
    return Semantics(
      button: onTap != null,
      label: '$title, $subtitle',
      child: onTap == null ? content : InkWell(onTap: onTap, child: content),
    );
  }
}
