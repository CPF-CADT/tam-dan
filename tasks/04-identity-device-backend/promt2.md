Below are 10 detailed development prompts for completing PRD 4. Each prompt is designed to be given to the coding agent independently.

---

## Prompt 1: Family provisioning and family API

```text
Implement the missing family provisioning flow for PRD 4.

Inspect the existing Family, FamilyMember, FamilyService, UserService, repository, controller, DTO, security, and Liquibase code before editing.

Requirements:

- Automatically create one active family and active OWNER membership when a parent successfully registers.
- Registration must remain transactional.
- Do not create duplicate active families for the same parent.
- Preserve existing registration behavior and response contracts.
- Add FamilyResponse endpoints:
  - POST /api/family
  - GET /api/family/current
- Resolve the parent ID only from authenticated identity.
- Never accept a trusted parentId or familyId from request data.
- Reject inactive or missing memberships with existing semantic exceptions.
- Use constructor injection.
- Use read-only transactions for reads.
- Return DTOs only; never expose Family or FamilyMember entities.
- Add database constraints preventing duplicate family memberships.
- Add integration tests for:
  - registration creates a family
  - current-family lookup
  - missing membership
  - inactive family
  - cross-parent family access
  - duplicate family provisioning

Do not modify unrelated modules.
Run spotless:check, mvn test, and mvn verify.
Report changed files, endpoints, migrations, and test totals.
```

---

## Prompt 2: Complete PRD 4 acceptance tests

```text
Add the full PRD 4 acceptance-test suite.

Inspect all existing unit, integration, controller, repository, Testcontainers, and security test conventions before editing.

Create acceptance tests for these scenarios:

1. Parent registration
2. Parent activation
3. Parent login
4. Refresh-token rotation
5. Logout
6. Logout-all
7. Account recovery
8. Family isolation
9. Child-profile ownership
10. Pairing-code expiration
11. Pairing-code replay rejection
12. Successful device registration
13. Device credential rotation
14. Device revocation
15. Revoked device rejection
16. Consent history
17. Audit history
18. Rate limits
19. Parent/device authentication separation
20. IdentityLookupService contract behavior

Test requirements:

- Use the existing integration-test infrastructure.
- Use isolated database state per test.
- Do not log passwords, refresh tokens, pairing codes, or device credentials.
- Assert HTTP status codes and stable error codes.
- Assert response DTO fields and absence of sensitive fields.
- Assert database persistence and revocation state where appropriate.
- Test cross-family access denial.
- Test replay and rotation race-sensitive behavior where practical.
- Do not weaken production security to make tests pass.

Do not modify unrelated tests.
Run the complete test suite and report exact totals by unit and integration test category.
```

---

## Prompt 3: Distributed rate limiting

```text
Replace the current in-memory rate limiter with a production-safe, configuration-driven rate-limiting design.

Inspect the current RateLimitService, security configuration, application YAML files, exception handling, and test setup first.

Rate-limited operations:

- parent registration
- parent login
- account recovery
- activation resend
- pairing-code generation
- pairing-code submission
- refresh-token requests

Requirements:

- Use configuration properties for limits and time windows.
- Support multiple application instances without relying on local JVM memory.
- Return HTTP 429 Too Many Requests.
- Add a stable error code for rate-limit failures.
- Do not include passwords, tokens, pairing codes, or credentials in rate-limit keys.
- Use normalized login/email or one-way hashes where appropriate.
- Avoid user-enumeration leaks.
- Keep rate limiting separate from authentication and business logic.
- Preserve constructor injection and package-layer rules.
- Add tests for:
  - below-limit requests
  - limit reached
  - window reset
  - separate keys
  - concurrent requests
  - login failure throttling
  - pairing submission throttling
  - refresh throttling

If an external store is unavailable in the project, implement a clean provider interface and a documented development implementation without pretending it is distributed.

Do not modify unrelated modules.
Run spotless:check, mvn test, and mvn verify.
```

---

## Prompt 4: Complete audit coverage

```text
Complete the PRD 4 security-audit implementation.

Inspect AuditService, SecurityAuditEvent, audit repositories, all authentication services, pairing services, device services, consent services, recovery flows, and logging configuration.

Audit these actions:

- registration
- activation
- login success
- login failure
- logout
- logout-all
- recovery request
- recovery completion
- pairing-code creation
- pairing submission success
- pairing submission failure
- pairing completion
- credential rotation
- device revocation
- consent changes
- child deactivation

Requirements:

- Never store or log passwords.
- Never store or log refresh tokens.
- Never store or log raw pairing codes.
- Never store or log raw device credentials.
- Never store sensitive request bodies.
- Hash IP addresses and user-agent values.
- Store only explicitly approved safe metadata.
- Use stable action and result values.
- Audit failures without revealing whether an account exists.
- Ensure audit writes do not expose secrets through exception messages.
- Ensure audit records are transactionally consistent with the business operation.
- Add tests for each audited action and sensitive-value exclusion.
- Preserve family authorization for audit search.
- Prevent one family from reading another family’s audit records.

Do not add speculative audit fields.
Do not modify unrelated modules.
Run the full test suite and report audit coverage.
```

---

## Prompt 5: Parent/device authentication separation

```text
Harden parent and device authentication separation.

Inspect SecurityConfiguration, DeviceAuthenticationFilter, JWT authentication, AuthenticatedUserIdResolver, AuthenticatedDeviceIdResolver, authorities, controllers, and all protected routes.

Define three route categories:

- parent-only
- device-only
- shared authenticated routes

Requirements:

- Parent JWTs must not authenticate as devices.
- Device credentials must not authenticate as parents.
- Device credentials must not access parent account, family administration, or parent session routes.
- Parent JWTs must not access device-only event or policy routes.
- Device authentication must use a dedicated authority.
- Device authentication must expose only AuthenticatedDeviceContext.
- Parent authentication must expose only parent identity context.
- Reject requests containing conflicting parent and device credentials.
- Ensure revoked device credentials fail on the next request.
- Ensure expired credentials fail.
- Add explicit route authorization rules.
- Add integration tests for every route category.
- Do not use raw tokens as principal data.
- Do not log authentication headers.

Do not weaken `/api/**` protection.
Preserve existing public routes.
Run architecture tests, spotless:check, mvn test, and mvn verify.
```

---

## Prompt 6: Device credential lifecycle completion

```text
Complete the child-device credential lifecycle.

Inspect ChildDevice, DeviceCredential, DeviceCredentialRepository, DeviceCredentialService, DeviceService, revocation events/listeners, and device authentication filter.

Requirements:

- Add authenticated endpoints for credential rotation and revocation.
- Enforce family ownership for parent-triggered operations.
- Enforce device identity for device-triggered operations.
- Rotate credentials transactionally.
- Revoke the previous credential before the replacement becomes usable.
- Populate replacedByCredentialId.
- Store only credential hashes.
- Return raw credentials only once.
- Reject expired, revoked, inactive, or child-inactive credentials.
- Revoke all credentials when a device is revoked.
- Publish DeviceRevocationEvent with:
  - deviceId
  - childProfileId
  - familyId
  - revokedAt
  - reason
- Ensure listeners receive the event only after the state transition is valid.
- Add tests for:
  - successful rotation
  - old credential rejection
  - new credential acceptance
  - expired credential
  - revoked credential
  - device revocation
  - cross-family access
  - repeated revocation

Do not expose DeviceCredential entities.
Do not log credentials.
Run the full verification commands.
```

---

## Prompt 7: Stable API error contract

```text
Standardize the PRD 4 API error contract.

Inspect GlobalExceptionHandler, semantic exceptions, validation handling, response DTOs, existing integration tests, and all new PRD 4 endpoints.

Define stable error codes for at least:

- invalid input
- invalid password
- invalid credentials
- resource not found
- forbidden access
- duplicate resource
- expired token
- revoked token
- expired pairing code
- replayed pairing code
- inactive device
- rate limit exceeded
- database conflict

Requirements:

- Preserve correct HTTP status codes.
- Return a consistent RFC 7807-compatible structure.
- Include stable machine-readable code.
- Include request path.
- Include request ID when available.
- Include field errors for validation failures.
- Do not expose stack traces, SQL details, passwords, tokens, or internal identifiers.
- Avoid account and credential enumeration.
- Keep existing exception classes where possible.
- Update tests for every semantic exception.
- Ensure production responses do not leak implementation details.

Do not rewrite unrelated error handling.
Run spotless:check, mvn test, and mvn verify.
```

---

## Prompt 8: Database migration and integrity validation

```text
Validate and harden all PRD 4 database migrations.

Inspect every Liquibase changelog, master include order, entity mappings, constraints, indexes, and integration database setup.

Requirements:

- Verify all migrations are versioned and included exactly once.
- Validate clean-database creation from zero.
- Validate upgrade from the previous schema.
- Add foreign keys for every ownership relationship.
- Add uniqueness constraints for:
  - user login
  - user email
  - family membership
  - refresh-token hashes
  - pairing-code hashes
  - device credential hashes
- Add indexes for:
  - active family membership lookup
  - child-family lookup
  - active pairing-code lookup
  - active credential lookup
  - audit family/time search
- Ensure timestamps use UTC-compatible database types.
- Ensure inactive and revoked records remain queryable for audit/history.
- Ensure migration rollback behavior is documented where supported.
- Add migration integration tests.
- Do not use Hibernate schema generation.
- Do not modify unrelated tables.

Run Liquibase validation, migration tests, spotless:check, mvn test, and mvn verify.
Report migration IDs and database-test results.
```

---

## Prompt 9: Internal identity contract validation

```text
Finalize and validate the internal identity contract used by Safety and Policy modules.

Inspect IdentityLookupService, IdentityLookupServiceImpl, AuthenticatedParentContext, AuthenticatedDeviceContext, device authentication, family membership, child ownership, and active-device checks.

Requirements:

- `resolveParent(Long parentId)` returns only parentId and familyId.
- `resolveDevice(Long deviceId)` returns only deviceId, childProfileId, and familyId.
- `verifyChildOwnership` must require an active child and active family membership.
- `verifyActiveDevice` must reject:
  - missing devices
  - inactive devices
  - revoked devices
  - inactive children
  - inactive families
- Missing or unauthorized resources must use semantic exceptions consistently.
- Never expose:
  - password hashes
  - refresh tokens
  - raw device credentials
  - pairing codes
  - capabilities unless explicitly required
  - unnecessary child profile fields
- Add contract tests that simulate Safety and Policy callers.
- Add tests for cross-family identifiers.
- Add tests for revoked devices immediately after revocation.
- Preserve package-layer architecture.
- Do not add generic base classes or speculative abstractions.

Run architecture tests and the complete build.
```

---

## Prompt 10: Final production-readiness and security review

```text
Perform the final PRD 4 production-readiness review and fix all confirmed issues.

Inspect the complete backend implementation from Steps 1 through 10.

Verify:

- Parent registration and activation
- Parent login and session rotation
- Logout and logout-all
- Recovery flows
- Family provisioning and isolation
- Child ownership
- Pairing-code expiration and replay rejection
- Device registration
- Device credential rotation
- Device revocation
- Consent history
- Audit history
- Rate limits
- Parent/device authentication separation
- Identity contract behavior
- Liquibase migration integrity

Security review requirements:

- Search for password, token, pairing-code, credential, and request-body logging.
- Search for raw secret serialization.
- Search for controllers returning entities.
- Search for repository queries missing family ownership.
- Search for endpoints accepting trusted owner or family IDs from clients.
- Search for transactions missing on security-sensitive writes.
- Search for read methods missing read-only transactions.
- Search for race conditions in token rotation and pairing-code use.
- Search for inconsistent error statuses or codes.
- Search for accidental public routes.
- Confirm `spring.jpa.open-in-view=false`.
- Confirm liveness/readiness endpoints.
- Confirm no Docker runtime dependency remains for local development.
- Confirm configuration and secret handling matches the project policy.

Run exactly:

```bash
./mvnw spotless:check
./mvnw test
./mvnw verify
```

If tests fail, distinguish code failures from environment failures.
Report:

- changed files
- endpoints
- migrations
- security behavior
- acceptance-test totals
- failed tests and exact causes
- remaining risks

Do not modify unrelated modules.

```
