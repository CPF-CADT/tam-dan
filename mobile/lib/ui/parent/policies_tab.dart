import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';
import '../../model/parent_models.dart';
import 'parent_view_models.dart';
import 'widgets/state_message.dart';
import 'widgets/view_model_builder.dart';

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
