# Mobile testing task

- ID: `06-mobile-testing`
- Role: `mobile-testing`
- Branch/worktree: `codex/13-figma-mobile-foundation` / `/Users/nak/.codex/worktrees/16a4/tam-dan`
- State: `in-progress`
- Requirements approval: PRD 13 — Tam Dan Figma Mobile Screen Implementation (`tasks/active/13-figma-mobile-screen-implementation.md`); implementation commits are present on this worktree
- OpenAPI approval: `agent-workflow/contracts/openapi-v1.2.0-proposal.yaml` / existing `/api/v1` contract; no API changes in this task
- Assigned role: Flutter integration/test cleanup owner
- Depends on: `04-mobile-development`

## Scope

Update stale test expectations to PRD 13, cover all 20 screen entry points and four-item parent/child navigation, and remove only unused duplicate legacy composition entry points. Do not change API behavior, production feature behavior, or merge/deploy.

## Acceptance criteria

- [x] Parent tests assert Home, Activity, Controls, and Settings instead of obsolete Overview, Alerts, and Policies tabs.
- [x] Duplicate Alex and child Home expectations are made exact without weakening coverage.
- [x] All 20 PRD 13 screen entry points and both four-item navigation shells are covered.
- [x] Unused legacy monolithic child entry points and duplicate parent tab composition are removed only where not referenced by production composition.
- [x] Format, analyze, unit/widget tests, integration tests when an emulator is available, and debug APK build are run and recorded.

## Verification

Required commands:

```text
dart format mobile
flutter analyze
flutter test
flutter test integration_test
flutter build apk --debug
```

Actual results:

- `dart format mobile` — exit `0`, 81 files checked, 0 changed.
- `flutter analyze` — exit `0`, no issues found.
- `flutter test` — exit `0`, 35 tests passed.
- `flutter test integration_test` — exit `1`; no supported device connected. Available macOS and Chrome targets are unsupported by this Android integration project.
- `flutter build apk --debug` — exit `0`; APK at `mobile/build/app/outputs/flutter-apk/app-debug.apk`.

## Handoff

- Changed files: test expectation/coverage updates, child pairing composition extraction, removal of unused legacy child and parent tab composition files, and this task record
- Risks/compatibility: no API or backend changes; `.codex/` remains untracked and was not staged; integration evidence is blocked until an Android emulator/device is available
- Review status: pending Integration Review; do not merge or deploy
