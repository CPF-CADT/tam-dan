import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';
import '../../model/parent_models.dart';
import 'parent_view_models.dart';
import 'widgets/view_model_builder.dart';

Widget stateMessage(LoadStatus status, {String? message, VoidCallback? retry}) {
  final text = switch (status) {
    LoadStatus.loading => 'Loading…',
    LoadStatus.offline =>
      'You are offline. Last known data may be unavailable.',
    LoadStatus.unauthorized => 'Your session expired. Sign in again.',
    LoadStatus.stale => 'Some data is stale or incomplete.',
    LoadStatus.unsupported =>
      'This capability is not supported on the connected device.',
    LoadStatus.empty => 'Nothing to show yet.',
    LoadStatus.failure => message ?? 'Something went wrong.',
    _ => message ?? '',
  };
  if (text.isEmpty) return const SizedBox.shrink();
  return Padding(
    padding: const EdgeInsets.all(AppTheme.gap),
    child: Column(
      children: [
        Text(text, textAlign: TextAlign.center),
        if (retry != null && status != LoadStatus.loading)
          TextButton(onPressed: retry, child: const Text('Retry')),
      ],
    ),
  );
}

class AuthScreen extends StatefulWidget {
  const AuthScreen({
    super.key,
    required this.viewModel,
    required this.onAuthenticated,
  });
  final AuthViewModel viewModel;
  final VoidCallback onAuthenticated;
  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  final login = TextEditingController();
  final password = TextEditingController();
  final email = TextEditingController();
  @override
  void dispose() {
    login.dispose();
    password.dispose();
    email.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: ViewModelBuilder<AuthViewModel>(
                viewModel: widget.viewModel,
                builder: (context, vm) => Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Tam Dan',
                      style: Theme.of(context).textTheme.headlineLarge,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      vm.registerMode
                          ? 'Create a parent account'
                          : 'Your family safety center',
                    ),
                    const SizedBox(height: 28),
                    if (vm.registerMode) ...[
                      TextField(
                        controller: email,
                        keyboardType: TextInputType.emailAddress,
                        decoration: const InputDecoration(labelText: 'Email'),
                      ),
                      const SizedBox(height: 12),
                    ],
                    TextField(
                      controller: login,
                      decoration: const InputDecoration(labelText: 'Login'),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: password,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Password'),
                    ),
                    const SizedBox(height: 16),
                    stateMessage(vm.state.status, message: vm.message),
                    ElevatedButton(
                      onPressed: vm.state.isBusy
                          ? null
                          : () async {
                              if (await vm.submit(
                                login.text,
                                password.text,
                                email: email.text,
                              )) {
                                widget.onAuthenticated();
                              }
                            },
                      child: Text(
                        vm.registerMode ? 'Create account' : 'Sign in',
                      ),
                    ),
                    TextButton(
                      onPressed: vm.state.isBusy
                          ? null
                          : () => setState(
                              () => vm.registerMode = !vm.registerMode,
                            ),
                      child: Text(
                        vm.registerMode
                            ? 'I already have an account'
                            : 'Create an account',
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class ParentShell extends StatefulWidget {
  const ParentShell({
    super.key,
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
  State<ParentShell> createState() => _ParentShellState();
}

class _ParentShellState extends State<ParentShell> {
  int tab = 0;
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
        return Scaffold(
          appBar: AppBar(
            title: child == null
                ? const Text('Family')
                : PopupMenuButton<Child>(
                    tooltip: 'Select child',
                    onSelected: children.select,
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
              : IndexedStack(
                  index: tab,
                  children: [
                    OverviewTab(
                      child: child,
                      devices: widget.devices,
                      reports: widget.reports,
                    ),
                    AlertsTab(child: child, viewModel: widget.alerts),
                    PoliciesTab(child: child, viewModel: widget.policies),
                  ],
                ),
          bottomNavigationBar: NavigationBar(
            selectedIndex: tab,
            onDestinationSelected: (value) => setState(() => tab = value),
            destinations: const [
              NavigationDestination(
                icon: Icon(Icons.dashboard_outlined),
                label: 'Overview',
              ),
              NavigationDestination(
                icon: Icon(Icons.notifications_none),
                label: 'Alerts',
              ),
              NavigationDestination(icon: Icon(Icons.tune), label: 'Policies'),
            ],
          ),
        );
      },
    );
  }
}

class OverviewTab extends StatelessWidget {
  const OverviewTab({
    super.key,
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
                    Text('Expires: ${vm.pairingExpiresAt!.toLocal()}'),
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
                    Text('Data through ${report.dataThrough.toLocal()}'),
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

class AlertsTab extends StatelessWidget {
  const AlertsTab({super.key, required this.child, required this.viewModel});
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

class PoliciesTab extends StatelessWidget {
  const PoliciesTab({super.key, required this.child, required this.viewModel});
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
