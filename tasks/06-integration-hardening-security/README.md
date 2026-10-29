# PRD 6 — Integration Hardening and OWASP Security Remediation

## Status

- State: `requirements-approved`
- Priority: `P0 — merge blocker`
- Owner: coordinator
- Gate: approved requirements and versioned OpenAPI contract are required before implementation
- Merge rule: no merge or deployment until Integration Review is `approved`

## Problem

The current integration review rejected merge. The product cannot be considered release-ready until the seven blockers below are resolved and verified with automated, device, and integration evidence.

This PRD is a remediation plan. It does not add unrelated product features, but it does include the missing feature logic needed for ownership-scoped children, devices, activity, reports, alerts, and policies.

## Approved implementation focus

The next development wave is feature-first:

1. Implement the core parent/device/child logic and data ownership boundaries.
2. Implement correct validation, authorization, idempotency, and injection-safe persistence/query behavior.
3. Implement report, alert, and policy state transitions with truthful data-quality states.
4. Keep security fixes directly attached to the feature boundary they protect.
5. Defer broad security-hardening work such as scanner automation, dashboards, signing infrastructure, and full device-lab setup to the fix and verification track.

This approval does not approve insecure shortcuts: credentials must still use secure storage, unauthorized ownership access must fail, untrusted input must be validated, and unsupported enforcement must not be reported as active.

## Goals and non-goals

Goals:

1. Make mobile, Android, backend, and OpenAPI contracts consistent.
2. Remove unsafe production placeholders and unsupported success states.
3. Prevent direct persistence-entity exposure from API responses.
4. Define complete and freshness-aware usage-report semantics.
5. Produce reproducible test evidence and document unavailable environment checks.
6. Apply OWASP Top 10:2021 controls to every changed boundary.

Non-goals:

- Adding new monitoring features.
- Claiming VPN/domain enforcement works without a real Android verification run.
- Merging or deploying any branch from this remediation wave.

## Blocker requirements

### B1 — Authentication route alignment

The mobile client and backend must use one approved route and payload contract for register, login, refresh, logout, and device authentication. Remove duplicate or legacy route ambiguity, update OpenAPI, and add contract tests that fail on drift.

Acceptance criteria:

- Flutter calls only routes present in the approved OpenAPI document.
- Spring controllers implement those exact routes and response shapes.
- Register, login, refresh, logout, and unauthorized responses have automated tests.
- Access-token, refresh-token, expiry, rotation, and revocation behavior is documented.

### B2 — Secure-storage channel registration

Register the Flutter secure-storage channel in Android and connect it to Android Keystore-backed storage. Production composition must not use a placeholder device ID or throw an unconditional security exception.

Acceptance criteria:

- Android registers and handles `com.tamdan/secure_storage_v1` before Flutter use.
- Credentials and device identity are encrypted at rest and never logged.
- Missing, revoked, and corrupted credentials produce explicit recovery states.
- Android instrumentation or device evidence covers read, write, delete, restart, and logout.

### B3 — Android enforcement capability truthfulness

Define the supported Android API/device matrix for application and domain enforcement. Capability status must be evidence-based; unsupported paths must remain visible and must not report `ACTIVE`.

Acceptance criteria:

- Physical-device checks cover pairing, permissions, reboot, VPN conflict, policy delivery, enforcement, removal, and resynchronization.
- `ACTIVE` is emitted only after device confirmation.
- Unsupported and failed states contain a user-actionable reason.
- VPN/domain observation code has a documented threat model and permission boundary.

### B4 — ID type contract correction

Use one ID representation across OpenAPI, backend entities/DTOs, persistence, tests, and mobile models. The current backend uses `Long`; the approved contract must reflect the chosen representation consistently.

Acceptance criteria:

- No child or policy endpoint has UUID schema while its implementation accepts `Long`.
- OpenAPI is regenerated or validated in CI against controller DTOs.
- Mobile serialization/deserialization and repository tests cover IDs, invalid IDs, and missing resources.

### B5 — DTO boundary enforcement

All external responses must use explicit response DTOs. JPA entities, lazy relationships, internal audit fields, and persistence implementation details must not cross the API boundary.

Acceptance criteria:

- Controllers and services return approved DTOs or projections.
- DTOs use allowlisted fields and stable null/empty semantics.
- Serialization tests prove that entity-only fields and relationships are absent.
- Authorization is checked before loading or returning family/device data.

### B6 — Usage-report freshness and completeness

Reports must distinguish complete, partial, stale, delayed, and unavailable data. `complete=true` must never be hardcoded.

Acceptance criteria:

- Every report includes `generatedAt`, `dataThrough`, `freshness`, `completeness`, and source/device coverage.
- Freshness thresholds and timezone/day-boundary rules are documented.
- Offline events, duplicate events, late events, and missing-device data have deterministic behavior.
- Tests cover empty, partial, stale, delayed, duplicate, and complete reports.

### B7 — Test-environment readiness and evidence

Restore or explicitly document the required verification environments: Docker/PostgreSQL, JVM test execution, Flutter SDK/cache, Android SDK/device, Gradle wrapper, and full integration execution.

Acceptance criteria:

- Backend unit/integration tests run with real PostgreSQL where required.
- Flutter analysis and tests run without denied SDK-cache writes.
- Android build/tests run with the repository's declared Gradle wrapper or a recorded environment exception.
- A final evidence report records command, revision, result, and logs for every required check.

## OWASP Top 10 security requirements

This project uses OWASP Top 10:2021 as the web/API security baseline and applies the same control intent to mobile and Android boundaries.

| OWASP control | Tam Dan application requirement |
|---|---|
| A01 Broken Access Control | Enforce parent/family/device ownership on every read, write, pairing, policy, report, and alert endpoint. Never trust child or device IDs from the client. |
| A02 Cryptographic Failures | Use platform secure storage/Keystore, TLS, safe token handling, redacted logs, and no plaintext credentials or sensitive activity data. |
| A03 Injection | Validate route/path/query/body inputs, use parameterized persistence queries, constrain domain/content inputs, and test malformed payloads. |
| A04 Insecure Design | Treat unsupported, stale, partial, and failed states as first-class states; require threat-model review for pairing, VPN, reports, and enforcement. |
| A05 Security Misconfiguration | Remove placeholders, fail-closed security defaults, configure CORS/headers/error handling safely, and separate test/development credentials. |
| A06 Vulnerable and Outdated Components | Run dependency audits for Maven, Flutter, Gradle, and Android; record severity, owner, fix or accepted exception. |
| A07 Identification and Authentication Failures | Align auth routes, validate token expiry/rotation/revocation, rate-limit sensitive flows, and test unauthorized/expired/replayed credentials. |
| A08 Software and Data Integrity Failures | Version and validate OpenAPI/contracts, verify policy versions and device acknowledgements, protect migrations, and reject untrusted update/configuration data. |
| A09 Security Logging and Monitoring Failures | Record security-relevant events without secrets, correlate device/policy/auth failures, expose freshness and sync health, and alert on repeated failures. |
| A10 SSRF | Allowlist any backend URL fetch/classification source, block private-network targets and redirects, enforce timeouts/size limits, and test malicious URLs. |

Security acceptance criteria:

- Every changed endpoint has an authorization test and an input-validation test.
- Every credential path has a secure-storage and redacted-log review.
- Dependency and secret scans are run and attached to the review.
- Any accepted risk names an owner, expiry date, scope, and compensating control.

## Workstreams and isolation

Each workstream starts from the coordinator branch and uses a dedicated branch/worktree:

- `codex/hardening-backend` — B1, B4, B5, B6 and backend OWASP controls.
- `codex/hardening-mobile` — B1, B2, B4 and Flutter MVVM client behavior.
- `codex/hardening-android` — B2, B3 and Android capability/enforcement evidence.
- `codex/hardening-security` — independent OWASP threat model, dependency/secret review, and cross-boundary findings.
- `codex/hardening-backend-testing` — backend test harness, PostgreSQL/JVM evidence, and failure logs.
- `codex/hardening-mobile-testing` — Flutter/Android test matrix and failure logs.
- `codex/hardening-integration-review` — final contract, security, test, and merge review only.

Agents must not modify another workstream's files or merge another branch. If a test fails, the tester returns the task to the responsible developer with the exact command, revision, environment, and log excerpt.

## Required sequence

1. Coordinator records requirements approval and approves the corrected, versioned OpenAPI contract.
2. Backend, mobile, Android, and security agents work in parallel on isolated branches.
3. Testing agents run actual automated tests against the responsible branches.
4. Failed tests return to the responsible developer with logs and a fix request.
5. Integration Review checks all seven blockers, OWASP controls, and evidence.
6. Only an explicit approved review permits merge; deployment requires a separate approval.

## Verification matrix

| Area | Required evidence |
|---|---|
| Backend | format/check, compile, unit tests, integration tests with PostgreSQL, OpenAPI validation, dependency/secret scan |
| Flutter | format, analyze, unit/widget/repository tests, contract tests, secure-storage behavior |
| Android | build, unit/instrumentation tests, physical-device matrix, capability truthfulness, VPN conflict behavior |
| Integration | end-to-end auth/pairing/policy/report flow, negative authorization cases, stale/partial report cases |
| Security | OWASP checklist, threat model, dependency scan, secret scan, redacted logs, unresolved-risk register |

## Definition of done

- All B1–B7 acceptance criteria pass or have an explicitly approved exception.
- OpenAPI and Flutter/Kotlin contracts are versioned and approved.
- Automated test logs are attached; unavailable environments are recorded as blockers, not silently skipped.
- Integration Review is `approved`.
- No merge or deployment occurs before the approval gates above.
