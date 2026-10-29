# Active NEEDS-FIX Task — Release and Integration Gate

## Status

- State: `needs-fix`
- Priority: `P0`
- Owner: coordinator
- Applies to: final implementation revisions and future PRD 7 work
- Merge/deployment: blocked

## Purpose

Track the remaining release-gate issues separately from product feature completion. This task must be closed before merge, even if individual feature tests pass.

## Current implementation baseline

- Backend: `ca17cde`
- Mobile runtime: `c4e69e8`
- Backend/testing branch alignment: both point to `ca17cde`
- Native PostgreSQL: verified
- Android emulator runtime: verified
- Code-level review: approved

## Remaining fixes

### F1 — Vulnerability scan evidence

Problem: OWASP Dependency-Check cannot update NVD data because the API key/data configuration is unavailable.

Required action:

- Configure a valid NVD data/API source or approved offline vulnerability database.
- Run Maven, Flutter, and Android dependency audits.
- Store the reports with the reviewed revisions.
- Record severity, affected component, remediation, and accepted-risk owner.

Done when:

- A reproducible dependency vulnerability result exists, or the release owner explicitly accepts the unavailable-scan risk with an expiry date.

### F2 — Secret and production configuration evidence

Problem: Code was changed to use environment-backed secrets, but the final dedicated scan and production configuration review are still required.

Required action:

- Run a secret scanner or approved fallback scan.
- Verify `secret-samples` is development-only.
- Verify production secrets are injected externally.
- Verify no password, JWT secret, token, pairing code, or device credential is logged or returned.

Done when:

- Scan output is attached and production configuration review is recorded.

### F3 — Exact revision evidence

Problem: Test evidence must always match the implementation revision under review.

Required action:

- Record implementation and test commit SHAs together.
- Verify the test commit is based on the implementation commit.
- Keep backend, mobile, Android, and testing branches/worktrees clean.
- Update the knowledge base after each final rerun.

Done when:

- Integration Review can reproduce every reported result from the exact revisions.

### F4 — Final Integration Review

Required action:

- Submit the final code revisions, native PostgreSQL logs, emulator logs, security reports, and risk register.
- Obtain `APPROVED`.

Done when:

- Integration Review returns `APPROVED`.
- Merge is separately approved.
- Deployment is separately approved.

## Verification commands

```bash
cd backend
./mvnw spotless:check
./mvnw test
./mvnw verify
./mvnw liquibase:validate

cd ../mobile
flutter analyze
flutter test
flutter test integration_test
```

Native PostgreSQL and Android emulator are required for the primary verification path. Docker is not required.

## Handoff rule

Any failed command returns to the responsible owner with the exact command, exit code, revision, environment, log path, and fix request. Do not mark this task complete because unit tests alone pass.
