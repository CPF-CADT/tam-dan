import 'package:flutter/material.dart';

import '../theme/app_radii.dart';
import '../theme/app_spacing.dart';
import '../theme/app_text_styles.dart';
import 'icon_badge.dart';
import 'status_tone.dart';

class StatusCard extends StatelessWidget {
  const StatusCard({
    super.key,
    required this.label,
    required this.value,
    this.icon,
    this.tone = StatusTone.neutral,
    this.onTap,
  });

  final String label;
  final String value;
  final IconData? icon;
  final StatusTone tone;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final child = Padding(
      padding: const EdgeInsets.all(AppSpacing.gap),
      child: Row(
        children: [
          if (icon != null) ...[
            IconBadge(icon: icon!, tone: tone),
            const SizedBox(width: AppSpacing.small),
          ],
          Expanded(child: Text(label, style: AppTextStyles.label)),
          const SizedBox(width: AppSpacing.small),
          Flexible(
            child: Text(
              value,
              textAlign: TextAlign.end,
              style: AppTextStyles.title.copyWith(color: tone.foreground),
            ),
          ),
        ],
      ),
    );
    return Semantics(
      button: onTap != null,
      label: '$label: $value',
      child: Card(
        margin: EdgeInsets.zero,
        elevation: 0,
        color: tone.background,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadii.card),
        ),
        child: onTap == null
            ? child
            : InkWell(
                onTap: onTap,
                borderRadius: BorderRadius.circular(AppRadii.card),
                child: child,
              ),
      ),
    );
  }
}
