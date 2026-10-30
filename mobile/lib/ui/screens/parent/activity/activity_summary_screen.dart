import 'package:flutter/material.dart';

import '../../../../model/parent_models.dart';
import '../home/parent_visual_tokens.dart';
import '../view_model/parent_view_models.dart';

class ActivitySummaryScreen extends StatefulWidget {
  const ActivitySummaryScreen({
    super.key,
    required this.child,
    required this.viewModel,
  });
  final Child child;
  final UsageReportViewModel viewModel;
  @override
  State<ActivitySummaryScreen> createState() => _ActivitySummaryScreenState();
}

class _ActivitySummaryScreenState extends State<ActivitySummaryScreen> {
  int range = 0;
  @override
  void initState() {
    super.initState();
    _load();
  }

  void _load() {
    final now = DateTime.now().toUtc();
    widget.viewModel.load(
      widget.child.id,
      now.subtract(
        Duration(
          days: range == 0
              ? 1
              : range == 1
              ? 7
              : 30,
        ),
      ),
      now,
    );
  }

  @override
  Widget build(BuildContext context) {
    final state = widget.viewModel.state;
    final report = state.value;
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
      children: [
        const Text(
          'Activity',
          style: TextStyle(
            fontSize: 23,
            fontWeight: FontWeight.w800,
            color: ParentVisualTokens.ink,
          ),
        ),
        Text(
          'Today • ${widget.child.displayName}',
          style: const TextStyle(color: ParentVisualTokens.muted, fontSize: 12),
        ),
        const SizedBox(height: 16),
        SegmentedButton<int>(
          segments: const [
            ButtonSegment(value: 0, label: Text('Today')),
            ButtonSegment(value: 1, label: Text('7 Days')),
            ButtonSegment(value: 2, label: Text('30 Days')),
          ],
          selected: {range},
          onSelectionChanged: (value) {
            setState(() => range = value.first);
            _load();
          },
        ),
        const SizedBox(height: 22),
        const Text(
          'Screen time',
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w700,
            color: ParentVisualTokens.ink,
          ),
        ),
        Text(
          report == null ? 'Unavailable' : _freshnessLabel(report),
          style: const TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.w800,
            color: ParentVisualTokens.ink,
          ),
        ),
        const SizedBox(height: 8),
        _StateBanner(status: state.status, message: state.message),
        const SizedBox(height: 20),
        Card(
          child: SizedBox(
            height: 148,
            child: Center(
              child: Text(
                report == null
                    ? 'App-level history is not returned by the current API.'
                    : 'Chart data is not returned by the current API.',
                textAlign: TextAlign.center,
                style: const TextStyle(color: ParentVisualTokens.muted),
              ),
            ),
          ),
        ),
        const SizedBox(height: 20),
        const Text(
          'Most used apps',
          style: TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w700,
            color: ParentVisualTokens.ink,
          ),
        ),
        const SizedBox(height: 8),
        const Card(
          child: ListTile(
            leading: Icon(
              Icons.info_outline,
              color: ParentVisualTokens.primary,
            ),
            title: Text('Unavailable in this contract'),
            subtitle: Text('Tam Dan does not fabricate app usage data.'),
          ),
        ),
      ],
    );
  }

  String _freshnessLabel(UsageReport report) =>
      report.freshness == ReportFreshness.fresh
      ? 'Report received'
      : 'Report ${report.freshness.name}';
}

class _StateBanner extends StatelessWidget {
  const _StateBanner({required this.status, this.message});
  final LoadStatus status;
  final String? message;
  @override
  Widget build(BuildContext context) {
    if (status == LoadStatus.loading) return const LinearProgressIndicator();
    if (status == LoadStatus.failure ||
        status == LoadStatus.offline ||
        status == LoadStatus.unauthorized) {
      return Text(
        message ?? 'Activity could not be loaded.',
        style: const TextStyle(color: ParentVisualTokens.error),
      );
    }
    return const Text(
      'App history and exact screen-time totals require additional API fields.',
      style: TextStyle(color: ParentVisualTokens.muted, fontSize: 11),
    );
  }
}
