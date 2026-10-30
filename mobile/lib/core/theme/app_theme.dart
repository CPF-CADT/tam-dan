import 'package:flutter/material.dart';

import 'app_colors.dart';
import 'app_spacing.dart';
import 'app_text_styles.dart';
import 'app_radii.dart';

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
  static const titleLarge = AppTextStyles.titleLarge;

  static ThemeData data() => ThemeData(
    colorScheme: ColorScheme.fromSeed(
      seedColor: AppColors.primary,
      surface: AppColors.canvas,
    ),
    scaffoldBackgroundColor: AppColors.canvas,
    appBarTheme: const AppBarTheme(
      backgroundColor: AppColors.canvas,
      foregroundColor: AppColors.ink,
      elevation: 0,
      scrolledUnderElevation: 0,
      centerTitle: false,
      titleTextStyle: AppTextStyles.title,
    ),
    cardTheme: CardThemeData(
      color: Colors.white,
      surfaceTintColor: Colors.transparent,
      elevation: 0,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(AppRadii.card)),
        side: BorderSide(color: AppColors.divider),
      ),
    ),
    navigationBarTheme: NavigationBarThemeData(
      height: 72,
      backgroundColor: Colors.white,
      surfaceTintColor: Colors.transparent,
      indicatorColor: AppColors.paleBlue,
      indicatorShape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(AppRadii.pill)),
      ),
      labelTextStyle: const WidgetStatePropertyAll(AppTextStyles.label),
      iconTheme: const WidgetStatePropertyAll(
        IconThemeData(color: AppColors.muted, size: 22),
      ),
    ),
    textTheme: const TextTheme(
      bodyLarge: AppTextStyles.bodyLarge,
      titleLarge: AppTextStyles.titleLarge,
      titleMedium: AppTextStyles.title,
      bodyMedium: AppTextStyles.body,
      labelMedium: AppTextStyles.label,
    ),
    inputDecorationTheme: const InputDecorationTheme(
      border: OutlineInputBorder(),
      filled: true,
      fillColor: Colors.white,
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        minimumSize: const Size.fromHeight(48),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadii.pill),
        ),
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
