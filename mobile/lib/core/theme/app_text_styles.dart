import 'package:flutter/material.dart';

import 'app_colors.dart';

abstract final class AppTextStyles {
  static const display = TextStyle(
    color: AppColors.ink,
    fontSize: 32,
    height: 40 / 32,
    fontWeight: FontWeight.w700,
  );
  static const heading = TextStyle(
    color: AppColors.ink,
    fontSize: 24,
    height: 30 / 24,
    fontWeight: FontWeight.w700,
  );
  static const title = TextStyle(
    color: AppColors.ink,
    fontSize: 16,
    height: 22 / 16,
    fontWeight: FontWeight.w700,
  );
  static const body = TextStyle(
    color: AppColors.ink,
    fontSize: 14,
    height: 20 / 14,
  );
  static const bodyLarge = body;
  static const label = TextStyle(
    color: AppColors.muted,
    fontSize: 12,
    height: 16 / 12,
    fontWeight: FontWeight.w500,
  );
  static const titleLarge = TextStyle(
    color: AppColors.ink,
    fontWeight: FontWeight.w700,
  );
}
