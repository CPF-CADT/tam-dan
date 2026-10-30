import 'package:flutter/material.dart';

import '../home/child_home_screen.dart';
import '../restrictions/child_restrictions_screen.dart';
import '../settings/child_settings_screen.dart';
import '../view_model/child_view_model.dart';
import 'child_activity_screen.dart';

class ChildNavigationShell extends StatefulWidget {
  const ChildNavigationShell({super.key, required this.viewModel});

  final ChildViewModel viewModel;

  @override
  State<ChildNavigationShell> createState() => _ChildNavigationShellState();
}

class _ChildNavigationShellState extends State<ChildNavigationShell> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) => Scaffold(
    body: IndexedStack(
      index: _selectedIndex,
      children: [
        ChildHomeScreen(
          viewModel: widget.viewModel,
          onNavigate: _selectDestination,
        ),
        ChildActivityScreen(viewModel: widget.viewModel),
        ChildRestrictionsScreen(viewModel: widget.viewModel),
        ChildSettingsScreen(viewModel: widget.viewModel),
      ],
    ),
    bottomNavigationBar: NavigationBar(
      selectedIndex: _selectedIndex,
      onDestinationSelected: (index) => setState(() => _selectedIndex = index),
      destinations: const [
        NavigationDestination(icon: Icon(Icons.home_outlined), label: 'Home'),
        NavigationDestination(
          icon: Icon(Icons.bar_chart_outlined),
          label: 'Activity',
        ),
        NavigationDestination(
          icon: Icon(Icons.tune_outlined),
          label: 'Restrictions',
        ),
        NavigationDestination(
          icon: Icon(Icons.settings_outlined),
          label: 'Settings',
        ),
      ],
    ),
  );

  void _selectDestination(int index) {
    if (index < 0 || index > 3) return;
    setState(() => _selectedIndex = index);
  }
}
