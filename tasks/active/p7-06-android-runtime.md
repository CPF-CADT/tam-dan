# Task: PRD 7 supported Android monitoring runtime

- ID: `P7-06`
- Role: `mobile-development`
- State: `ready`
- Branch/worktree: isolated Android worktree, branch prefix `codex/p7-android-runtime`
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29
- OpenAPI approval: `v1.1.0`; no API changes
- Figma/design approval: not applicable
- Dependencies: `P7-01`, `P7-04` for platform state contract

## Scope

Implement only supported Android monitoring behavior: permission lifecycle, durable offline queue, synchronization status, reboot/network/permission recovery, capability matrix, and truthful unsupported VPN/domain behavior. Do not claim unsupported enforcement.

## Acceptance criteria

- [ ] Event IDs are stable and duplicate uploads are safe.
- [ ] Queue size and event payloads are bounded.
- [ ] Keystore-backed credentials survive restart and clear on logout/revocation.
- [ ] `ACTIVE`, `FAILED`, `PENDING`, and `UNSUPPORTED` have deterministic transitions.
- [ ] VPN/domain limitations remain visible as `UNSUPPORTED` where not technically available.
- [ ] Android unit/instrumentation tests cover recovery and state transitions.

## Verification

```text
cd mobile
flutter analyze
flutter test
cd android
./gradlew :app:compileDebugKotlin :app:assembleDebug :app:testDebugUnitTest
adb devices -l
```

## Handoff

- Changed files: `mobile/android`, Android tests, and platform-channel contract docs only
- Risks/compatibility: no backend or Flutter widget changes; unsupported capabilities must not be silently upgraded
- Review status: pending
