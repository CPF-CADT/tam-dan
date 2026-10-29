import 'package:flutter/material.dart';

class AppTheme {
  static const ink = Color(0xFF172033);
  static const navy = Color(0xFF263B66);
  static const mint = Color(0xFFBDE8D5);
  static const canvas = Color(0xFFF7F8FC);
  static const gap = 16.0;

  static ThemeData data() => ThemeData(
    colorScheme: ColorScheme.fromSeed(seedColor: navy, surface: canvas),
    scaffoldBackgroundColor: canvas,
    textTheme: const TextTheme(
      bodyLarge: TextStyle(color: ink),
      titleLarge: TextStyle(color: ink, fontWeight: FontWeight.w700),
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
