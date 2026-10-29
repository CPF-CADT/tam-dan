# Task: PRD 7 backend identity and device lifecycle

- ID: `P7-02`
- Role: `backend-development`
- State: `in-progress`
- Branch/worktree: isolated backend worktree, branch prefix `codex/p7-backend-identity-device`
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29
- OpenAPI approval: `v1.2.0`, approved 2026-10-29
- Figma/design approval: not applicable
- Dependencies: `P7-01` for gap confirmation; may use existing v1.1.0 routes immediately

## Scope

Implement only missing identity/device lifecycle behavior: account recovery, family/child lifecycle, pairing, device rename/revoke/rotation, consent, and audit behavior. Preserve DTO-only responses and ownership checks. Do not modify safety/report/policy packages or mobile code.

## Acceptance criteria

- [ ] Every parent operation derives family ownership from the authenticated principal.
- [ ] Device operations require the authenticated device context where applicable.
- [ ] Pairing codes are single-use, expiring, throttled, hashed at rest, and never logged raw.
- [ ] Revoked/inactive devices cannot authenticate, ingest, or acknowledge policies.
- [ ] Responses use DTOs and numeric ID types matching OpenAPI v1.1.0.
- [ ] Existing tests remain intact and new behavior has automated tests.

## Verification

```text
cd backend
./mvnw spotless:check
./mvnw -Dtest='*Identity*,*Family*,*Pairing*,*Device*,*Consent*' test
./mvnw test
```

## Handoff

- Changed files: backend identity/device controllers, services, repositories, DTOs, migrations, and tests only
- Risks/compatibility: report/policy behavior is out of scope; API changes require contract review
- Review status: pending
