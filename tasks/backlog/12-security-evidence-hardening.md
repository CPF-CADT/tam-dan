# Backlog — Security Evidence Hardening

## Status

- State: `backlog`
- Priority: `P2 — hardening`
- Owner: coordinator/security
- Scope: evidence and operational hardening only
- Application feature development: not blocked
- Merge/deployment: still requires normal Integration Review approval

## Reason for deferral

Core feature logic, data ownership, native PostgreSQL verification, Flutter tests, and Android emulator verification are complete. The following evidence work is deferred because it is not currently treated as a critical feature-development blocker.

## Deferred items

### B1 — Dependency vulnerability evidence

- Configure a valid NVD API key or approved offline vulnerability database.
- Run OWASP Dependency-Check for Maven, Flutter, and Android/Gradle dependencies.
- Record tool version, database date, exact revisions, reports, findings, and remediation.
- If unavailable, record explicit release-owner risk acceptance with an expiry date.

### B2 — Production secret injection

- Replace production placeholders with externally injected values.
- Verify database URL, username, password, JWT secret, mail settings, TLS password, and public URL.
- Ensure production startup fails when required values are absent.
- Prevent accidental fallback to the `dev` profile.

### B3 — Production log review

- Review representative production-like logs.
- Confirm passwords, JWTs, bearer tokens, pairing codes, device credentials, and sensitive request bodies are not emitted.
- Attach the review command, date, environment, and result.

### B4 — Final security evidence review

- Reconcile backend, mobile, and testing revisions.
- Attach all logs and reports.
- Ask Integration Review for a final disposition after B1–B3 are complete.

## Completion commands

Use native runtime tooling; Docker is not required:

```bash
cd backend
./mvnw test
./mvnw verify
./mvnw liquibase:validate
./mvnw org.owasp:dependency-check-maven:check -DnvdApiKey="$NVD_API_KEY"

cd ../mobile
flutter analyze
flutter test
flutter test integration_test
```

## Current evidence

- Backend implementation: `ca17cde`
- Mobile runtime: `c4e69e8`
- Mobile testing evidence: `d9dfc2c`
- Native PostgreSQL and Android emulator checks passed.
- Dependency scan remains unavailable without NVD data.
- Production secret-injection and operational log evidence remain incomplete.

## Rule

Do not claim B1–B4 complete until the corresponding evidence is attached. Do not weaken tests or silently convert missing evidence into a passing result.
