# Active task: Integration hardening and OWASP security remediation

- ID: `08-integration-hardening`
- State: `requirements-approved`
- PRD: [`tasks/06-integration-hardening-security/README.md`](../06-integration-hardening-security/README.md)
- Coordinator branch: `codex/coordinator`
- Merge gate: Integration Review must approve
- Scope decision: feature logic, ownership, validation, and injection prevention are the primary implementation focus; broad security hardening remains a fix/verification track.

## Delegated branches

- `codex/hardening-backend`
- `codex/hardening-mobile`
- `codex/hardening-android`
- `codex/hardening-security`
- `codex/hardening-backend-testing`
- `codex/hardening-mobile-testing`
- `codex/hardening-integration-review`

## Current blockers

1. Auth route mismatch.
2. Android secure-storage channel not registered.
3. Android VPN/domain enforcement unsupported.
4. OpenAPI UUID versus backend `Long` mismatch.
5. JPA entities exposed from API responses.
6. Usage-report freshness/completeness semantics incomplete.
7. Docker/PostgreSQL, Android device, and full integration evidence unavailable.

## Coordinator checklist

- [x] Approve this PRD requirements and feature-first scope.
- [x] Approve corrected OpenAPI route, payload, auth-header, and ID representations.
- [ ] Migrate backend controllers to `/api/v1` and add contract-drift tests.
- [ ] Dispatch isolated implementation and audit branches.
- [ ] Collect actual test commands and logs.
- [ ] Return failures to the responsible developer.
- [ ] Obtain OWASP security review.
- [ ] Obtain final Integration Review approval.
- [ ] Keep merge and deployment blocked until approval.
