import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_radii.dart';
import '../../../../core/theme/app_text_styles.dart';

class ChildSetupButton extends StatelessWidget {
  const ChildSetupButton({super.key, required this.label, this.onPressed});
  final String label;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: double.infinity,
    height: 52,
    child: DecoratedBox(
      decoration: BoxDecoration(
        color: onPressed == null ? AppColors.divider : AppColors.primary,
        borderRadius: BorderRadius.circular(AppRadii.pill),
      ),
      child: ElevatedButton(
        onPressed: onPressed,
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.transparent,
          shadowColor: Colors.transparent,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppRadii.pill),
          ),
        ),
        child: Text(
          label,
          style: AppTextStyles.title.copyWith(color: Colors.white),
        ),
      ),
    ),
  );
}
