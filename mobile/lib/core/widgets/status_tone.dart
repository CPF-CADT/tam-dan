import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

enum StatusTone { neutral, success, warning, error, info }

extension StatusToneColors on StatusTone {
  Color get background => switch (this) {
    StatusTone.success => AppColors.paleMint,
    StatusTone.warning => AppColors.warningSurface,
    StatusTone.error => AppColors.errorSurface,
    StatusTone.info => AppColors.paleBlue,
    StatusTone.neutral => Colors.white,
  };

  Color get foreground => switch (this) {
    StatusTone.success => AppColors.success,
    StatusTone.warning => AppColors.warning,
    StatusTone.error => AppColors.error,
    StatusTone.info => AppColors.primary,
    StatusTone.neutral => AppColors.ink,
  };
}
