# Task: PRD 7 Flutter MVVM child journeys

- ID: `P7-05`
- Role: `mobile-development`
- State: `ready`
- Branch/worktree: isolated mobile worktree, branch prefix `codex/p7-mobile-child-mvvm`
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29
- OpenAPI approval: `v1.2.0`, approved 2026-10-29
- Dependencies: P7-01, P7-00, and shared MVVM/client infrastructure from P7-04

## Scope

Implement child pairing, consent/disclosure, permission guidance, secure credential/device-context persistence, monitoring/synchronization status, active policy display, offline last-known state, revoked-device recovery, and expired-session recovery.

## Acceptance criteria

- [ ] Pairing handles invalid, expired, used, offline, and successful states.
- [ ] Consent clearly explains collected data and required permissions.
- [ ] Secure storage clears credentials on logout/revocation and handles corrupt data safely.
- [ ] Device identity comes only from the authenticated pairing response.
- [ ] `ACTIVE` is shown only after device confirmation.
- [ ] MVVM tests cover all required success/error/recovery states.

## Verification

```text
cd mobile
dart format --output=none --set-exit-if-changed lib test
flutter analyze
flutter test
```

## Handoff

- Changed files: `mobile/lib/features/child/**`, child models/repositories/tests, and shared storage only when coordinated with P7-04
- Risks/compatibility: do not edit backend or Android Kotlin; no fake enforcement status
- Review status: pending
