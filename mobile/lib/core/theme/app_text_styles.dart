import 'package:flutter/material.dart';

import 'app_colors.dart';

abstract final class AppTextStyles {
  static const bodyLarge = TextStyle(color: AppColors.ink);
  static const titleLarge = TextStyle(
    color: AppColors.ink,
    fontWeight: FontWeight.w700,
  );
}
