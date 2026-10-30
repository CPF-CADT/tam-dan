import 'package:flutter/material.dart';

import '../../../core/theme/app_theme.dart';
import '../parent_view_models.dart';

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
