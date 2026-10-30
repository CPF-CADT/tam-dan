import 'package:flutter/foundation.dart';

import '../../core/network/api_client.dart';
import '../../data/repositories/parent_repository.dart';
import '../../model/parent_models.dart';

enum LoadStatus {
  idle,
  loading,
  ready,
  empty,
  offline,
  stale,
  unauthorized,
  unsupported,
  failure,
}

class AsyncState<T> {
  const AsyncState({this.status = LoadStatus.idle, this.value, this.message});
  final LoadStatus status;
  final T? value;
  final String? message;
  bool get isBusy => status == LoadStatus.loading;
  AsyncState<T> copyWith({LoadStatus? status, T? value, String? message}) =>
      AsyncState(
        status: status ?? this.status,
        value: value ?? this.value,
        message: message ?? this.message,
      );
}

abstract class BaseViewModel extends ChangeNotifier {
  String? _message;
  String? get message => _message;
  void clearMessage() {
    _message = null;
    notifyListeners();
  }

  String readableError(Object error) {
    if (error is ApiException) {
      if (error.isOffline) {
        return 'You appear to be offline. Check your connection and try again.';
      }
      if (error.isUnauthorized) {
        return 'Your session has expired. Sign in again.';
      }
      return 'Something went wrong. Please try again.';
    }
    if (error is ArgumentError) {
      return 'Check the entered values and try again.';
    }
    return 'Something went wrong. Please try again.';
  }
}

class AuthViewModel extends BaseViewModel {
  AuthViewModel(this._repository);
  final ParentRepository _repository;
  AsyncState<void> state = const AsyncState();
  bool registerMode = false;

  Future<bool> submit(String login, String password, {String? email}) async {
    _message = null;
    state = const AsyncState(status: LoadStatus.loading);
    notifyListeners();
    try {
      if (registerMode) {
        await _repository.register(
          login: login,
          email: email ?? '',
          password: password,
        );
      } else {
        await _repository.login(login, password);
      }
      state = const AsyncState(status: LoadStatus.ready);
      notifyListeners();
      return true;
    } catch (error) {
      _message = registerMode ? _registrationError(error) : _loginError(error);
      state = AsyncState(
        status: error is ApiException && error.isUnauthorized
            ? LoadStatus.unauthorized
            : error is ApiException && error.isOffline
            ? LoadStatus.offline
            : LoadStatus.failure,
        message: _message,
      );
      notifyListeners();
      return false;
    }
  }

  String _loginError(Object error) {
    if (error is ApiException && error.isOffline) {
      return readableError(error);
    }
    if (error is ApiException && error.isUnauthorized) {
      return readableError(error);
    }
    return 'We couldn’t sign you in. Check your details and try again.';
  }

  String _registrationError(Object error) {
    if (error is ApiException && error.isOffline) {
      return readableError(error);
    }
    return 'We couldn’t create your account. Check your details and try again.';
  }

  void showSocialAuthUnavailable(String provider) {
    _message = '$provider sign-in is not configured yet.';
    notifyListeners();
  }

  Future<void> recoverSession() async {
    state = const AsyncState(status: LoadStatus.loading);
    notifyListeners();
    try {
      await _repository.refreshSession();
      state = const AsyncState(status: LoadStatus.ready);
    } catch (error) {
      _message = readableError(error);
      state = AsyncState(status: LoadStatus.unauthorized, message: _message);
    }
    notifyListeners();
  }

  Future<void> logout() async {
    await _repository.logout();
    state = const AsyncState(status: LoadStatus.idle);
    notifyListeners();
  }
}

class ChildSelectionViewModel extends BaseViewModel {
  ChildSelectionViewModel(this._repository);
  final ParentRepository _repository;
  AsyncState<List<Child>> state = const AsyncState();
  Child? selected;

  Future<void> load() async {
    state = const AsyncState(status: LoadStatus.loading);
    notifyListeners();
    try {
      final children = await _repository.listChildren();
      selected = selected ?? (children.isEmpty ? null : children.first);
      state = AsyncState(
        status: children.isEmpty ? LoadStatus.empty : LoadStatus.ready,
        value: List.unmodifiable(children),
      );
    } catch (error) {
      state = AsyncState(
        status: error is ApiException && error.isOffline
            ? LoadStatus.offline
            : error is ApiException && error.isUnauthorized
            ? LoadStatus.unauthorized
            : LoadStatus.failure,
        message: readableError(error),
      );
    }
    notifyListeners();
  }

  void select(Child child) {
    selected = child;
    notifyListeners();
  }

  Future<void> create(String displayName) async {
    try {
      final child = await _repository.createChild(displayName);
      final current = [...state.value ?? const <Child>[], child];
      selected = child;
      state = AsyncState(
        status: LoadStatus.ready,
        value: List.unmodifiable(current),
      );
    } catch (error) {
      state = AsyncState(
        status: LoadStatus.failure,
        value: state.value,
        message: readableError(error),
      );
    }
    notifyListeners();
  }
}

class DeviceViewModel extends BaseViewModel {
  DeviceViewModel(this._repository);
  final ParentRepository _repository;
  AsyncState<List<Device>> state = const AsyncState();
  String? pairingCode;
  DateTime? pairingExpiresAt;

  Future<void> load(int childId) async {
    state = const AsyncState(status: LoadStatus.loading);
    notifyListeners();
    try {
      final devices = await _repository.listDevices(childId);
      state = AsyncState(
        status: devices.isEmpty ? LoadStatus.empty : LoadStatus.ready,
        value: List.unmodifiable(devices),
      );
    } catch (error) {
      state = AsyncState(
        status: error is ApiException && error.isOffline
            ? LoadStatus.offline
            : LoadStatus.failure,
        message: readableError(error),
      );
    }
    notifyListeners();
  }

  Future<void> createPairingCode(int childId) async {
    try {
      final result = await _repository.createPairingCode(childId);
      pairingCode = result['code'] as String?;
      pairingExpiresAt = DateTime.tryParse(
        result['expiresAt'] as String? ?? '',
      )?.toUtc();
    } catch (error) {
      _message = readableError(error);
    }
    notifyListeners();
  }
}

class UsageReportViewModel extends BaseViewModel {
  UsageReportViewModel(this._repository);
  final ParentRepository _repository;
  AsyncState<UsageReport> state = const AsyncState();

  Future<void> load(int childId, DateTime from, DateTime to) async {
    state = const AsyncState(status: LoadStatus.loading);
    notifyListeners();
    try {
      final report = await _repository.getUsageReport(childId, from, to);
      state = AsyncState(
        status:
            report.freshness == ReportFreshness.stale ||
                report.completeness == ReportCompleteness.partial
            ? LoadStatus.stale
            : LoadStatus.ready,
        value: report,
      );
    } catch (error) {
      state = AsyncState(
        status: error is ApiException && error.isOffline
            ? LoadStatus.offline
            : LoadStatus.failure,
        message: readableError(error),
      );
    }
    notifyListeners();
  }
}

class AlertViewModel extends BaseViewModel {
  AlertViewModel(this._repository);
  final ParentRepository _repository;
  AsyncState<List<Alert>> state = const AsyncState();

  Future<void> load(int childId) async {
    state = const AsyncState(status: LoadStatus.loading);
    notifyListeners();
    try {
      final alerts = await _repository.listAlerts(childId);
      state = AsyncState(
        status: alerts.isEmpty ? LoadStatus.empty : LoadStatus.ready,
        value: List.unmodifiable(alerts),
      );
    } catch (error) {
      state = AsyncState(
        status: error is ApiException && error.isOffline
            ? LoadStatus.offline
            : LoadStatus.failure,
        message: readableError(error),
      );
    }
    notifyListeners();
  }

  Future<void> review(int alertId, {String? note}) async {
    try {
      final updated = await _repository.reviewAlert(alertId, note: note);
      state = AsyncState(
        status: LoadStatus.ready,
        value: List.unmodifiable(
          (state.value ?? const <Alert>[]).map(
            (item) => item.id == updated.id ? updated : item,
          ),
        ),
      );
    } catch (error) {
      _message = readableError(error);
    }
    notifyListeners();
  }
}

class PolicyViewModel extends BaseViewModel {
  PolicyViewModel(this._repository);
  final ParentRepository _repository;
  AsyncState<List<Policy>> state = const AsyncState();

  Future<void> load(int childId) async {
    state = const AsyncState(status: LoadStatus.loading);
    notifyListeners();
    try {
      final policies = await _repository.listPolicies(childId);
      state = AsyncState(
        status: policies.isEmpty ? LoadStatus.empty : LoadStatus.ready,
        value: List.unmodifiable(policies),
      );
    } catch (error) {
      state = AsyncState(
        status: error is ApiException && error.isOffline
            ? LoadStatus.offline
            : LoadStatus.failure,
        message: readableError(error),
      );
    }
    notifyListeners();
  }

  Future<void> create(int childId, PolicyType type, String target) async {
    try {
      final policy = await _repository.createPolicy(
        childId,
        type: type,
        target: target,
      );
      state = AsyncState(
        status: LoadStatus.ready,
        value: List.unmodifiable([...state.value ?? const <Policy>[], policy]),
      );
    } catch (error) {
      _message = readableError(error);
      notifyListeners();
    }
  }
}
