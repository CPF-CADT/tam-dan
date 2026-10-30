import 'package:flutter/material.dart';

import 'app_colors.dart';
import 'app_spacing.dart';
import 'app_text_styles.dart';

abstract final class AppTheme {
  // Compatibility aliases keep feature visuals unchanged while tokens live in
  // focused files for new code.
  static const ink = AppColors.ink;
  static const authAccent = AppColors.authAccent;
  static const authCanvas = AppColors.authCanvas;
  static const authCard = AppColors.authCard;
  static const authMuted = AppColors.authMuted;
  static const gap = AppSpacing.gap;
  static const authRadius = AppSpacing.authRadius;

  static ThemeData data() => ThemeData(
    colorScheme: ColorScheme.fromSeed(
      seedColor: AppColors.navy,
      surface: AppColors.canvas,
    ),
    scaffoldBackgroundColor: AppColors.canvas,
    textTheme: const TextTheme(
      bodyLarge: AppTextStyles.bodyLarge,
      titleLarge: AppTextStyles.titleLarge,
    ),
    inputDecorationTheme: const InputDecorationTheme(
      border: OutlineInputBorder(),
      filled: true,
      fillColor: Colors.white,
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        minimumSize: const Size.fromHeight(48),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        minimumSize: const Size.fromHeight(48),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      ),
    ),
  );
}
