import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/widgets/metric_card.dart';
import 'package:mobile/core/widgets/primary_button.dart';
import 'package:mobile/core/widgets/segmented_tabs.dart';
import 'package:mobile/core/widgets/setup_stepper.dart';
import 'package:mobile/core/widgets/status_card.dart';
import 'package:mobile/ui/widgets/activity_policy_row.dart';
import 'package:mobile/ui/widgets/bottom_navigation.dart';
import 'package:mobile/ui/widgets/policy_rule_card.dart';

void main() {
  testWidgets('shared foundation renders accessible controls', (tester) async {
    var selected = 0;
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: Column(
            children: [
              PrimaryButton(label: 'Continue', onPressed: () {}),
              const MetricCard(label: 'Screen time', value: '3h 20m'),
              const StatusCard(label: 'Protection', value: 'Active'),
              SegmentedTabs<int>(
                items: const [0, 1],
                value: selected,
                labelBuilder: (item) => item == 0 ? 'Today' : '7 Days',
                onChanged: (value) => selected = value,
              ),
              const ActivityPolicyRow(title: 'YouTube', subtitle: '45 minutes'),
              const PolicyRuleCard(
                title: 'Bedtime',
                subtitle: '9:00 PM to 7:00 AM',
                status: 'Active',
              ),
              const SetupStepper(
                steps: ['VPN', 'Permissions', 'Complete'],
                currentStep: 1,
              ),
            ],
          ),
          bottomNavigationBar: TamDanBottomNavigation(
            currentIndex: selected,
            onDestinationSelected: (value) => selected = value,
          ),
        ),
      ),
    );

    expect(find.text('Continue'), findsOneWidget);
    expect(find.text('Screen time'), findsOneWidget);
    expect(find.text('Protection'), findsOneWidget);
    expect(find.text('Today'), findsOneWidget);
    expect(find.text('YouTube'), findsOneWidget);
    expect(find.text('Bedtime'), findsOneWidget);
    expect(find.text('Permissions'), findsOneWidget);
    expect(find.byType(NavigationBar), findsOneWidget);
  });
}
