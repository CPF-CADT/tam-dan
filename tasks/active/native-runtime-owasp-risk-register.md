# Native Runtime OWASP / Risk Register

Review scope: native runtime evidence only. No Docker was used. No application code was changed.

Revisions reviewed: backend `ca17cde` (security fixes from `871ad14`), mobile implementation `3c60255`, mobile runtime evidence `c4e69e8`.

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
- A05: sample profile activation and secret configuration were reviewed; `secret-samples` activates only under `dev`, and production values use environment-backed configuration.
- A06: dependency inventory passed; vulnerability audit blocked by NVD API-key/data-update failure.
- A07: auth/session/revocation code and native integration evidence passed.
- A08: OpenAPI `v1.1.0` and controller drift validation passed on `ca17cde`.
- A09: redaction tests passed; operational log review remains.
- A10: no current outbound-fetch path found.

