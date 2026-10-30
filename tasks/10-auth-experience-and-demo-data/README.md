# PRD 10 — Auth Experience Refresh and Demo Data

## Status

- State: `PROPOSED`
- Priority: `P1 — usability improvement`
- Owner: coordinator/product
- Depends on: approved OpenAPI v1.2.0; new social-auth contract approval before Google/Apple implementation
- Deployment: not included

## Goal

Replace the weak parent authentication screen with a calm, modern mobile experience based on the supplied design references, while keeping MVVM boundaries and secure authentication behavior intact. Add safe, repeatable backend demo data so the parent dashboard can be evaluated without manually creating every entity.

The design direction is: soft blue-gray background, centered rounded authentication card, strong product title, clear hierarchy, large accessible controls, password visibility toggle, non-blocking error presentation, and Google/Apple sign-in entry points.

## Non-goals and gates

- Do not display raw backend exceptions, SQL, stack traces, URLs, tokens, or database details in the UI.
- Do not implement Google or Apple token exchange until a versioned OpenAPI social-auth contract is separately approved.
- Do not add password recovery routes; that remains deferred to the approved v1.2.1 process.
- Do not weaken authentication, rate limiting, validation, ownership, or secure storage.
- Demo data must be opt-in, deterministic, idempotent, clearly marked, and unavailable in production profiles.

## Design requirements

1. Use the Tam Dan brand name and “Your family safety center” positioning.
2. Use a rounded card with generous spacing, restrained blue accent color, readable typography, and accessible contrast.
3. Use email/login and password fields with inline validation, obscure/reveal password control, keyboard-safe layout, and loading state.
4. Show friendly, bounded error copy such as “We couldn’t sign you in. Check your details and try again.” Never render raw exception text.
5. Provide Google and Apple buttons as explicit social-auth actions. Until the contract and providers are approved, they must show a clear unavailable/not-configured state and must not fake success.
6. Keep registration available as a secondary action without crowding the sign-in flow.
7. Preserve loading, offline, unauthorized, and failed states in the ViewModel.

## Workstreams

### P10-01 — Parent auth UI and ViewModel

Owner: mobile-development

Refactor the auth screen and ViewModel presentation state. Widgets render state only; repositories and platform services remain outside widgets.

Acceptance criteria:

- UI matches the approved design direction on Android emulator sizes.
- Raw backend/SQL exception strings never reach rendered widgets.
- Login, registration, loading, offline, unauthorized, and retry states are tested.
- Password visibility, focus order, semantics labels, and keyboard dismissal work.
- Google and Apple controls are present but do not claim success before contract/provider approval.

Verification:

```bash
cd mobile
dart format --output=none --set-exit-if-changed lib test integration_test
flutter analyze
flutter test
flutter test integration_test -d emulator-5554
flutter build apk --debug
```

### P10-02 — Social-auth contract proposal

Owner: coordinator/contract

Prepare a separate versioned OpenAPI proposal for Google and Apple authorization-code/token exchange, account linking, nonce/state validation, account conflict behavior, provider failure behavior, and logout/revocation semantics.

Acceptance criteria:

- No provider token is sent to an unapproved endpoint.
- The proposal defines error and linking behavior without exposing provider tokens.
- Implementation remains blocked until the proposal is approved.

### P10-03 — Backend demo-data fixture

Owner: backend-development

Create an opt-in development-only fixture for one parent, family, children, devices, reports/events, alerts, policies, delivery states, and acknowledgements. Use existing services/repositories and valid ownership relationships.

Acceptance criteria:

- Fixture runs only under an explicit development command/profile.
- Re-running it is idempotent and does not duplicate users, devices, events, or policies.
- Passwords and device credentials are generated for local use and never logged.
- Production profile refuses or ignores demo-data activation.
- Tests verify ownership, DTO response shapes, and absence of cross-family data.

Verification:

```bash
cd backend
./mvnw -q spotless:check
./mvnw -q test
./mvnw -q liquibase:validate
```

### P10-04 — Integration review

Owner: integration-review

Review the exact mobile and backend revisions for design fidelity, MVVM boundaries, safe errors, contract compatibility, fixture isolation, authentication security, and sensitive logging.

Acceptance criteria:

- Review returns `APPROVED` or lists exact file/line findings.
- No merge or deployment occurs before approval.

## Dependency graph

1. Approve this PRD and freeze the current v1.2.0 contract.
2. Implement P10-01 UI/error handling and P10-03 demo fixture in parallel.
3. Prepare P10-02 social-auth proposal; do not implement provider exchange yet.
4. Run mobile/backend checks on exact revisions.
5. Request P10-04 Integration Review.
6. Approve social-auth implementation separately after the contract is approved.

## Definition of done

- Auth screen follows the approved design direction and never exposes raw backend errors.
- Existing email/login authentication remains functional and secure.
- Google/Apple entry points are honest, gated, and non-failing when unconfigured.
- Demo data is deterministic, opt-in, idempotent, and development-only.
- Required tests and evidence are recorded.
- Integration Review approves the exact revisions.
