import 'package:flutter/material.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/utils/date_time_utils.dart';
import '../../../model/parent_models.dart';
import '../../widgets/view_model_builder.dart';
import '../../states/state_message.dart';
import 'activity/activity_summary_screen.dart';
import 'alerts/safety_alerts_screen.dart';
import 'controls/controls_hub_screen.dart';
import 'device/child_overview_screen.dart';
import 'home/parent_home_screen.dart';
import 'settings/device_settings_screen.dart';
import 'view_model/parent_view_models.dart';

part 'auth_screen.dart';
part 'parent_screen.dart';
part 'widgets/overview_tab.dart';
part 'widgets/alerts_tab.dart';
part 'widgets/policies_tab.dart';

class _AuthScreen extends StatefulWidget {
  const _AuthScreen({required this.viewModel, required this.onAuthenticated});
  final AuthViewModel viewModel;
  final VoidCallback onAuthenticated;
  @override
  State<_AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<_AuthScreen> {
  final login = TextEditingController();
  final password = TextEditingController();
  final email = TextEditingController();
  final loginFocus = FocusNode();
  final passwordFocus = FocusNode();
  final emailFocus = FocusNode();
  bool obscurePassword = true;
  String? validationMessage;

  @override
  void dispose() {
    login.dispose();
    password.dispose();
    email.dispose();
    loginFocus.dispose();
    passwordFocus.dispose();
    emailFocus.dispose();
    super.dispose();
  }

  bool _validate(AuthViewModel vm) {
    final loginValue = login.text.trim();
    final passwordValue = password.text;
    final emailValue = email.text.trim();
    setState(() {
      validationMessage =
          loginValue.isEmpty ||
              passwordValue.isEmpty ||
              (vm.registerMode && emailValue.isEmpty)
          ? 'Enter your ${vm.registerMode ? 'email, login, and ' : ''}password to continue.'
          : null;
    });
    return validationMessage == null;
  }

  Future<void> _submit(AuthViewModel vm) async {
    FocusManager.instance.primaryFocus?.unfocus();
    if (!_validate(vm)) return;
    if (await vm.submit(login.text, password.text, email: email.text) &&
        mounted) {
      widget.onAuthenticated();
    }
  }

  Widget _field({
    required TextEditingController controller,
    required FocusNode focusNode,
    required String label,
    required String hint,
    TextInputType? keyboardType,
    TextInputAction? textInputAction,
    FocusNode? nextFocus,
    bool obscureText = false,
    Widget? suffixIcon,
  }) {
    return TextField(
      controller: controller,
      focusNode: focusNode,
      keyboardType: keyboardType,
      textInputAction: textInputAction,
      obscureText: obscureText,
      onSubmitted: nextFocus == null ? null : (_) => nextFocus.requestFocus(),
      onTapOutside: (_) => FocusManager.instance.primaryFocus?.unfocus(),
      decoration: InputDecoration(
        labelText: label,
        hintText: hint,
        prefixIcon: Icon(
          label == 'Password' ? Icons.lock_outline : Icons.person_outline,
          semanticLabel: '$label icon',
        ),
        suffixIcon: suffixIcon,
      ),
    );
  }

  Widget _socialButton(
    BuildContext context,
    AuthViewModel vm, {
    required String provider,
    required Widget icon,
  }) {
    return OutlinedButton.icon(
      onPressed: vm.state.isBusy
          ? null
          : () {
              vm.showSocialAuthUnavailable(provider);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text('$provider sign-in is not configured yet.'),
                ),
              );
            },
      icon: icon,
      label: Text('Continue with $provider'),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.authCanvas,
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) => SingleChildScrollView(
            keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.onDrag,
            padding: EdgeInsets.fromLTRB(
              20,
              24,
              20,
              MediaQuery.viewInsetsOf(context).bottom + 24,
            ),
            child: ConstrainedBox(
              constraints: BoxConstraints(
                minHeight: constraints.maxHeight - 48,
                maxWidth: 440,
              ),
              child: Center(
                child: ViewModelBuilder<AuthViewModel>(
                  viewModel: widget.viewModel,
                  builder: (context, vm) => Card(
                    color: AppTheme.authCard,
                    elevation: 0,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(AppTheme.authRadius),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.all(28),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Semantics(
                            label: 'Tam Dan logo',
                            child: Align(
                              alignment: Alignment.centerLeft,
                              child: CircleAvatar(
                                radius: 28,
                                backgroundColor: AppTheme.authAccent,
                                child: Text(
                                  'TD',
                                  style: Theme.of(context).textTheme.titleMedium
                                      ?.copyWith(
                                        color: Colors.white,
                                        fontWeight: FontWeight.w800,
                                      ),
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(height: 22),
                          Text(
                            'Tam Dan',
                            style: Theme.of(context).textTheme.headlineMedium
                                ?.copyWith(
                                  color: AppTheme.ink,
                                  fontWeight: FontWeight.w800,
                                ),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            vm.registerMode
                                ? 'Create your parent account'
                                : 'Your family safety center',
                            style: Theme.of(context).textTheme.bodyLarge
                                ?.copyWith(color: AppTheme.authMuted),
                          ),
                          const SizedBox(height: 26),
                          if (vm.registerMode) ...[
                            _field(
                              controller: email,
                              focusNode: emailFocus,
                              label: 'Email',
                              hint: 'you@example.com',
                              keyboardType: TextInputType.emailAddress,
                              textInputAction: TextInputAction.next,
                              nextFocus: loginFocus,
                            ),
                            const SizedBox(height: 14),
                          ],
                          _field(
                            controller: login,
                            focusNode: loginFocus,
                            label: 'Login',
                            hint: 'Enter your login',
                            textInputAction: TextInputAction.next,
                            nextFocus: passwordFocus,
                          ),
                          const SizedBox(height: 14),
                          _field(
                            controller: password,
                            focusNode: passwordFocus,
                            label: 'Password',
                            hint: 'Enter your password',
                            textInputAction: TextInputAction.done,
                            obscureText: obscurePassword,
                            suffixIcon: IconButton(
                              tooltip: obscurePassword
                                  ? 'Show password'
                                  : 'Hide password',
                              onPressed: () => setState(
                                () => obscurePassword = !obscurePassword,
                              ),
                              icon: Icon(
                                obscurePassword
                                    ? Icons.visibility_outlined
                                    : Icons.visibility_off_outlined,
                              ),
                            ),
                          ),
                          if (validationMessage != null) ...[
                            const SizedBox(height: 10),
                            Text(
                              validationMessage!,
                              semanticsLabel: validationMessage,
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.error,
                              ),
                            ),
                          ],
                          const SizedBox(height: 18),
                          stateMessage(vm.state.status, message: vm.message),
                          ElevatedButton(
                            onPressed: vm.state.isBusy
                                ? null
                                : () => _submit(vm),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppTheme.authAccent,
                              foregroundColor: Colors.white,
                            ),
                            child: vm.state.isBusy
                                ? const SizedBox(
                                    height: 20,
                                    width: 20,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      color: Colors.white,
                                    ),
                                  )
                                : Text(
                                    vm.registerMode
                                        ? 'Create account'
                                        : 'Sign in',
                                  ),
                          ),
                          const SizedBox(height: 20),
                          Row(
                            children: [
                              const Expanded(child: Divider()),
                              Padding(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 12,
                                ),
                                child: Text(
                                  'OR',
                                  style: Theme.of(context).textTheme.labelSmall
                                      ?.copyWith(color: AppTheme.authMuted),
                                ),
                              ),
                              const Expanded(child: Divider()),
                            ],
                          ),
                          const SizedBox(height: 16),
                          _socialButton(
                            context,
                            vm,
                            provider: 'Google',
                            icon: const Text(
                              'G',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                          const SizedBox(height: 10),
                          _socialButton(
                            context,
                            vm,
                            provider: 'Apple',
                            icon: const Icon(Icons.apple),
                          ),
                          const SizedBox(height: 10),
                          Text(
                            'Social sign-in is not configured yet.',
                            textAlign: TextAlign.center,
                            style: Theme.of(context).textTheme.bodySmall
                                ?.copyWith(color: AppTheme.authMuted),
                          ),
                          const SizedBox(height: 12),
                          TextButton(
                            onPressed: vm.state.isBusy
                                ? null
                                : () => setState(() {
                                    vm.registerMode = !vm.registerMode;
                                    validationMessage = null;
                                    vm.clearMessage();
                                  }),
                            child: Text(
                              vm.registerMode
                                  ? 'Already have an account? Sign in'
                                  : 'New to Tam Dan? Create an account',
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _ParentShell extends StatefulWidget {
  const _ParentShell({
    required this.children,
    required this.devices,
    required this.reports,
    required this.alerts,
    required this.policies,
    required this.onLogout,
  });
  final ChildSelectionViewModel children;
  final DeviceViewModel devices;
  final UsageReportViewModel reports;
  final AlertViewModel alerts;
  final PolicyViewModel policies;
  final VoidCallback onLogout;
  @override
  State<_ParentShell> createState() => _ParentShellState();
}

class _ParentShellState extends State<_ParentShell> {
  int tab = 0;
  bool deviceDetail = false;
  @override
  void initState() {
    super.initState();
    widget.children.load();
  }

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<ChildSelectionViewModel>(
      viewModel: widget.children,
      builder: (context, children) {
        final child = children.selected;
        if (child == null) deviceDetail = false;
        return Scaffold(
          appBar: AppBar(
            title: child == null
                ? const Text('Family')
                : PopupMenuButton<Child>(
                    tooltip: 'Select child',
                    onSelected: (value) => setState(() {
                      children.select(value);
                      deviceDetail = false;
                    }),
                    itemBuilder: (context) => [
                      for (final option
                          in children.state.value ?? const <Child>[])
                        PopupMenuItem(
                          value: option,
                          child: Text(option.displayName),
                        ),
                    ],
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(child.displayName),
                        const Icon(Icons.arrow_drop_down),
                      ],
                    ),
                  ),
            actions: [
              if (child != null)
                IconButton(
                  tooltip: 'Safety alerts',
                  onPressed: () => Navigator.of(context).push<void>(
                    MaterialPageRoute(
                      builder: (_) => SafetyAlertsScreen(
                        child: child,
                        viewModel: widget.alerts,
                      ),
                    ),
                  ),
                  icon: const Icon(Icons.notifications_none),
                ),
              IconButton(
                tooltip: 'Sign out',
                onPressed: widget.onLogout,
                icon: const Icon(Icons.logout),
              ),
            ],
          ),
          body: child == null
              ? stateMessage(
                  children.state.status,
                  message: children.state.message,
                  retry: children.load,
                )
              : tab == 0
              ? (deviceDetail
                    ? ChildOverviewScreen(
                        child: child,
                        devices: widget.devices,
                        reports: widget.reports,
                        policies: widget.policies,
                        onActivity: () => setState(() {
                          deviceDetail = false;
                          tab = 1;
                        }),
                        onControls: () => setState(() {
                          deviceDetail = false;
                          tab = 2;
                        }),
                      )
                    : ParentHomeScreen(
                        child: child,
                        devices: widget.devices,
                        reports: widget.reports,
                        alerts: widget.alerts,
                        onOpenDevice: () => setState(() => deviceDetail = true),
                        onOpenActivity: () => setState(() => tab = 1),
                      ))
              : tab == 1
              ? ActivitySummaryScreen(child: child, viewModel: widget.reports)
              : tab == 2
              ? ControlsHubScreen(child: child, viewModel: widget.policies)
              : DeviceSettingsScreen(
                  child: child,
                  devices: widget.devices,
                  onLogout: widget.onLogout,
                  showBottomNavigation: false,
                ),
          bottomNavigationBar: NavigationBar(
            selectedIndex: tab,
            onDestinationSelected: (value) => setState(() => tab = value),
            destinations: const [
              NavigationDestination(
                icon: Icon(Icons.home_outlined),
                label: 'Home',
              ),
              NavigationDestination(
                icon: Icon(Icons.bar_chart_outlined),
                label: 'Activity',
              ),
              NavigationDestination(icon: Icon(Icons.tune), label: 'Controls'),
              NavigationDestination(
                icon: Icon(Icons.settings_outlined),
                label: 'Settings',
              ),
            ],
          ),
        );
      },
    );
  }
}

class _OverviewTab extends StatelessWidget {
  const _OverviewTab({
    required this.child,
    required this.devices,
    required this.reports,
  });
  final Child child;
  final DeviceViewModel devices;
  final UsageReportViewModel reports;

  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(AppTheme.gap),
    children: [
      Text('Overview', style: Theme.of(context).textTheme.headlineSmall),
      const SizedBox(height: 8),
      ViewModelBuilder<DeviceViewModel>(
        viewModel: devices,
        builder: (context, vm) {
          _loadDevices(vm);
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(AppTheme.gap),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text(
                    'Devices',
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  stateMessage(
                    vm.state.status,
                    message: vm.state.message,
                    retry: () => vm.load(child.id),
                  ),
                  ...?vm.state.value?.map(
                    (device) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: Icon(
                        device.status == DeviceStatus.active
                            ? Icons.phone_android
                            : Icons.warning_amber,
                      ),
                      title: Text(device.displayName),
                      subtitle: Text(
                        '${device.platform ?? 'Unknown platform'} • ${device.status.name}',
                      ),
                    ),
                  ),
                  if (vm.state.status == LoadStatus.empty)
                    OutlinedButton(
                      onPressed: () => vm.createPairingCode(child.id),
                      child: const Text('Create pairing code'),
                    ),
                  if (vm.pairingCode != null)
                    Text('Pairing code: ${vm.pairingCode}'),
                  if (vm.pairingExpiresAt != null)
                    Text(
                      'Expires: ${DateTimeUtils.localText(vm.pairingExpiresAt!)}',
                    ),
                ],
              ),
            ),
          );
        },
      ),
      const SizedBox(height: 12),
      ViewModelBuilder<UsageReportViewModel>(
        viewModel: reports,
        builder: (context, vm) {
          _loadReport(vm, child.id);
          final report = vm.state.value;
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(AppTheme.gap),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text(
                    'Usage report',
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  stateMessage(vm.state.status, message: vm.state.message),
                  if (report != null) ...[
                    Text(
                      'Data through ${DateTimeUtils.localText(report.dataThrough)}',
                    ),
                    Text('Freshness: ${report.freshness.name}'),
                    Text('Completeness: ${report.completeness.name}'),
                    Text('Risk: ${report.risk.name}'),
                  ],
                ],
              ),
            ),
          );
        },
      ),
    ],
  );

  void _loadDevices(DeviceViewModel vm) {
    if (vm.state.status == LoadStatus.idle) {
      WidgetsBinding.instance.addPostFrameCallback((_) => vm.load(child.id));
    }
  }

  void _loadReport(UsageReportViewModel vm, int childId) {
    if (vm.state.status == LoadStatus.idle) {
      final to = DateTime.now().toUtc();
      WidgetsBinding.instance.addPostFrameCallback(
        (_) => vm.load(childId, to.subtract(const Duration(days: 1)), to),
      );
    }
  }
}

class _AlertsTab extends StatelessWidget {
  const _AlertsTab({required this.child, required this.viewModel});
  final Child child;
  final AlertViewModel viewModel;
  @override
  Widget build(BuildContext context) => ViewModelBuilder<AlertViewModel>(
    viewModel: viewModel,
    builder: (context, vm) {
      if (vm.state.status == LoadStatus.idle) {
        WidgetsBinding.instance.addPostFrameCallback((_) => vm.load(child.id));
      }
      return ListView(
        padding: const EdgeInsets.all(AppTheme.gap),
        children: [
          Text('Alerts', style: Theme.of(context).textTheme.headlineSmall),
          stateMessage(
            vm.state.status,
            message: vm.state.message,
            retry: () => vm.load(child.id),
          ),
          ...?vm.state.value?.map((alert) => _alertCard(alert)),
        ],
      );
    },
  );

  Widget _alertCard(Alert alert) => Card(
    child: ListTile(
      title: Text(alert.title ?? 'Safety alert'),
      subtitle: Text(alert.detail ?? 'Review the available evidence.'),
      trailing: alert.state == AlertState.open
          ? TextButton(
              onPressed: () => viewModel.review(alert.id),
              child: const Text('Review'),
            )
          : Text(alert.state.name),
    ),
  );
}

class _PoliciesTab extends StatelessWidget {
  const _PoliciesTab({required this.child, required this.viewModel});
  final Child child;
  final PolicyViewModel viewModel;
  @override
  Widget build(BuildContext context) => ViewModelBuilder<PolicyViewModel>(
    viewModel: viewModel,
    builder: (context, vm) {
      if (vm.state.status == LoadStatus.idle) {
        WidgetsBinding.instance.addPostFrameCallback((_) => vm.load(child.id));
      }
      return ListView(
        padding: const EdgeInsets.all(AppTheme.gap),
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Policies',
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              IconButton(
                tooltip: 'Add policy',
                onPressed: () => showDialog<void>(
                  context: context,
                  builder: (_) =>
                      _PolicyDialog(childId: child.id, viewModel: vm),
                ),
                icon: const Icon(Icons.add),
              ),
            ],
          ),
          stateMessage(
            vm.state.status,
            message: vm.state.message,
            retry: () => vm.load(child.id),
          ),
          ...?vm.state.value?.map((policy) => _policyCard(policy)),
        ],
      );
    },
  );

  Widget _policyCard(Policy policy) => Card(
    child: ListTile(
      title: Text(policy.target),
      subtitle: Text('${policy.type.name} • version ${policy.version}'),
      trailing: Text(policy.state.name),
    ),
  );
}

class _PolicyDialog extends StatefulWidget {
  const _PolicyDialog({required this.childId, required this.viewModel});
  final int childId;
  final PolicyViewModel viewModel;
  @override
  State<_PolicyDialog> createState() => _PolicyDialogState();
}

class _PolicyDialogState extends State<_PolicyDialog> {
  final target = TextEditingController();
  PolicyType type = PolicyType.domain;
  @override
  void dispose() {
    target.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('New policy'),
    content: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        DropdownButtonFormField<PolicyType>(
          initialValue: type,
          items: PolicyType.values
              .map(
                (value) =>
                    DropdownMenuItem(value: value, child: Text(value.name)),
              )
              .toList(),
          onChanged: (value) => setState(() => type = value ?? type),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: target,
          maxLength: 255,
          decoration: const InputDecoration(
            labelText: 'Application, game, or domain',
          ),
        ),
      ],
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      ElevatedButton(
        onPressed: () async {
          await widget.viewModel.create(widget.childId, type, target.text);
          if (context.mounted) Navigator.pop(context);
        },
        child: const Text('Save'),
      ),
    ],
  );
}
