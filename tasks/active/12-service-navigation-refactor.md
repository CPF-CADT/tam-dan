# Task: Group backend services by feature

- ID: `12-service-navigation-refactor`
- Role: `backend-development`
- State: `implemented-awaiting-integration-review`
- Branch/worktree: `/Users/nak/.codex/worktrees/16a4/tam-dan` (isolated worktree; detached HEAD)
- Requirements approval: direct user request for conservative service-package navigation refactor
- OpenAPI approval: `not applicable` (package/import organization only)
- Figma/design approval: `not applicable`
- Dependencies: none

## Scope

Group existing Spring services under `com.tamdan.service.<feature>` where the feature boundary is clear. Keep cross-cutting rate limiting, metrics, and demo initialization in `com.tamdan.service`. Preserve routes, contracts, business behavior, persistence, and Spring component scanning.

## Acceptance criteria

- [x] Feature services are grouped under auth, family, device, safety, reporting, and consent packages.
- [x] Imports, package declarations, and affected tests compile without API or persistence changes.
- [x] Spring component scanning and architecture tests remain passing.
- [x] Spotless, compile, and test verification are run with results recorded.
- [x] Changes are committed in this isolated worktree and not merged or deployed.

## Verification

Required commands:

```text
./mvnw spotless:check
./mvnw -q -DskipTests compile
./mvnw test
./mvnw verify
./mvnw liquibase:validate
```

Actual results:

- `./mvnw spotless:check` — exit `0`, clean.
- `./mvnw -q -DskipTests compile` — exit `0`.
- `./mvnw test` — exit `0`, 71 tests, 0 failures/errors.
- `./mvnw liquibase:validate` — exit `0`, no validation errors.
- `./mvnw verify` — exit `1`; unit tests and the moved security-context tests pass, but integration tests are blocked by unavailable Docker/Testcontainers and a pre-existing local PostgreSQL schema mismatch (`failed_login_attempts` column missing).

## Handoff

- Changed files: feature service packages, affected imports/tests, package-info files
- Risks/compatibility: package names changed internally; public routes, DTOs, persistence, and behavior are unchanged. Integration review should confirm downstream import assumptions.
- Review status: `pending`
