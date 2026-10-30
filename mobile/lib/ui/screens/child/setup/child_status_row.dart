import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_radii.dart';
import '../../../../core/theme/app_spacing.dart';
import '../../../../core/theme/app_text_styles.dart';

class ChildStatusRow extends StatelessWidget {
  const ChildStatusRow({
    super.key,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.action,
    this.onAction,
    this.actionEnabled = true,
  });
  final IconData icon;
  final String title;
  final String subtitle;
  final String action;
  final VoidCallback? onAction;
  final bool actionEnabled;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(AppSpacing.small),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(AppRadii.card),
    ),
    child: Row(
      children: [
        CircleAvatar(
          radius: 22,
          backgroundColor: AppColors.paleBlue,
          child: Icon(icon, color: AppColors.primary, size: 20),
        ),
        const SizedBox(width: AppSpacing.small),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: AppTextStyles.title.copyWith(fontSize: 13)),
              Text(subtitle, style: AppTextStyles.label.copyWith(fontSize: 10)),
            ],
          ),
        ),
        TextButton(
          onPressed: actionEnabled ? onAction : null,
          child: Text(action),
        ),
      ],
    ),
  );
}
