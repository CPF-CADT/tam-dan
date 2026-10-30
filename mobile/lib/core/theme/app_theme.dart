import 'package:flutter/material.dart';

class AppTheme {
  static const ink = Color(0xFF172033);
  static const navy = Color(0xFF263B66);
  static const authAccent = Color(0xFF3E6FB6);
  static const authCanvas = Color(0xFFEAF1F8);
  static const authCard = Colors.white;
  static const authFieldBorder = Color(0xFFD6E0EC);
  static const authMuted = Color(0xFF5E6B7E);
  static const mint = Color(0xFFBDE8D5);
  static const canvas = Color(0xFFF7F8FC);
  static const gap = 16.0;
  static const authRadius = 24.0;

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
