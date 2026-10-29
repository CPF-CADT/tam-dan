# Contract decision proposal

Status: `approved — coordinator approval received`

These approved defaults unblock implementation while preserving the PRD rules. The versioned contract artifacts are in `agent-workflow/contracts/`.

## Public API

- Format: JSON over HTTPS.
- Versioning: `/api/v1` URL prefix.
- Errors: RFC 7807-compatible error body with stable `code`.
- Responses: existing `ApiResponse<T>` and `PageResponse` conventions.
- Parent authentication: short-lived parent access token plus rotated refresh token.
- Child authentication: separate device credential; never reuse a parent token.
- Batch ingestion: stable client event IDs, idempotent acceptance, per-record results, explicit schema version.
- Policy status: `PENDING`, `ACTIVE`, `FAILED`, `UNSUPPORTED`; only device acknowledgement can produce `ACTIVE`.
- Risk status: `SAFE`, `CAUTION`, `BLOCKED`, `UNKNOWN`; insufficient or unavailable evidence produces `UNKNOWN`.

## Flutter–Kotlin contract

- Version: `v1`.
- Channel: `com.tamdan/monitoring_v1`.
- Transport: JSON-like maps using Flutter `StandardMethodCodec`.
- Methods: `getPermissionStatus`, `requestVpnPermission`, `startMonitoring`, `stopMonitoring`, `getMonitoringStatus`, `getSyncStatus`, `getPolicyStatus`, `retrySynchronization`, `getCapabilities`.
- Events: a separate `EventChannel` with versioned status/error envelopes.
- Native errors: structured `code`, `message`, `recoverable`, and optional details; no secrets.
- Child Dart code calls a platform adapter, never a channel directly from a ViewModel.

## Identity-to-Safety contract

- Version: in-process Java interface `v1` initially, because both backend modules are in one Spring Boot application.
- Parent context: authenticated parent ID and family ID.
- Device context: active device ID, child profile ID, and family ID.
- Revocation: synchronous live identity checks plus a transaction-bound `DeviceRevocationEvent` for downstream caches.
- Excluded data: passwords, raw tokens, pairing codes, raw device credentials, and unnecessary child-sensitive data.

## ML adapters

- Version: separate `domain-classification-v1` and `public-content-classification-v1` interfaces.
- Both preserve `status`, confidence, model version, taxonomy version, and correlation ID.
- Timeout, unavailable, invalid response, inaccessible content, and low-confidence results never become `SAFE`.
- External calls use bounded timeout, validated response schemas, and cache keys that include normalized input and model version.

## Approval checklist

- [ ] Product accepts `/api/v1` URL versioning.
- [ ] Product accepts the status enums and `UNKNOWN` behavior.
- [ ] Product accepts the Flutter channel name and method list.
- [ ] Backend accepts in-process Identity-to-Safety v1.
- [ ] Android support/version/vendor matrix is recorded.
- [ ] Schedule, timezone/DST, policy conflict, retention, notification, and reauthentication rules are recorded.
- [ ] OpenAPI and companion contracts are generated and approved.
