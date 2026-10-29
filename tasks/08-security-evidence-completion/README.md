# PRD 8 — Security Evidence Completion and NEEDS-FIX Closure

## Status

- State: `backlog`
- Priority: `P2 — hardening`
- Owner: coordinator/security
- Depends on: backend `ca17cde`, mobile runtime `c4e69e8`, approved OpenAPI v1.1.0
- Feature development: not blocked by this PRD
- Merge/deployment: still requires normal Integration Review approval

## Problem

Feature logic, ownership boundaries, native PostgreSQL verification, Flutter verification, and Android emulator verification have passed. The release gate remains `NEEDS-FIX` because vulnerability evidence is incomplete and the final security disposition is not yet approved.

## Goal

Produce reproducible security evidence for the exact implementation revisions, or obtain explicit release-owner risk acceptance for any unavailable evidence.

## Scope

### S1 — Dependency vulnerability evidence

Run vulnerability analysis for:

- Maven dependencies from backend `ca17cde`.
- Flutter packages from mobile runtime `c4e69e8`.
- Android/Gradle dependencies from the same runtime revision.

Accepted closure options:

1. A vulnerability scanner produces reports with tool version, database date, revision, findings, and remediation status.
2. If the scanner cannot obtain vulnerability data, the release owner records an explicit risk acceptance containing owner, reason, affected scope, compensating controls, expiry date, and follow-up scan date.

The dependency tree alone is inventory evidence, not vulnerability evidence.

### S2 — Secret and production configuration evidence

Verify:

- No literal database passwords, JWT secrets, private keys, access keys, tokens, pairing codes, or device credentials are committed.
- `secret-samples` is development-only.
- Production values are injected externally.
- TLS, database, mail, JWT, and API configuration do not contain usable secrets.
- Sensitive request/response/log fields are redacted.

Acceptance criteria:

- Dedicated scanner output or an approved fallback scan is attached.
- Production profile review names each external secret and its injection source.
- Any remaining sample value is clearly non-secret and cannot activate in production.

### S3 — Exact revision and evidence integrity

All evidence must identify:

- Implementation commit.
- Test commit.
- Branch/worktree.
- Exact command.
- Exit code.
- Environment.
- Log/report path.

The testing revision must be based on the implementation revision. Stale branch pointers and uncommitted evidence changes are blockers.

### S4 — Final review disposition

Integration Review must inspect the updated risk register and return:

- `APPROVED`, or
- `NEEDS-FIX` with one or more exact owners and actions.

## Workstreams

- `codex/hardening-security`: vulnerability, secret, configuration, and OWASP evidence.
- `codex/backend-testing`: Maven dependency and native PostgreSQL evidence.
- `codex/mobile-testing`: Flutter and Android dependency/runtime evidence.
- `codex/coordinator`: risk register, exact-commit reconciliation, and final review request.

No application feature code should change unless a security finding proves a concrete defect.

## Required commands

Native runtime only; do not use Docker.

```bash
# Backend inventory and tests
cd backend
./mvnw dependency:tree -DoutputFile=/tmp/tamdan-task8-backend-dependencies.txt
./mvnw test
./mvnw verify
./mvnw liquibase:validate

# Mobile inventory and tests
cd ../mobile
flutter pub deps --style=compact
flutter analyze
flutter test
flutter test integration_test

# Repository fallback scan
git grep -n -I -i -E 'BEGIN (RSA|EC|OPENSSH) PRIVATE KEY|AKIA[0-9A-Z]{16}' -- backend mobile
```

If OWASP Dependency-Check is used, record its full output even when it fails. A failed NVD update is not a vulnerability result.

## Definition of done

- S1 has a valid vulnerability report or explicit release-owner risk acceptance with expiry.
- S2 secret/config evidence is attached.
- S3 exact implementation/test revisions are reconciled.
- Native PostgreSQL and Android evidence remain reproducible.
- Risk register is updated and committed.
- Integration Review returns `APPROVED`.
- Merge and deployment receive separate approval.
