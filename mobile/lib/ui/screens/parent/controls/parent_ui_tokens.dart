import 'package:flutter/material.dart';

abstract final class ParentUiTokens {
  static const canvas = Color(0xFFFBFDFF);
  static const ink = Color(0xFF060B14);
  static const primary = Color(0xFF096BF5);
  static const muted = Color(0xFF52637D);
  static const paleBlue = Color(0xFFE0F0FF);
  static const paleMint = Color(0xFFDEFAED);
  static const success = Color(0xFF05AD6B);
  static const warning = Color(0xFFF28C05);
  static const warningSurface = Color(0xFFFFF2D1);
  static const error = Color(0xFFEB292E);
  static const errorSurface = Color(0xFFFFE3E5);
  static const divider = Color(0xFFE0E8F2);
  static const radius = 17.0;

  static const title = TextStyle(
    color: ink,
    fontSize: 23,
    fontWeight: FontWeight.w700,
  );
  static const section = TextStyle(
    color: ink,
    fontSize: 15,
    fontWeight: FontWeight.w700,
  );
  static const body = TextStyle(color: muted, fontSize: 12);
}
