# PRD 9 — Production Readiness

## Status

- State: `PROPOSED`
- Priority: `P0 — release gate`
- Owner: coordinator/release
- Depends on: PRD 8 security evidence completion, approved OpenAPI contract, Integration Review approval
- Deployment: explicitly out of scope until this PRD is approved and all gates pass

## Goal

Prepare Tam Dan for a controlled production release. This PRD covers operational infrastructure, configuration, security evidence, database reliability, observability, recovery, release automation, and support readiness. It does not expand product behavior.

The current native PostgreSQL and Android-emulator application is a development-validation build. Passing this PRD requires production-like evidence from the exact release revisions.

## Non-goals

- No new product journeys or API behavior without a separately approved contract.
- No password recovery implementation until OpenAPI v1.2.1 is approved.
- No claim of compliance certification, legal approval, or production safety without the responsible owner’s written approval.
- No deployment during implementation or testing tasks.

## Release gates

1. Product requirements and the exact OpenAPI version are approved.
2. All P0/P1 Integration Review findings are closed.
3. Secrets are injected externally; no credentials or production fallbacks exist in source or artifacts.
4. Database migrations, rollback boundaries, backup, restore, and retention are tested.
5. Authentication, authorization, ownership, injection, privacy, dependency, and logging evidence is complete.
6. Monitoring, alerting, incident response, and operational runbooks are complete.
7. Backend, Flutter, Android, security, and release checks pass on the exact release revision.
8. A release owner explicitly approves deployment after this PRD is complete.

## Workstreams

### P9-01 — Production architecture and environment

Owner: platform/infrastructure

Define the production topology, network boundaries, TLS termination, service-to-database connectivity, resource sizing, availability targets, and environment separation for development, staging, and production.

Acceptance criteria:

- Architecture diagram and environment matrix are reviewed.
- Public, private, and administrative network paths are documented.
- Health, readiness, timeout, connection-pool, and shutdown behavior are verified.
- No production deployment is performed by this task.

### P9-02 — Configuration and secret management

Owner: backend/platform

Replace placeholders and implicit development defaults with externally injected configuration for database credentials, JWT signing material, mail, TLS, public URLs, CORS, and encryption settings.

Acceptance criteria:

- Startup fails clearly when required production configuration is absent.
- Production cannot silently activate the `dev` profile.
- Secrets are absent from Git history, logs, crash reports, APKs, and generated artifacts.
- Secret rotation procedure is documented and tested.

### P9-03 — Database reliability and migration operations

Owner: backend/database

Define migration ownership, forward-only and rollback boundaries, backup policy, restore testing, retention, indexing review, connection limits, and data integrity checks for native PostgreSQL.

Acceptance criteria:

- A clean production-like database applies every migration successfully.
- Restore creates a usable database and preserves ownership/security invariants.
- Backup frequency, retention, encryption, and deletion policy are documented.
- Migration failure and recovery runbooks are tested.

### P9-04 — Application security and privacy closure

Owner: security/backend/mobile

Complete the deferred security evidence from PRD 8 and backlog item 12: dependency scanning, secret scanning, threat-model review, authentication/authorization review, input validation, sensitive logging, device pairing, secure storage, and privacy boundaries.

Acceptance criteria:

- Every finding has severity, owner, remediation, evidence, and expiry/acceptance decision.
- No password, token, pairing code, device credential, authorization header, or sensitive activity payload is logged.
- Cross-family and parent/child/device ownership tests pass.
- Unsupported Android capabilities remain explicitly `UNSUPPORTED`.

### P9-05 — Observability and operations

Owner: SRE/operations

Define structured logs, metrics, traces, dashboards, health checks, alert thresholds, audit retention, and escalation ownership.

Acceptance criteria:

- Authentication failures, authorization failures, pairing failures, migration failures, latency, error rate, database health, and queue/sync failures are observable.
- Alerts have actionable thresholds, owners, severity, and runbooks.
- Correlation IDs do not expose secrets or personal data.
- A representative incident can be detected and traced without sensitive-data leakage.

### P9-06 — Release engineering and supply chain

Owner: release/DevOps

Create reproducible backend and Android release artifacts, dependency locks, SBOM generation, artifact signing, provenance, versioning, changelog, and rollback procedures.

Acceptance criteria:

- The same revision produces the recorded backend and APK artifacts.
- Artifact signatures and verification steps are documented.
- CI runs formatting, unit, contract, security, migration, build, and available runtime checks.
- Failed checks prevent release promotion.

### P9-07 — Production runtime validation

Owner: backend-testing/mobile-testing

Run the complete exact-revision matrix against staging-like infrastructure:

```bash
cd backend
./mvnw -q spotless:check
./mvnw -q test
./mvnw -q liquibase:validate
./mvnw -q verify

cd ../mobile
flutter pub get
dart format --output=none --set-exit-if-changed lib test integration_test
flutter analyze
flutter test
flutter test integration_test -d emulator-5554
flutter build apk --debug

cd android
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

Acceptance criteria:

- Every command records revision, environment, exit code, and log path.
- Unavailable infrastructure is recorded as blocked, never as passed.
- Native PostgreSQL startup, migration, ownership, security, and recovery evidence is attached.
- Android emulator evidence covers identity, secure storage, pairing, restart, permission recovery, synchronization, and unsupported capabilities.

### P9-08 — Support and incident readiness

Owner: operations/product

Document user support, incident severity, escalation, data-access requests, account/device revocation, outage communication, rollback, and post-incident review procedures.

Acceptance criteria:

- On-call ownership and escalation contacts are assigned.
- Runbooks cover authentication outage, database failure, compromised credential, device revocation, bad migration, and mobile rollback.
- Recovery time and recovery point objectives are stated and tested where applicable.
- Release notes identify known limitations and deferred password recovery.

## Dependency graph

1. Approve this PRD and freeze the production contract.
2. Complete P9-01 and P9-02 before staging deployment preparation.
3. Complete P9-03 before production migration approval.
4. Complete P9-04 and P9-06 before release-candidate creation.
5. Complete P9-05 and P9-08 before operational approval.
6. Run P9-07 on the exact release candidate.
7. Request Integration Review and release-owner approval.
8. Deployment is a separate explicit action after all gates pass.

## Required evidence record

For every task, record:

- owner, branch/worktree, exact revision, and changed files;
- requirements and OpenAPI references;
- command, environment, exit code, and log path;
- security, migration, compatibility, and rollback risks;
- reviewer decision and unresolved backlog;
- explicit confirmation that no deployment occurred during the task.

## Definition of done

- P9-01 through P9-08 are approved or have explicit release-owner risk acceptance.
- PRD 8 and backlog security evidence are closed.
- Production-like database restore and migration evidence passes.
- Release artifacts are reproducible, signed, and traceable to an exact revision.
- Monitoring, alerting, runbooks, and support ownership are operationally ready.
- Integration Review returns `APPROVED`.
- A separate release owner approves deployment.
