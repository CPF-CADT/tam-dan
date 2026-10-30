import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';
import '../../core/theme/app_text_styles.dart';

class TamDanBottomNavigation extends StatelessWidget {
  const TamDanBottomNavigation({
    super.key,
    required this.currentIndex,
    required this.onDestinationSelected,
    this.destinations = const [
      NavigationDestination(icon: Icon(Icons.home_outlined), label: 'Home'),
      NavigationDestination(
        icon: Icon(Icons.bar_chart_outlined),
        label: 'Activity',
      ),
      NavigationDestination(
        icon: Icon(Icons.grid_view_outlined),
        label: 'Controls',
      ),
      NavigationDestination(
        icon: Icon(Icons.settings_outlined),
        label: 'Settings',
      ),
    ],
  });

  final int currentIndex;
  final ValueChanged<int> onDestinationSelected;
  final List<NavigationDestination> destinations;

  @override
  Widget build(BuildContext context) => NavigationBar(
    selectedIndex: currentIndex,
    onDestinationSelected: onDestinationSelected,
    height: 72,
    backgroundColor: Colors.white,
    indicatorColor: AppColors.paleBlue,
    labelTextStyle: WidgetStatePropertyAll(AppTextStyles.label),
    destinations: destinations,
  );
}
