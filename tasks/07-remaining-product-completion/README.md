# PRD 7 — Remaining Product Completion

## Status

- State: `draft-awaiting-approval`
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

## Detailed implementation specification — approval required

This section is the implementation contract for PRD 7. It is a design and planning document only. No agent may implement these items until the coordinator records explicit approval of this PRD and the affected OpenAPI contract.

### 1. Global rules and constraints

#### Requirements gates

1. Requirements approval is required before application-code changes.
2. OpenAPI v1.1.0 remains the baseline. Any route, request, response, enum, ID type, or error change requires a new versioned contract proposal and approval.
3. Backend and mobile work must use separate branches/worktrees.
4. Each task has one owner and one file boundary. Cross-boundary changes require coordinator reassignment.
5. Every changed boundary requires real automated tests. Unavailable checks must be reported as unavailable, never as passed.
6. Integration Review must approve before merge. Deployment is a separate approval.

#### Ownership rules

- The authenticated parent may access only resources whose family membership is established by the server.
- A child belongs to exactly one family in the active product model.
- A device belongs to exactly one child and one family.
- A device credential never grants parent privileges.
- Device-originated activity, acknowledgement, and synchronization writes must be scoped to the authenticated device and its child.
- Client-supplied `familyId`, `childId`, `deviceId`, `parentId`, or owner fields are selectors only; authorization is resolved from the authenticated principal and database relationships.
- Cross-family access returns the repository’s approved not-found/forbidden behavior consistently without leaking resource existence.
- Reports, alerts, policies, and acknowledgements must not cross family, child, or device boundaries.

#### Input and persistence rules

- Validate all body, path, query, enum, date, numeric, and free-text inputs at the API boundary.
- Apply maximum lengths, page-size limits, date-range limits, and bounded list sizes.
- Reject unknown enum values unless the OpenAPI contract explicitly permits them.
- Use parameterized repository queries only; never concatenate SQL, JPQL, shell, URL, or log-format fragments from user input.
- Normalize identifiers and trim user-entered display text before persistence.
- Never log passwords, JWTs, refresh tokens, device credentials, pairing codes, or sensitive activity content.
- Use DTOs at controller boundaries; do not return JPA entities directly.
- Persist timestamps in UTC and return ISO-8601 timestamps.

### 2. Backend class and function design

These are target responsibilities. Existing classes may be extended only when they retain one clear responsibility; otherwise create a focused class.

#### Identity and device domain

`AuthenticationService`

- `TokenResponse authenticate(LoginRequest request)`
- `TokenResponse refresh(RefreshRequest request)`
- `void logout(String refreshToken)`
- `void requestPasswordReset(String email)`
- `void resetPassword(String key, String newPassword)`

Rules: validate credentials, enforce account activation, rotate/revoke sessions, never return or log raw credentials, and rate-limit repeated failures.

`FamilyService`

- `FamilyResponse getOwnedFamily(AuthenticatedParentContext parent)`
- `ChildProfileResponse createChild(AuthenticatedParentContext parent, CreateChildRequest request)`
- `ChildProfileResponse updateChild(AuthenticatedParentContext parent, Long childId, UpdateChildRequest request)`
- `void deactivateChild(AuthenticatedParentContext parent, Long childId)`

Rules: derive family from the authenticated parent; verify child membership in every operation; prevent reassignment through client input; preserve audit events.

`PairingService`

- `PairingCodeResponse createCode(AuthenticatedParentContext parent, CreatePairingCodeRequest request)`
- `ChildContextResponse completePairing(CompletePairingRequest request)`
- `void revokePairing(AuthenticatedParentContext parent, Long deviceId)`

Rules: codes are random, hashed at rest, single-use, expiring, throttled, and never logged; completion creates a device credential scoped to the selected child; replay and expired codes fail deterministically.

`DeviceService`

- `PageResponse<DeviceResponse> listDevices(AuthenticatedParentContext parent, PageRequest page)`
- `DeviceResponse renameDevice(AuthenticatedParentContext parent, Long deviceId, RenameDeviceRequest request)`
- `DeviceCredentialResponse rotateCredential(AuthenticatedParentContext parent, Long deviceId)`
- `void revokeDevice(AuthenticatedParentContext parent, Long deviceId)`
- `DeviceResponse updatePermissions(AuthenticatedDeviceContext device, DevicePermissionRequest request)`

Rules: parent operations require family ownership; device operations require device identity; revoked devices cannot ingest or acknowledge; rotation invalidates the previous credential.

#### Safety and policy domain

`ActivityIngestionService`

- `IngestionResult ingest(AuthenticatedDeviceContext device, ActivityBatchRequest request)`
- `IngestionResult retry(AuthenticatedDeviceContext device, String idempotencyKey, ActivityBatchRequest request)`

Rules: enforce device-child-family ownership, validate event timestamps and bounds, deduplicate by stable event ID, reject malformed or oversized batches, and report accepted/rejected counts without exposing secrets.

`UsageReportService`

- `UsageReportResponse getReport(AuthenticatedParentContext parent, Long childId, ReportQuery query)`
- `ReportFreshness calculateFreshness(Instant generatedAt, Instant dataThrough, Instant now)`
- `Completeness calculateCompleteness(Collection<DeviceCoverage> coverage)`

Rules: reports include `generatedAt`, `dataThrough`, `freshness`, `completeness`, and `sourceDeviceCoverage`; missing, delayed, partial, or unknown evidence must not become `SAFE`; all child/device data is family-scoped.

`RiskAssessmentService`

- `RiskResult evaluate(ActivityWindow window)`
- `RiskResult classifyInsufficientEvidence(EvidenceState state)`

Rules: deterministic rule-based results are allowed; unavailable or insufficient evidence returns `UNKNOWN`; do not claim ML certainty or infer safety from missing data.

`AlertService`

- `PageResponse<AlertResponse> listAlerts(AuthenticatedParentContext parent, AlertQuery query)`
- `AlertResponse review(AuthenticatedParentContext parent, Long alertId, AlertReviewRequest request)`
- `void dismiss(AuthenticatedParentContext parent, Long alertId)`

Rules: alerts are family-scoped, review/dismiss actions are idempotent, duplicate alerts use a stable deduplication key, and alert content is bounded and redacted in logs.

`PolicyService`

- `PolicyResponse create(AuthenticatedParentContext parent, Long childId, CreatePolicyRequest request)`
- `PolicyResponse update(AuthenticatedParentContext parent, Long policyId, UpdatePolicyRequest request)`
- `void pause(AuthenticatedParentContext parent, Long policyId)`
- `void remove(AuthenticatedParentContext parent, Long policyId)`
- `PageResponse<PolicyDeliveryResponse> deliveryStatus(AuthenticatedParentContext parent, Long policyId)`

Rules: policies are family/child scoped, type-specific constraints are enforced, unsupported capabilities return `UNSUPPORTED`, and `ACTIVE` is valid only after device acknowledgement.

`PolicyAcknowledgementService`

- `AcknowledgementResponse acknowledge(AuthenticatedDeviceContext device, PolicyAcknowledgementRequest request)`
- `PolicyState getEffectiveState(AuthenticatedParentContext parent, Long policyId)`

Rules: a device may acknowledge only policies assigned to its own child; stale versions cannot overwrite newer versions; acknowledgement transitions are monotonic and auditable.

#### Required backend layering

```text
Controller/Resource -> Request DTO validation -> Application Service
  -> Ownership/authorization check -> Repository query
  -> Domain result -> Response DTO
```

Controllers must not contain business rules or direct repository access. Repositories must expose ownership-scoped query methods rather than loading broad data and filtering in memory.

### 3. Flutter MVVM design

The mobile implementation must follow MVVM.

#### Required layers

```text
Screen/Widget -> ViewModel -> Repository -> ApiClient / SecureStorage / PlatformChannel
```

Widgets must render state and dispatch user intents. Widgets must not call HTTP clients, repositories, secure storage, or Android channels directly.

#### Target classes

`AuthViewModel`

- `Future<void> login(String email, String password)`
- `Future<void> refreshSession()`
- `Future<void> logout()`
- `Future<void> recoverExpiredSession()`

State must distinguish `idle`, `loading`, `authenticated`, `unauthenticated`, `expired`, `offline`, and `error`.

`FamilyViewModel`

- `Future<void> loadFamily()`
- `Future<void> createChild(CreateChildInput input)`
- `Future<void> updateChild(ChildId id, UpdateChildInput input)`
- `Future<void> deactivateChild(ChildId id)`

Rules: clear stale data on account change, expose empty/loading/error states, and never construct ownership IDs from UI-only state.

`DeviceViewModel`

- `Future<void> loadDevices()`
- `Future<void> createPairingCode(ChildId childId)`
- `Future<void> renameDevice(DeviceId id, String name)`
- `Future<void> revokeDevice(DeviceId id)`
- `Future<void> rotateCredential(DeviceId id)`

Rules: show pending, active, revoked, offline, and unsupported states; never display raw credentials after the one-time handoff screen.

`ReportViewModel`

- `Future<void> loadReport(ChildId childId, ReportRange range)`
- `void selectRange(ReportRange range)`
- `void retry()`

Rules: render freshness/completeness/source-device coverage; stale or partial data must be visibly labeled; unknown risk must not render as safe.

`AlertViewModel` and `PolicyViewModel`

- Load, review/dismiss, create, update, pause, remove, retry, and refresh delivery state.
- Every mutation must update state only after the server response succeeds.
- Failed mutations must preserve the last known server state and expose a retry action.

#### Mobile constraints

- Numeric IDs must match OpenAPI and backend types.
- API serialization must be centralized in repository/client code.
- Secure storage must clear session/device credentials on logout or revocation.
- Loading, empty, offline, stale, unauthorized, failed, and unsupported states are mandatory where applicable.
- No placeholder device identity, fake enforcement success, or hard-coded production endpoint.

### 4. Android Kotlin design

`MonitoringEngine`

- `startMonitoring(): Result<MonitoringState>`
- `stopMonitoring(): Result<MonitoringState>`
- `getCapabilities(): CapabilityMatrix`
- `getStatus(): MonitoringStatus`

`ActivityEventStore`

- `enqueue(ActivityEvent event)`
- `List<ActivityEvent> pendingBatch(int limit)`
- `markUploaded(Set<String> eventIds)`
- `discardExpired(Instant cutoff)`

Rules: durable offline storage, stable event IDs, bounded queue size, duplicate-safe upload, and no sensitive data in debug logs.

`PolicyEnforcementEngine`

- `apply(Policy policy): EnforcementResult`
- `remove(PolicyId policyId): EnforcementResult`
- `getSupport(PolicyType type): SupportState`

Rules: supported behavior reports `ACTIVE` only after confirmation; unavailable VPN/domain enforcement reports `UNSUPPORTED`; permission loss reports `FAILED` or `PENDING` as defined by the state machine; no silent success.

`DeviceCredentialStore`

- `save(DeviceContext context)`
- `read(): DeviceContext?`
- `clear()`

Rules: Android Keystore-backed encryption, corrupt/missing data becomes a recoverable signed-out state, and credentials are never returned to logs or UI analytics.

### 5. State machines

#### Device state

```text
PAIRING_PENDING -> ACTIVE -> REVOKED
PAIRING_PENDING -> EXPIRED
ACTIVE -> FAILED (temporary health failure)
FAILED -> ACTIVE (successful resynchronization)
```

#### Policy state

```text
DRAFT -> PENDING_DELIVERY -> ACTIVE
PENDING_DELIVERY -> FAILED
ACTIVE -> PAUSED -> ACTIVE
ACTIVE -> REMOVED
```

`ACTIVE` requires a device acknowledgement for the exact policy version. A stale acknowledgement cannot activate a newer version.

#### Report state

```text
LOADING -> COMPLETE | PARTIAL | STALE | UNKNOWN | ERROR
```

`UNKNOWN` is required when evidence is insufficient. `PARTIAL` is required when one or more expected devices or time windows are missing.

### 6. Contract requirements

Before implementation:

1. Compare every target function with `agent-workflow/contracts/openapi-v1.yaml`.
2. Add missing schemas/routes only through an OpenAPI v1.1.x proposal.
3. Confirm all IDs, timestamps, enums, pagination, errors, and ownership semantics.
4. Generate or update contract tests before backend/mobile implementation.
5. Obtain coordinator approval for the contract before API code changes.

### 7. Task decomposition after approval

| Task | Owner | Boundary | Depends on |
|---|---|---|---|
| P7-01 contract gap matrix | planning/coordinator | OpenAPI and task records | PRD approval |
| P7-02 identity/device lifecycle | backend-development | `backend` identity/device packages | P7-01 |
| P7-03 safety/report/policy lifecycle | backend-development | `backend` safety/policy packages | P7-01, P7-02 |
| P7-04 parent MVVM journeys | mobile-development | `mobile/lib` parent features | P7-01 |
| P7-05 child MVVM journeys | mobile-development | `mobile/lib` child features | P7-01, P7-02 |
| P7-06 Android supported runtime | mobile-development | `mobile/android` | P7-01, P7-05 |
| P7-07 backend native tests | backend-testing | backend test sources and logs | P7-02, P7-03 |
| P7-08 mobile/device tests | mobile-testing | Flutter/Android tests and logs | P7-04, P7-05, P7-06 |
| P7-09 integration review | integration-review | review records only | P7-07, P7-08 |

No task may start until its requirements reference, approved contract version, branch/worktree, acceptance criteria, and verification commands are recorded.

### 8. Approval record

Current state: `WAITING FOR USER APPROVAL`.

Approval must explicitly confirm:

- this detailed PRD scope;
- backend, Flutter MVVM, and Android responsibilities;
- ownership, validation, and unsupported-capability rules;
- the task order and isolated workstreams;
- permission to create task records and begin planning only.

Until approval is recorded, agents must not implement application features.
