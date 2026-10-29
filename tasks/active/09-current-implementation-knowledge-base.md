# Tam Dan — Current Implementation Knowledge Base

## Current state

- Product: Tam Dan parental control and child online-safety platform.
- Coordinator branch: `codex/coordinator`.
- Current gate: `NEEDS-FIX / environment-blocked`.
- Code-level Integration Review: approved.
- Merge/deployment: not approved.
- API contract: OpenAPI `v1.1.0`, `/api/v1`, numeric `Long` IDs, `X-Device-Credential`.
- Primary scope: feature logic, ownership boundaries, validation, and injection prevention.
- Native runtime verification: backend and Android emulator checks now available; final security evidence remains incomplete.

## What was implemented

### Backend — `codex/hardening-backend`

Final implementation: `9795ee3`

- Migrated API behavior toward `/api/v1`.
- Aligned authentication routes and session responses.
- Added parent/device/child ownership checks.
- Added explicit DTO responses instead of persistence entities.
- Added pairing, activity, domain-observation, policy, alert, and usage-report logic.
- Added input validation and injection-safe policy/event handling.
- Added idempotent activity ingestion.
- Added report freshness/completeness states.
- Added nested `sourceDeviceCoverage`.

### Flutter — `codex/hardening-mobile`

Final implementation: `691e581`

- Added MVVM-oriented auth/session flows.
- Added API route and payload alignment.
- Added parent and device credential handling.
- Added secure-storage abstraction.
- Added pairing credential and device-context persistence.
- Added positive `Long` ID validation.
- Added policy enum serialization.
- Added loading, offline, stale, error, and permission states.

### Android — `codex/hardening-android`

Final implementation: `9f2c085`

- Registered secure-storage channel compatibility methods.
- Added Android Keystore AES-GCM storage.
- Added missing/corrupt/security error handling.
- Added monitoring channel methods.
- Reports unsupported capability states truthfully.
- Avoids placeholder device identity.

## Verification completed

### Backend

- Focused ownership/DTO/security/report/pairing tests: **19 passed**.
- Full unit suite: **75 passed**.
- Formatting: passed.

Backend test evidence: `cb13b55`, based on backend implementation `9795ee3`.

### Flutter

- `flutter analyze`: passed.
- Flutter tests: **23 passed**.
- Mobile test evidence: `200ea96`.

### Android

- Kotlin compilation: passed.
- Debug assemble/build: passed.
- Unit test task: passed, but no Android unit sources existed.

## Native runtime verification update

- Backend canonical native-runtime commit: `080baf6`.
- Native PostgreSQL: reachable with role/database `tamdan`.
- Backend `./mvnw verify`: passed; 75 unit tests and 110 integration tests, zero failures/errors.
- Backend log: `/tmp/tamdan-verify-final-rerun.log`.
- Android emulator: `emulator-5554` reported `device`.
- Flutter analyze, Flutter tests, APK build, Flutter integration tests, and Android compile/assemble passed.
- Mobile runtime commits: `3c60255` and `c4e69e8`.
- OWASP register: [native-runtime-owasp-risk-register.md](native-runtime-owasp-risk-register.md).

Remaining security work: dependency audit data/API configuration, committed sample-secret cleanup, structured sensitive-log redaction evidence, and final exact-commit review.

## Known blockers

1. The primary verification environment must use native PostgreSQL; Docker/Testcontainers is optional CI infrastructure, not the developer path.
2. PostgreSQL role/database `tamdan` must be created locally before Liquibase and database-backed tests can run.
3. An Android emulator or physical device must be connected for secure-storage restart/logout, pairing, VPN, enforcement, reboot, and resynchronization.
4. Dependency scan, secret scan, threat-model evidence, and accepted-risk register are not attached.
5. The coordinator worktree contains untracked mobile agent artifacts; they are not merged or approved.

## Start next

### 1. Prepare the real local runtime

```bash
pg_isready -h localhost -p 5432 -U tamdan
adb devices -l
```

Install and start PostgreSQL natively using the operating system package manager or official installer. Create database `tamdan` and login role `tamdan` with the credentials expected by the selected Spring profile. Connect an Android emulator or physical device. Do not use Docker for the primary local verification.

### 2. Run final backend verification

```bash
cd backend
./mvnw test
./mvnw verify
./mvnw liquibase:validate
```

If integration tests are hard-coded to Testcontainers, update the test profile to use the approved local PostgreSQL connection without weakening the tests. Treat Testcontainers as an optional CI path.

### 3. Run final mobile/device verification

```bash
cd mobile
flutter analyze
flutter test
flutter test integration_test
flutter build apk --debug
```

Verify secure storage read/write/delete/restart/logout, pairing, credential revocation, permissions, policy delivery, unsupported enforcement, and resynchronization.

### 4. Run security evidence checks

- Dependency audit for Maven, Flutter, and Android/Gradle.
- Secret scan with no active credentials detected.
- OWASP ownership, authentication, injection, secure-storage, logging, and SSRF checklist.
- Record unresolved risks with owner, mitigation, and expiry.

### 5. Request final Integration Review

Only after native PostgreSQL, backend runtime, Flutter, and Android runtime checks have logs. Do not merge or deploy before `APPROVED`.

## Important rule

Do not restart feature implementation from the old audit. Continue from the final commits listed above, preserve the approved OpenAPI contract, and keep backend, Flutter, Android, testing, and review work isolated.
