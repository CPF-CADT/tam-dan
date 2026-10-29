# Task: PRD 7 contract and implementation gap matrix

- ID: `P7-01`
- Role: `planning`
- State: `complete`
- Branch/worktree: coordinator planning boundary; documentation only
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29 by user request to process with multiple agents
- OpenAPI approval: `v1.1.0`, `agent-workflow/contracts/openapi-v1.yaml`
- Figma/design approval: `tasks/active/02-ui-design-result.md`; use existing approved design decisions
- Dependencies: none

## Scope

Compare PRD 7 functions, existing backend/mobile/Android code, and OpenAPI v1.1.0. Produce a route/schema/ownership gap matrix and bounded follow-up tasks. Do not implement application code or change the OpenAPI contract.

## Acceptance criteria

- [x] Every PRD 7 backend/mobile/Android function is mapped to existing code or a bounded task.
- [x] Every API gap identifies whether v1.1.0 already supports it or requires a new contract proposal.
- [x] Ownership, validation, state-machine, and test gaps are explicit.
- [x] No duplicate file ownership exists between follow-up tasks.

## Verification

Required commands:

```text
rg --files backend mobile agent-workflow/contracts
rg -n "path:|operationId:|class |Future<|fun |TODO|UNSUPPORTED" backend mobile agent-workflow/contracts/openapi-v1.yaml
```

## Handoff

- Changed files: this task/document only
- Risks/compatibility: API changes remain blocked until a versioned contract is approved
- Review status: coordinator review required before implementation dispatch

## Contract and implementation gap matrix

### Contract baseline

The only approved API baseline is `agent-workflow/contracts/openapi-v1.yaml`, version `1.1.0`.
It defines 19 operations: authentication (5), child/family (2), pairing (2), device health/activity/domain/policy operations (5), reports and alerts (3), and policy management (2). The contract uses numeric `int64` IDs, RFC 7807-style problems, `ApiResponse` envelopes, parent JWT authentication, and the `X-Device-Credential` device boundary.

Rules for all follow-up work:

1. An operation marked **implement v1.1.0** may be implemented without changing the contract, but controller paths, payloads, status codes, security scheme, and ownership behavior must match it exactly.
2. An operation marked **contract proposal required** must not be implemented until a versioned OpenAPI v1.1.x proposal is written, reviewed, and approved by the coordinator.
3. Existing backend routes are not automatically approved substitutes for contract routes. A compatibility route requires an explicit contract decision.
4. No client may infer ownership from a submitted `familyId`, `parentId`, `childId`, or `deviceId`; the server resolves ownership from the authenticated principal/device credential.

### Backend identity and device matrix

| PRD responsibility / v1.1.0 operation | Current implementation | Gap and disposition | Owner / boundary |
|---|---|---|---|
| `registerParent`, `loginParent`, `refreshParent`, `logoutParent`, `logoutAllParentSessions` | `AuthenticationService`, `AuthenticateController`, `AccountResource`; routes are legacy `/api/authenticate*` and `/api/register` style | Business capability exists. Route and exact response mapping must be reconciled with `/auth/*`; implement against v1.1.0 if the existing contract is authoritative. Do not add password-recovery routes without a contract proposal. | P7-02, `backend/src/main/java/com/tamdan/{controller,service,web,dto}` |
| `listChildren`, `createChild` | `ChildProfileResource`, `ChildProfileService`; current route `/api/children` | Ownership and DTO foundations exist. Add/align the v1.1.0 family-scoped route `/families/me/children`; update tests for parent-family scoping. Implement v1.1.0. | P7-02, child controller/service/repository/DTO tests |
| Child edit/deactivate lifecycle | `ChildProfileResource` has `PUT` and `DELETE` under `/api/children/{childId}` | v1.1.0 has no edit/deactivate operations. Requires a new approved contract proposal before API implementation. | P7-02, only child lifecycle backend boundary |
| `createPairingCode` | `PairingResource`, `PairingService`; current route `/api/pairing-codes` accepts a request body | v1.1.0 requires `/families/me/children/{childId}/pairing-codes` with child path ownership. Align only after contract/controller tests confirm request/response shape. Implement v1.1.0. | P7-02, pairing files |
| `completeDevicePairing` | `DevicePairingResource`, `PairingService`; current route `/api/pairing/complete` | Same operation exists, but response and route must match v1.1.0 `/pairing/complete`. Implement v1.1.0; preserve single-use, expiry, hash-at-rest, throttling, and one-time credential rules. | P7-02, pairing/device credential files |
| `listDevices` | `DeviceResource`; current route uses query `childId` under `/api/devices` | v1.1.0 requires `/families/me/children/{childId}/devices`. Align route and parent ownership query. Implement v1.1.0. | P7-02, device controller/service/repository/DTO tests |
| Device rename, revoke, credential rotation | `DeviceResource`, `DeviceCredentialResource`, `DeviceService`, `DeviceCredentialService` | No corresponding v1.1.0 parent operations are specified. Requires a new approved contract proposal, including status codes, DTOs, revocation/rotation semantics, and audit behavior. | P7-02, device lifecycle boundary |
| `updateDeviceHealth` | No matching controller/service implementation found; entities contain no completed health domain | Route and `DeviceHealth` schema exist with `x-status: planned`. Implement v1.1.0 in a device-only boundary; persist/validate deterministic `ACTIVE`, `FAILED`, `UNSUPPORTED`, and sync states. | P7-02/P7-03 decision required; device health files only |
| Consent and permission records | `ConsentResource`, `ConsentService`, `ConsentRecord` exist | No v1.1.0 operations cover consent. Existing APIs cannot be treated as the public product contract. Requires new contract proposal or explicit scope exclusion. | P7-02, consent boundary |

### Backend safety and policy matrix

| PRD responsibility / v1.1.0 operation | Current implementation | Gap and disposition | Owner / boundary |
|---|---|---|---|
| `ingestActivity` | No activity event entity, repository, service, or controller found | v1.1.0 route/schema are planned. Implement against the approved `BatchEnvelope`, with device ownership, event bounds, stable event IDs, idempotency/deduplication, and bounded batches. | P7-03, new backend safety package only |
| `ingestDomainObservations` | No domain observation implementation found | v1.1.0 route/schema are planned. Implement against the approved envelope; risk classification must remain `UNKNOWN` when evidence is unavailable. | P7-03, safety/domain files |
| `downloadPolicies` | No policy entity, service, repository, or controller found | v1.1.0 route/schema are planned. Implement device-scoped download and versioned policy payloads using existing schema only; flag missing policy response details for contract clarification before coding if needed. | P7-03, policy backend boundary |
| `acknowledgePolicy` | No acknowledgement implementation found | v1.1.0 route/schema are planned. Implement device-child-policy ownership, monotonic exact-version acknowledgement, and `ACTIVE` only after confirmation. | P7-03, policy acknowledgement boundary |
| `getUsageReport` | No report implementation found | v1.1.0 route exists but response schema is untyped (`ApiResponse.data` is unconstrained). Implementing a stable report payload requires a new approved schema/contract proposal before coding. | P7-03, reports boundary |
| `listAlerts` / `reviewAlert` | No alert entity, service, repository, or controller found | v1.1.0 routes exist but list/review response and query semantics are underspecified; dismiss is absent. Requires a new approved contract proposal covering pagination, states, deduplication, and dismissal. | P7-03, alerts boundary |
| `listPolicies` / `createPolicy` | No policy lifecycle implementation found | v1.1.0 routes and `PolicyRequest` exist. Implement create/list only against current schema after controller tests define response DTOs. Update/pause/remove/delivery status are absent and require a new contract proposal. | P7-03, policy lifecycle boundary |
| Freshness/completeness/source-device coverage | No report domain exists | Required by PRD 7 but not represented in OpenAPI v1.1.0. Requires new report schemas/enums and explicit `generatedAt`, `dataThrough`, `freshness`, `completeness`, and coverage semantics before implementation. | P7-03, reports contract proposal |

### Flutter mobile matrix

| PRD responsibility | Current implementation | Gap and disposition | Owner / boundary |
|---|---|---|---|
| Parent auth, family, child, device, report, alert, and policy journeys | `mobile/lib/main.dart` is a Flutter starter app; no ViewModels, repositories, API client, or feature folders found | Entire parent MVVM flow is missing. Build only after P7-01 contract decisions; use v1.1.0 operations where marked implementable and block unsupported screens behind explicit states. | P7-04, `mobile/lib/features/parent/**` |
| Child pairing, consent, status, recovery, secure storage | No feature implementation found; only app shell and Android activity exist | Entire child flow is missing. Pairing can use v1.1.0; consent/recovery routes need contract decision. Secure storage and session state must use an adapter, never widget calls. | P7-05, `mobile/lib/features/child/**`, storage adapter boundary |
| MVVM constraints | No current MVVM structure found | Create `View -> ViewModel -> Repository -> ApiClient/SecureStorage/PlatformChannel`; immutable state must distinguish loading, success, empty, offline, stale, unauthorized, failed, and unsupported. | P7-04/P7-05; no shared widget ownership overlap |
| API DTOs and numeric IDs | No API model/client files found | Add centralized DTO serialization using `int64`/Dart numeric IDs and approved envelope/problem handling. Do not invent report/policy response fields. | P7-04/P7-05, repository/client boundary |

### Android matrix

| PRD responsibility | Current implementation | Gap and disposition | Owner / boundary |
|---|---|---|---|
| Monitoring engine, activity queue, policy enforcement, credential store | Only `MainActivity.kt` is present; no monitoring, queue, policy, or credential classes found | All Android runtime responsibilities are missing. Implement only supported capabilities and expose deterministic `UNSUPPORTED` rather than fake success. | P7-06, `mobile/android/app/src/main/kotlin/com/tamdan/mobile/**` |
| Secure device credential persistence | No Kotlin implementation found in current source listing | Implement Keystore-backed adapter and Flutter channel registration, with corrupt/missing data -> signed-out recovery. Contract uses one-time pairing credential; never log it. | P7-06, Android storage/channel boundary |
| Reboot, permission loss, offline sync, VPN conflict | No runtime implementation found | Requires Android support matrix and emulator tests. VPN/domain enforcement remains unsupported unless runtime evidence proves otherwise; no API change needed for status reporting, but policy capability semantics may need contract clarification. | P7-06, Android runtime/tests |

### Cross-cutting rules and state gaps

| Area | Required rule | Current status | Follow-up |
|---|---|---|---|
| Ownership | Server derives family/child/device from authenticated context; all repository queries are ownership-scoped | Existing identity/device services provide a foundation; planned safety/report/policy domains are absent | Every P7-02/P7-03 acceptance test must include same-family and cross-family cases |
| Validation/injection | Jakarta boundary validation, bounded sizes, enum rejection, parameterized queries, normalized identifiers, redacted logs, DTO-only responses | Existing backend instructions and PRD specify the rules; new domains do not yet exist | Add negative tests per operation; no SQL/JPQL/shell/log-format concatenation |
| Device state | `PAIRING_PENDING -> ACTIVE -> REVOKED`, failure/recovery semantics | Contract has health enums but no backend state implementation | P7-02/P7-03 define persistence and transition tests; do not add states without contract decision |
| Policy state | `DRAFT -> PENDING_DELIVERY -> ACTIVE`, pause/remove/failure | No implementation; v1.1.0 only partially describes policy operations | P7-03 must propose missing lifecycle/delivery schemas before implementing absent operations |
| Report state | `LOADING -> COMPLETE/PARTIAL/STALE/UNKNOWN/ERROR` | No implementation; v1.1.0 lacks report payload schema | Contract proposal required before report implementation |
| Testing | Native PostgreSQL for ownership/transactions; Flutter tests for every ViewModel; Android emulator/device evidence for runtime | Existing baseline tests cover prior PRDs, not P7 missing domains | P7-07 and P7-08 own tests only in their boundaries; tests must run against exact implementation revisions |

## Bounded follow-up task graph

| Task | Owner | Exclusive boundary | Contract status | Depends on | Handoff criterion |
|---|---|---|---|---|---|
| P7-02 identity/device lifecycle | backend-development | Existing identity, family, child, pairing, device controller/service/repository/DTO/test files | Implement v1.1.0 routes; submit proposal first for edit/deactivate/rename/revoke/rotation/consent | P7-01 | Contract decision recorded; ownership and lifecycle tests pass |
| P7-03 safety/report/policy lifecycle | backend-development | New safety/report/policy entities, repositories, services, controllers, DTOs, migrations, tests | Implement only typed operations already supported; propose report/alert/missing lifecycle schemas first | P7-01, P7-02 | Approved contract revision; native PostgreSQL flow passes |
| P7-04 parent MVVM journeys | mobile-development | `mobile/lib/features/parent/**`, parent repositories/client/models/tests | Use v1.1.0 only; no invented report/policy shapes | P7-01, P7-02 contract decisions | MVVM tests and all required UI states pass |
| P7-05 child MVVM journeys | mobile-development | `mobile/lib/features/child/**`, child storage/client/models/tests | Pairing uses v1.1.0; consent/recovery require decision | P7-01, P7-02 | Secure storage/recovery and pairing tests pass |
| P7-06 Android supported runtime | mobile-development/android | `mobile/android/app/src/main/kotlin/com/tamdan/mobile/**` and Android tests | Status/pairing uses v1.1.0; no VPN enforcement claim without support evidence | P7-01, P7-05 | Emulator evidence and support matrix attached |
| P7-07 backend native tests | backend-testing | Backend test sources, fixtures, logs only | Tests exact approved contract/implementation | P7-02, P7-03 | Native PostgreSQL and contract tests pass |
| P7-08 mobile/device tests | mobile-testing | Flutter/Android test sources and evidence only | Tests exact approved contract/implementation | P7-04, P7-05, P7-06 | Flutter, emulator/device, and lineage evidence pass |
| P7-09 integration review | integration-review | Review record and findings only | Requires all approved revisions and evidence | P7-07, P7-08 | `APPROVED`; coordinator may then request merge approval |

## Required handoff and risks

- API implementation is blocked where the matrix says **contract proposal required**. The planning result does not approve any OpenAPI edit.
- Backend and mobile branches/worktrees must remain isolated. P7-04/P7-05 must not edit Android Kotlin; P7-06 must not edit Flutter feature code; P7-07/P7-08 must not modify application code.
- The current mobile shell and absent safety/policy domain mean “feature complete” cannot be inferred from existing passing baseline tests.
- The untyped `ApiResponse.data` makes report, alert, policy delivery, and acknowledgement payload drift likely; resolve this in an approved contract proposal before implementation.
- Unsupported Android VPN/domain enforcement must remain visible as `UNSUPPORTED`; a successful API acknowledgement alone cannot activate enforcement.

## Verification record

Commands run from `/Users/nak/.codex/worktrees/28cf/tam-dan`:

```text
rg --files backend mobile agent-workflow/contracts
```

Exit code: `0`. Log: terminal output from planning run; repository file inventory captured.

```text
rg -n "path:|operationId:|class |Future<|fun |TODO|UNSUPPORTED" backend mobile agent-workflow/contracts/openapi-v1.yaml
```

Exit code: `0`. Log: terminal output from planning run; route, operation, class, TODO, and unsupported markers captured.

Additional read-only inspection commands:

```text
rg -n '^#{1,4} ' tasks/07-remaining-product-completion/README.md
for f in backend/src/main/java/com/tamdan/controller/*.java; do rg -n '@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@DeleteMapping|public .*\\(' "$f"; done
git status --short --branch
```

All exited `0`. No application tests were run because this task changes documentation only; no application code was changed.

## Handoff status

`PLANNING-COMPLETE / IMPLEMENTATION-BLOCKED-ON-CONTRACT-DECISIONS`

The coordinator may dispatch P7-02/P7-04/P7-05/P7-06 only for v1.1.0-supported operations after recording the exact contract decisions. P7-03 must first obtain approval for typed report/alert/policy lifecycle additions where identified above. Integration Review remains mandatory before merge.
