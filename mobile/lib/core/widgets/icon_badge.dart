import 'package:flutter/material.dart';

import '../theme/app_radii.dart';
import 'status_tone.dart';

class IconBadge extends StatelessWidget {
  const IconBadge({super.key, required this.icon, this.tone = StatusTone.info});

  final IconData icon;
  final StatusTone tone;

  @override
  Widget build(BuildContext context) => Semantics(
    excludeSemantics: true,
    child: Container(
      width: 36,
      height: 36,
      decoration: BoxDecoration(
        color: tone.background,
        borderRadius: BorderRadius.circular(AppRadii.pill),
      ),
      alignment: Alignment.center,
      child: Icon(icon, size: 18, color: tone.foreground),
    ),
  );
}
