import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';

class ChildSetupStepper extends StatelessWidget {
  const ChildSetupStepper({super.key, required this.current});
  final int current;

  @override
  Widget build(BuildContext context) => Row(
    children: List.generate(3, (index) {
      final step = index + 1;
      return Expanded(
        child: Row(
          children: [
            CircleAvatar(
              radius: 13,
              backgroundColor: step <= current
                  ? AppColors.primary
                  : AppColors.muted,
              child: Text(
                step <= current ? '✓' : '$step',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            if (step < 3)
              Expanded(
                child: Container(
                  height: 2,
                  color: step < current ? AppColors.primary : AppColors.divider,
                ),
              ),
          ],
        ),
      );
    }),
  );
}
