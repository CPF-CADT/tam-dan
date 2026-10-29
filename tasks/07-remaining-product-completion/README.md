# PRD 7 — Remaining Product Completion

## Status

- State: `proposed`
- Priority: `P1` after integration hardening
- Owner: coordinator
- Depends on: approved OpenAPI v1.1.0, PRD 6 hardening, native PostgreSQL, Android emulator/device
- Merge gate: Integration Review approval

## Problem

The core contracts, ownership rules, backend foundation, Flutter foundation, Android secure-storage bridge, and native verification path are implemented. The five original product PRDs are not complete end-to-end.

Remaining work is product completion, not a restart of the old implementation audit.

## Goals

1. Complete the Parent Mobile user journeys from the approved Figma handoff.
2. Complete the Child Mobile setup, transparency, status, and recovery journeys.
3. Implement or explicitly scope Android monitoring and enforcement capabilities.
4. Complete Safety and Policy backend behavior for activity, reports, alerts, and policies.
5. Preserve parent/family/device ownership and injection-safe validation in every new feature.
6. Produce native runtime evidence for every completed journey.

## Non-goals

- Training or productionizing ML models.
- Claiming VPN/domain enforcement that the Android support matrix does not prove.
- Adding unrelated features outside the five original product PRDs.
- Merging or deploying before Integration Review approval.

## Product completion tracks

### Track A — Parent Mobile Application

Owner: `codex/mobile-development`

Complete these journeys:

- Registration, login, refresh, logout, expired-session recovery.
- Family and child selection.
- Child profile create, edit, deactivate, and ownership-safe empty/error states.
- Device pairing-code creation, device list, rename, revoke, and credential rotation.
- Usage reports with daily/weekly ranges, stale/partial/unknown states, and source-device coverage.
- Website-risk results and alerts.
- Alert review and dismissal.
- Application, game, and domain policy creation, update, pause, removal, and delivery status.
- Device health, permissions, sync status, and recovery guidance.

Acceptance criteria:

- Every Figma parent screen has loading, success, empty, offline, stale, unauthorized, failed, and unsupported states where applicable.
- Every child/device request is scoped to the authenticated parent family.
- Flutter models use the approved numeric ID and OpenAPI payloads.
- MVVM ViewModels contain state transitions; widgets do not call HTTP or platform channels directly.
- Flutter tests cover every repository/ViewModel journey.

### Track B — Child Mobile Application

Owner: `codex/mobile-development`

Complete these journeys:

- Transparent protection explanation and consent.
- Pairing-code entry with invalid, expired, used, and network-failure states.
- Permission setup and recovery after permission removal.
- Secure credential/device-context persistence.
- Monitoring and synchronization status.
- Active policy and restriction display.
- Offline display of last-known safe state.
- Revoked-device and expired-session recovery.

Acceptance criteria:

- The child app clearly explains collected data and required permissions.
- Secure storage survives restart and clears on logout/revocation.
- Device identity is derived from the authenticated pairing response, never from a placeholder.
- `ACTIVE` is shown only after device confirmation.
- Android emulator and, when required for release, physical-device evidence is attached.

### Track C — Android Monitoring and Enforcement

Owner: `codex/android-engine`

Complete only supported capabilities:

- Application usage observation.
- Permission and monitoring lifecycle.
- Durable offline event queue and synchronization.
- Supported domain observation.
- Application/game/domain policy status.
- VPN conflict and unsupported-capability reporting.
- Reboot, network loss, permission removal, and recovery behavior.

Acceptance criteria:

- A support matrix lists Android API levels, device vendors, permissions, and known limitations.
- `ACTIVE`, `FAILED`, `PENDING`, and `UNSUPPORTED` have deterministic meanings.
- Unsupported capabilities never silently succeed.
- Event identifiers are stable and duplicate uploads are rejected or deduplicated.
- Runtime evidence covers pairing, secure storage, permissions, reboot, network recovery, policy delivery, and status reporting.

If true VPN/domain enforcement is not available, keep the feature visibly `UNSUPPORTED` and update the product scope rather than faking enforcement.

### Track D — Identity and Device Backend

Owner: `codex/backend-development`

Complete remaining lifecycle behavior:

- Account activation and password recovery.
- Family/child lifecycle.
- Pairing-code creation and completion.
- Device list, rename, revoke, credential rotation, and audit history.
- Consent and permission records.
- Parent/device authorization separation.

Acceptance criteria:

- Cross-family access fails consistently.
- Pairing codes are expiring, single-use, throttled, and never logged in raw form.
- Revoked or inactive devices cannot authenticate, ingest data, or receive policies.
- Responses contain DTO allowlists only.
- Native PostgreSQL tests cover all lifecycle and authorization paths.

### Track E — Safety and Policy Backend

Owner: `codex/backend-development`

Complete remaining product behavior:

- Activity and domain-observation ingestion.
- Idempotent event storage and retention boundaries.
- Daily/weekly usage reports.
- Freshness, completeness, missing-device, duplicate, offline, delayed, and late-event semantics.
- Website-risk and `UNKNOWN` behavior.
- Parent alerts, review, dismissal, and deduplication.
- Application, game, and domain policy lifecycle.
- Policy delivery and device acknowledgement.
- Enforcement state transitions.

Acceptance criteria:

- Reports contain `generatedAt`, `dataThrough`, `freshness`, `completeness`, and nested `sourceDeviceCoverage`.
- Insufficient or unavailable evidence remains `UNKNOWN`, never `SAFE`.
- Policies are family-scoped and device acknowledgements cannot change another child’s policy.
- `ACTIVE` requires device confirmation.
- All free-form input is bounded, validated, and safe for persistence and logs.
- Native PostgreSQL integration tests cover the full flow from device event to parent report/alert/policy state.

### Track F — Security and release evidence

Owner: `codex/hardening-security`

Complete:

- OWASP ownership, authentication, injection, cryptography, logging, and configuration review.
- Maven/Flutter/Android dependency vulnerability scan.
- Secret scan with no active credential findings.
- Production profile/configuration review.
- Threat model for pairing, device credentials, reports, policies, and Android monitoring.
- Accepted-risk register for any unavailable scanner or unsupported capability.

Acceptance criteria:

- Every finding has severity, owner, mitigation, and expiry/acceptance decision.
- Vulnerability scan produces a reproducible result or has explicit release-owner acceptance.
- No raw password, token, device credential, pairing code, or sensitive activity data appears in logs or responses.

## Dependencies and order

1. Freeze the approved OpenAPI and Flutter–Kotlin contracts.
2. Complete backend Identity/Device and Safety/Policy API behavior.
3. Complete Parent and Child Flutter journeys against the contracts.
4. Complete supported Android runtime behavior.
5. Run backend native PostgreSQL tests.
6. Run Flutter and Android emulator/device tests.
7. Run security scans and update the risk register.
8. Request Integration Review.

Backend and mobile implementation may proceed in parallel only when the API and platform contracts are unchanged and branches/worktrees remain isolated.

## Verification matrix

| Area | Required commands/evidence |
|---|---|
| Backend | `./mvnw spotless:check`, `./mvnw test`, native PostgreSQL `./mvnw verify`, `./mvnw liquibase:validate` |
| Flutter | `flutter pub get`, `flutter analyze`, `flutter test`, `flutter test integration_test` |
| Android | `adb devices -l`, `flutter build apk --debug`, Android compile/assemble/unit checks, runtime checklist |
| Contract | OpenAPI parse/lint, controller drift test, serializer tests, route/payload compatibility tests |
| Security | dependency scan, secret scan, OWASP checklist, threat model, risk register |

Every result must record the exact revision, command, exit code, environment, and log path.

## Definition of done

- Tracks A–E acceptance criteria are complete or explicitly scoped as unsupported.
- Track F has reproducible security evidence or approved risk acceptance.
- Native PostgreSQL and Android runtime evidence are attached.
- All tests run against the exact implementation commits under review.
- Integration Review returns `APPROVED`.
- Only then may merge and deployment be separately approved.
