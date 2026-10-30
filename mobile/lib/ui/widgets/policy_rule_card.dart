import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_text_styles.dart';
import 'activity_policy_row.dart';

class PolicyRuleCard extends StatelessWidget {
  const PolicyRuleCard({
    super.key,
    required this.title,
    required this.subtitle,
    required this.status,
    this.onTap,
  });

  final String title;
  final String subtitle;
  final String status;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) => Card(
    margin: EdgeInsets.zero,
    elevation: 0,
    color: Colors.white,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(18),
      side: const BorderSide(color: AppColors.divider),
    ),
    child: ActivityPolicyRow(
      title: title,
      subtitle: subtitle,
      icon: Icons.tune_outlined,
      trailing: Text(status, style: AppTextStyles.label),
      onTap: onTap,
    ),
  );
}
