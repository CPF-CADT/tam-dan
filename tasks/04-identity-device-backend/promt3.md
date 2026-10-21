# PRD 4 Prompt 3 of 3: MVP hardening and release verification

This is the final prompt in the three-prompt MVP plan:

1. `promt.md` — identity, parent account, family, and child foundation.
2. `promt2.md` — pairing, device registration, authentication, consent, and audit.
3. `promt3.md` — security hardening, verification, and MVP release readiness.

Do not expand the MVP into PRD 5. Do not implement guardian management, child deletion requests, advanced device-health models, analytics, alerts, or policy/safety features unless the existing PRD 4 README explicitly requires them.

## Required preparation

Read `tasks/04-identity-device-backend/README.md`, then inspect the current implementation from prompts 1 and 2: entities, repositories, services, controllers, DTOs, security filters/resolvers, exceptions, rate limiting, audit, Liquibase, and tests. Produce a short gap matrix before editing. Implement only confirmed MVP gaps.

## MVP hardening requirements

### Account and session security

- Ensure registration, activation, login, recovery, refresh, logout, and logout-all use stable error codes and correct HTTP statuses.
- Add only the minimum configuration-driven failed-login/lock handling required by the PRD. Failed login must not reveal whether an account exists, is inactive, or is locked.
- Make refresh rotation atomic. Reject expired, revoked, replaced, or reused refresh tokens. Populate `replacedBySessionId`; record a safe audit event for reuse and never expose or log raw tokens.

### Pairing and device security

- Use `PairingCode.failedAttempts` atomically with a configuration-driven maximum. Reject expired, used, or over-attempt codes without logging or returning raw codes.
- Keep pairing submission rate limiting separate from authentication; rate-limit keys must use normalized or one-way values only.
- Enforce the PRD-required device-identifier uniqueness in the database and service layer. Convert duplicate races to the existing conflict error.
- Confirm device credentials are hashed, rotated transactionally, revoked immediately, and rejected when expired, revoked, inactive, or linked to an inactive child/family.
- Confirm parent JWTs cannot authenticate device routes and device credentials cannot authenticate parent routes. Do not log authentication headers.

### Ownership and API safety

- Resolve parent and family identity only from authenticated context. Never trust client-supplied owner or family IDs.
- Enforce family-aware ownership checks on every protected child, pairing, device, consent, and audit operation.
- Controllers return DTOs only; never expose entities, hashes, credentials, tokens, pairing codes, capabilities, or unnecessary child data.
- Preserve public health/info routes only where already defined; keep `/api/**` protected.

### Error, audit, and configuration hardening

- Return the existing RFC 7807-compatible error structure with stable machine-readable codes, path, request ID when available, and validation field errors.
- Confirm HTTP 400, 401, 403, 404, 409, and 429 mappings for invalid input, authentication failure, forbidden access, missing resources, duplicates, revoked/expired credentials, and rate limits.
- Audit registration, activation, login success/failure, logout/logout-all, recovery, pairing creation/submission/completion, credential rotation, device revocation, consent changes, and child deactivation.
- Audit only safe metadata. Never store or log passwords, tokens, pairing codes, credentials, sensitive request bodies, or unhashed IP/user-agent values.
- Confirm configuration-based secrets, `spring.jpa.open-in-view=false`, `ddl-auto=none`, liveness/readiness endpoints, and no Docker runtime dependency for local development.

## Database and migration requirements

- Validate every Liquibase changelog is versioned and included exactly once.
- Add only required constraints/indexes for uniqueness, ownership, active/revoked lookup, and audit time/family search.
- Confirm clean-database creation and upgrade from the previous schema. Do not use Hibernate schema generation.

## Required acceptance tests

Use the existing test infrastructure and isolated database state. Add or update tests for:

1. parent registration, activation, login, recovery, logout, and logout-all;
2. refresh rotation, old-token rejection, reuse detection, and concurrency;
3. family isolation and child ownership;
4. pairing expiration, replay rejection, failed-attempt threshold, and throttling;
5. successful device registration, credential rotation, old-credential rejection, and revocation;
6. revoked-device rejection on the next request;
7. consent history and family-scoped audit history;
8. parent/device authentication separation for every protected route category;
9. rate limits below-limit, reached-limit, reset, separate-key, and concurrent behavior;
10. sensitive-value exclusion from logs, exceptions, audit records, and response JSON;
11. Liquibase validation, clean creation, and upgrade.

## Verification

Run exactly:

```bash
./mvnw spotless:check
./mvnw test
./mvnw verify
./mvnw liquibase:validate
```

Report:

- the three-prompt MVP scope and confirmed gap matrix;
- changed files and package responsibilities;
- endpoints and route authorization;
- migration IDs, constraints, and database-test results;
- stable error codes and security behavior;
- exact unit and integration test totals;
- any environment blocker without weakening production security.
