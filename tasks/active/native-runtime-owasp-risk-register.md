# Native Runtime OWASP / Risk Register

Review scope: native runtime evidence only. No Docker was used. No application code was changed.

## Task 11 F1/F2 execution record — 2026-10-29

- Task/role: Task 11 F1/F2 / coordinator.
- Approved requirements: `tasks/active/11-needs-fix-release-gate.md`; runtime plan: `tasks/active/10-real-runtime-verification-plan.md`.
- Approved API contract: OpenAPI `v1.1.0`; API changes were not made by this task.
- Isolated worktrees: backend `/Users/nak/.codex/worktrees/28cf/tam-dan-backend` at `ca17cdea9e2e42e5803a8c39cc7f7d0f5ad2bb59`; mobile/Android `/Users/nak/.codex/worktrees/28cf/tam-dan-mobile-runtime` at `c4e69e8b73b34c94b2eac1b8fba4e6bce26bfeaf`. Both were clean before the checks.
- Acceptance criteria: reproducible dependency inventories and vulnerability-scan disposition; fallback secret scan; production-profile/configuration review; Android runtime evidence; exact commands, exit codes, and log paths recorded here.

### F1/F2 commands executed

| Area | Exact command | Result | Evidence |
|---|---|---|---|
| Backend dependency inventory | `./mvnw -q dependency:tree -DoutputFile=/tmp/tamdan-task11-backend-dependency-tree.txt` | Exit 0; 206-line dependency tree written. | `/tmp/tamdan-task11-backend-dependency-tree.txt`, `/tmp/tamdan-task11-backend-dependency-tree.log` |
| Flutter dependency inventory | `flutter pub deps --style=compact` | Exit 0; inventory completed with Flutter 3.44.9/Dart 3.12.2. | `/tmp/tamdan-task11-flutter-deps.txt` |
| Android dependency inventory | `./gradlew :app:dependencies --configuration debugRuntimeClasspath` | Exit 0; Gradle dependency report completed. | `/tmp/tamdan-task11-android-deps.txt` |
| OWASP Dependency-Check | `./mvnw org.owasp:dependency-check-maven:check -DskipTests -DfailBuildOnCVSS=0` | Exit 1; **no vulnerability result claimed**. NVD update failed with `NvdApiException: Invalid API Key, length of 0 too short`; analysis then reported `NoDataException: No documents exist`. | `/tmp/tamdan-task11-dependency-check.log` |
| Backend fallback secret scan | `git grep -n -I -i -E 'password:[[:space:]]+[^$#"[:space:]][^$#"]*|base64-secret:[[:space:]]+[^$#"[:space:]][^$#"]*|BEGIN (RSA|EC|OPENSSH) PRIVATE KEY|AKIA[0-9A-Z]{16}|eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}' ca17cde -- backend/src/main backend/src/test` | Exit 1 (`git grep` no matches); no embedded secret matches. | `/tmp/tamdan-task11-secret-scan-backend.log` |
| Mobile/Android fallback scan | `git grep -n -I -i -E 'password[[:space:]]*[:=][[:space:]]*[^"'"'"'[:space:]]+|secret[[:space:]]*[:=][[:space:]]*[^"'"'"'[:space:]]+|token[[:space:]]*[:=][[:space:]]*[^"'"'"'[:space:]]+|BEGIN (RSA|EC|OPENSSH) PRIVATE KEY|AKIA[0-9A-Z]{16}|eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}' c4e69e8 -- mobile/lib mobile/test mobile/integration_test mobile/android` | Exit 0; matches were token/password variable names and test fixture values (`password`, `access`, `refresh`), not production secret literals. | `/tmp/tamdan-task11-secret-scan-mobile.log` |
| Dedicated scanner availability | `command -v gitleaks trufflehog semgrep osv-scanner` | All four unavailable; fallback scans were used. | Terminal output from this execution |

### F2 profile/configuration review

- `application.yml` defaults to `SPRING_PROFILES_ACTIVE` with `dev` only when unset; the `dev` profile groups `secret-samples`, `local`, and `api-docs`.
- `application-secret-samples.yml` is constrained by `spring.config.activate.on-profile: dev` and references `TAMDAN_DB_USERNAME`, `TAMDAN_DB_PASSWORD`, and `TAMDAN_JWT_BASE64_SECRET`; it contains no literal secret values.
- `application-prod.yml` does not activate `secret-samples` and contains no literal database password or JWT secret. The production datasource/mail values remain deployment placeholders (`localhost`/port 25), so production injection and endpoint values require release-owner/environment validation; this is not evidence of a production deployment.
- TLS configuration references `TAMDAN_TLS_KEYSTORE_PASSWORD` externally. No password, JWT secret, token, pairing code, or device credential was found in the backend fallback scan. `SensitiveLogRedactor`/`LoggingAspect` review shows exception text and sensitive values are redacted before logging; the scan did not replace operational log review.

### Android runtime evidence inspected

The existing evidence recorded by mobile runtime commit `c4e69e8` was present and its referenced logs were readable: `adb devices -l` exit 0 with `emulator-5554 device`; `flutter analyze` exit 0; `flutter test` exit 0 with 14 tests; `flutter build apk --debug` exit 0; `flutter test integration_test` exit 0 with 2 integration tests; and `./gradlew :app:compileDebugKotlin :app:assembleDebug` exit 0. Logs are under `/tmp/tamdan-mobile-runtime-hardening/`. This supports the Android runtime evidence only; it is not a vulnerability scan.

Revisions reviewed: backend `ca17cde` (security fixes from `871ad14`), mobile/Android runtime `c4e69e8`.

Disposition: `needs-fix` / security evidence remains incomplete. Feature and native runtime verification passed; no merge or deployment approval is implied.

## Evidence

| Check | Exact command | Result |
|---|---|---|
| Backend formatting | `./mvnw -q spotless:check` | Exit 0; passed. |
| Backend compilation/package | `./mvnw -q -DskipTests -Dcheckstyle.skip package` | Exit 0; passed. |
| Backend unit tests | `./mvnw -q test` | Exit 0; passed. |
| Flutter analysis | `flutter analyze` | Exit 0; no issues found. |
| Flutter tests | `flutter test` | Exit 0; 14 tests passed. |
| Flutter APK build | `flutter build apk --debug` | Exit 0; completed. |
| Flutter device integration | `flutter test integration_test` | Exit 0 on `emulator-5554`; runtime evidence recorded in `c4e69e8`. |
| Android compile/build | `./gradlew :app:compileDebugKotlin :app:assembleDebug` | Exit 0 on the native runtime worktree. |
| Android unit tests | `./gradlew :app:testDebugUnitTest` | Exit 0; no Android unit sources. |
| Native PostgreSQL | `pg_isready -h localhost -p 5432 -U tamdan` | Exit 0; accepting connections. |
| Android device | `adb devices -l` | Exit 0; `emulator-5554 device`. |
| Dependency inventory | `./mvnw dependency:tree -DoutputFile=/tmp/tamdan-dependency-tree.txt` | Exit 0; dependency inventory written. |
| Vulnerability audit | `./mvnw org.owasp:dependency-check-maven:check -DskipTests -DfailBuildOnCVSS=0` | Exit 1; NVD update failed because API key length was 0; no vulnerability result claimed. |
| Secret fallback scan | `rg -n -i 'password:[[:space:]]+[^$#\"]|base64-secret:[[:space:]]+[^$#\"]|BEGIN (RSA|EC|OPENSSH) PRIVATE KEY|AKIA[0-9A-Z]{16}' src/main/resources src/test/resources` | Exit 0 with no matches on backend `ca17cde`. |
| Dedicated scanners | `command -v gitleaks trufflehog semgrep osv-scanner` | Unavailable; fallback repository scans used. |

## Risk register

| ID / OWASP | Severity | Evidence | Owner | Status / remediation |
|---|---|---|---|---|
| R-01 / A06 Vulnerable Components | High | Dependency inventory passed, but Dependency-Check could not update NVD data because the API key was missing; dedicated scanners were unavailable. | Security/release | **Blocked evidence**. Configure NVD API/cache and rerun vulnerability audits. |
| R-02 / A02 Cryptographic Failures | Medium | Runtime sample credentials were replaced with environment references in `ca17cde`; fallback repository scan produced no credential matches. | Backend/security | **Mitigated in code; dedicated scanner pending**. |
| R-03 / A09 Logging and Monitoring | Medium | Structured redaction for tokens, credentials, headers, bodies, JWTs, and sensitive map values was added with focused tests. | Backend/security | **Mitigated in code; operational review pending**. Retain redacted-log evidence. |
| R-04 / A01 Access Control, A03 Injection, A07 Authentication | High | Native PostgreSQL verification passed: 77 unit tests and 111 integration tests, exit 0, log `/tmp/tamdan-corrected-verify.log`. | Backend testing | **Verified for native runtime**. Continue regression coverage on future API changes. |
| R-05 / A02/A04 Mobile native security | High | Emulator `emulator-5554` passed Flutter integration, Android build/compile, secure-storage compatibility, and unsupported-state checks. | Mobile/Android testing | **Verified for current runtime evidence**. Repeat on a physical device before release if required. |
| R-06 / A10 SSRF | Low | No `RestTemplate`, `WebClient`, `HttpClient`, `OkHttp`, or URL-fetch implementation was found in the reviewed backend revision. | Backend/security | **Not applicable to current code; monitor**. If external fetches are added, enforce allowlists, private-network blocking, redirect limits, timeouts, and size limits. |

## OWASP control summary

- A01: ownership checks and native PostgreSQL authorization evidence passed.
- A02: Android Keystore/AES-GCM and backend environment-secret handling are present; secret-scan evidence remains.
- A03: validation, parameterized persistence, and native integration evidence passed.
- A04: stale/partial/unsupported states and emulator capability behavior are evidenced.
- A05: sample profile activation and secret configuration were reviewed; `secret-samples` activates only under `dev`, production contains no literal secrets, and external production injection still requires release-owner/environment validation.
- A06: dependency inventory passed; vulnerability audit blocked by NVD API-key/data-update failure.
- A07: auth/session/revocation code and native integration evidence passed.
- A08: OpenAPI `v1.1.0` and controller drift validation passed on `ca17cde`.
- A09: redaction tests passed; operational log review remains.
- A10: no current outbound-fetch path found.

