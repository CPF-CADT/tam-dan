# Native Runtime OWASP / Risk Register

Review scope: native runtime evidence only. No Docker was used. No application code was changed.

Revisions reviewed: backend `080baf6`, mobile implementation `3c60255`, mobile runtime evidence `c4e69e8`.

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
| Dependency audit | `./mvnw org.owasp:dependency-check-maven:check -DskipTests -DfailBuildOnCVSS=0` | Exit 1; NVD update failed because API key length was 0; no vulnerability result claimed. |
| Dedicated scanners | `command -v gitleaks trufflehog semgrep osv-scanner` | Unavailable; fallback repository scans used. |

## Risk register

| ID / OWASP | Severity | Evidence | Owner | Status / remediation |
|---|---|---|---|---|
| R-01 / A06 Vulnerable Components | High | Dependency-Check failed before analysis: `NvdApiException: Invalid API Key, length of 0 too short`; dedicated scanners unavailable. | Security/release | **Blocked**. Configure NVD API/cache and run Maven, Flutter, and Android dependency audits. |
| R-02 / A02 Cryptographic Failures | Medium | Literal sample values found at `backend/src/main/resources/config/application-secret-samples.yml:17,23`, `application-tls.yml:13`, and test JWT configuration. | Backend/security | **Open**. Replace with non-secret placeholders or environment/secret-manager references; rotate any reused values. |
| R-03 / A09 Logging and Monitoring | Medium | `LoggingAspect` redacts only exception messages containing `password`, `token`, `credential`, or `pairing`; audit metadata redaction is tested, but request/header/body redaction coverage is absent. | Backend/security | **Open**. Use structured allowlisted security events and add tests for headers, bodies, tokens, credentials, and arbitrary sensitive values. |
| R-04 / A01 Access Control, A03 Injection, A07 Authentication | High | Native PostgreSQL verification passed: 75 unit tests and 110 integration tests, exit 0, log `/tmp/tamdan-verify-final-rerun.log`. | Backend testing | **Verified for native runtime**. Continue regression coverage on future API changes. |
| R-05 / A02/A04 Mobile native security | High | Emulator `emulator-5554` passed Flutter integration, Android build/compile, secure-storage compatibility, and unsupported-state checks. | Mobile/Android testing | **Verified for current runtime evidence**. Repeat on a physical device before release if required. |
| R-06 / A10 SSRF | Low | No `RestTemplate`, `WebClient`, `HttpClient`, `OkHttp`, or URL-fetch implementation was found in the reviewed backend revision. | Backend/security | **Not applicable to current code; monitor**. If external fetches are added, enforce allowlists, private-network blocking, redirect limits, timeouts, and size limits. |

## OWASP control summary

- A01: ownership checks and native PostgreSQL authorization evidence passed.
- A02: Android Keystore/AES-GCM and emulator channel evidence passed; sample-secret finding remains.
- A03: validation, parameterized persistence, and native integration evidence passed.
- A04: stale/partial/unsupported states and emulator capability behavior are evidenced.
- A05: no final production configuration review was executed.
- A06: dependency audit blocked by NVD API-key/data-update failure.
- A07: auth/session/revocation code and native integration evidence passed.
- A08: OpenAPI `v1.1.0` is versioned; contract/runtime drift evidence is incomplete.
- A09: audit redaction and security meters have tests; comprehensive log redaction is not evidenced.
- A10: no current outbound-fetch path found.

