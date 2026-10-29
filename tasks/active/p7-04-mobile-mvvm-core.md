# Task: PRD 7 Flutter MVVM parent and child core journeys

- ID: `P7-04`
- Role: `mobile-development`
- State: `ready`
- Branch/worktree: isolated mobile worktree, branch prefix `codex/p7-mobile-mvvm-core`
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29
- OpenAPI approval: `v1.1.0`; use only approved routes and payloads
- Figma/design approval: `tasks/active/02-ui-design-result.md` and approved Figma handoff
- Dependencies: `P7-01`; backend API availability may be mocked only in unit tests

## Scope

Implement the Flutter MVVM foundation and core parent/child journeys: authentication/session recovery, family/child selection, device list/pairing state, consent, permission guidance, monitoring/synchronization status, and clear loading/empty/offline/stale/error/unauthorized/unsupported states. Do not call HTTP or platform channels from widgets.

## Acceptance criteria

- [ ] Screens render ViewModel state only.
- [ ] ViewModels own state transitions and call repositories, not raw HTTP.
- [ ] Secure storage clears credentials on logout/revocation and never exposes raw credentials after handoff.
- [ ] IDs and serialization match OpenAPI v1.1.0.
- [ ] Placeholder device identity and fake enforcement success are absent.
- [ ] MVVM tests cover success, loading, offline, unauthorized, stale, error, and unsupported states.

## Verification

```text
cd mobile
flutter pub get
dart format --output=none --set-exit-if-changed lib test
flutter analyze
flutter test
```

## Handoff

- Changed files: `mobile/lib`, Flutter tests, and approved UI assets only
- Risks/compatibility: do not edit backend or Android implementation; contract changes require coordinator approval
- Review status: pending
