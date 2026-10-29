# Task: PRD 7 backend safety, reports, alerts, and policies

- ID: `P7-03`
- Role: `backend-development`
- State: `in-progress`
- Branch/worktree: isolated backend worktree, branch prefix `codex/p7-backend-safety-policy`
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29
- OpenAPI approval: `v1.2.0`, approved 2026-10-29
- Dependencies: P7-01 and approved P7-00 contract

## Scope

Implement activity/domain ingestion, idempotency, usage reports, freshness/completeness/source-device coverage, risk `UNKNOWN`, alert review/dismissal, policy lifecycle, delivery, and device acknowledgement using authenticated parent/device context and approved v1.2.0 schemas.

## Acceptance criteria

- [ ] All writes are family/child/device scoped from authenticated context.
- [ ] Event IDs are stable and duplicate-safe; batches and free text are bounded.
- [ ] Reports expose typed freshness, completeness, data-through, generated-at, and source-device coverage.
- [ ] Insufficient evidence is `UNKNOWN`, never `SAFE`.
- [ ] Alert review/dismissal is idempotent and family-scoped.
- [ ] Policy `ACTIVE` requires acknowledgement of the exact version by the correct device.
- [ ] DTOs are returned; entities are not exposed.
- [ ] Native PostgreSQL tests cover the event-to-report/alert/policy flow.

## Verification

```text
cd backend
./mvnw spotless:check
./mvnw test
./mvnw verify
./mvnw liquibase:validate
```

## Handoff

- Changed files: new safety/report/policy packages, migrations, DTOs, controllers, services, repositories, and focused tests only
- Risks/compatibility: do not edit identity/device files owned by P7-02
- Review status: pending
