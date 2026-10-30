import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../theme/app_radii.dart';
import '../theme/app_spacing.dart';
import '../theme/app_text_styles.dart';

class SegmentedTabs<T> extends StatelessWidget {
  const SegmentedTabs({
    super.key,
    required this.items,
    required this.value,
    required this.onChanged,
    this.labelBuilder,
  });

  final List<T> items;
  final T value;
  final ValueChanged<T> onChanged;
  final String Function(T value)? labelBuilder;

  @override
  Widget build(BuildContext context) => Semantics(
    container: true,
    label: 'Selection tabs',
    child: Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: AppColors.paleBlue,
        borderRadius: BorderRadius.circular(AppRadii.pill),
      ),
      child: Row(
        children: [
          for (final item in items)
            Expanded(
              child: Semantics(
                button: true,
                selected: item == value,
                label: labelBuilder?.call(item) ?? '$item',
                child: SizedBox(
                  height: 48,
                  child: TextButton(
                    onPressed: () => onChanged(item),
                    style: TextButton.styleFrom(
                      backgroundColor: item == value ? Colors.white : null,
                      foregroundColor: item == value
                          ? AppColors.primary
                          : AppColors.muted,
                      padding: const EdgeInsets.symmetric(
                        horizontal: AppSpacing.xSmall,
                      ),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppRadii.pill),
                      ),
                    ),
                    child: Text(
                      labelBuilder?.call(item) ?? '$item',
                      style: AppTextStyles.label.copyWith(
                        color: item == value
                            ? AppColors.primary
                            : AppColors.muted,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ),
              ),
            ),
        ],
      ),
    ),
  );
}
