# P7-08 F5 Android instrumentation evidence

- Status: `IMPLEMENTATION-COMPLETE / TEST-EVIDENCE-PASS`
- Task: F5 — Android/mobile-testing recovery coverage
- Role: `mobile-testing`
- Branch: `codex/p7-f5-instrumentation`
- Worktree: `/Users/nak/.codex/worktrees/p7-f5-instrumentation/tam-dan`
- Approved requirements: PRD 7, F5 follow-up from `tasks/active/07-integration-review.md`
- Approved OpenAPI: `v1.2.0`; no API or backend files changed
- Android implementation under test: `75f9b02`
- Flutter parent input: `ee326dd`
- Flutter child input: `5071ba6`
- Device: `emulator-5554`, Pixel 8 Pro AVD, API 35

## Changes

- Added AndroidX instrumentation runner configuration and test dependencies.
- Added six emulator instrumentation tests covering:
  - Android Keystore credential persistence across store recreation and explicit clear;
  - persisted event queue recovery and upload acknowledgement;
  - permission removal, `PENDING`, `FAILED`, and restored `ACTIVE` states;
  - offline synchronization recovery to `COMPLETE`;
  - exact policy-version acknowledgement;
  - explicit `UNSUPPORTED` results for domain and VPN enforcement.
- Added one Flutter integration test for the approved `com.tamdan.mobile/android_runtime` channel.
- Added the Flutter `integration_test` SDK dependency, which was missing from the prior P7-08 run.
- Added Android test manifest overrides required by the resolved AndroidX test runner on API 35.

## Verification

| Check | Result | Command | Evidence |
|---|---|---|---|
| Flutter dependencies | PASS | `flutter pub get` | `/tmp/tamdan-p7-f5-pub-get.log` |
| Dart formatting | PASS | `dart format --output=none --set-exit-if-changed lib test integration_test` | `/tmp/tamdan-p7-f5-dart-format.log` |
| Flutter analysis | PASS | `flutter analyze` | `/tmp/tamdan-p7-f5-flutter-analyze.log` |
| Flutter unit/widget tests | PASS | `flutter test` | `/tmp/tamdan-p7-f5-flutter-test.log` |
| Flutter APK build | PASS | `flutter build apk --debug` | `/tmp/tamdan-p7-f5-flutter-build.log` |
| Kotlin compilation/unit tests | PASS | `gradle :app:compileDebugKotlin :app:testDebugUnitTest` | `/tmp/tamdan-p7-f5-gradle-unit.log` |
| Android instrumentation | PASS — 6 tests | `gradle :app:connectedDebugAndroidTest` | `/tmp/tamdan-p7-f5-instrumentation.log` |
| Flutter emulator integration | PASS — 1 test | `flutter test integration_test -d emulator-5554` | `/tmp/tamdan-p7-f5-flutter-integration.log` |
| APK install | PASS | `adb install -r build/app/outputs/flutter-apk/app-debug.apk` | `/tmp/tamdan-p7-f5-adb-install.log` |
| Process restart launch | PASS | `adb shell am force-stop` then `adb shell monkey -p com.tamdan.mobile 1` | `/tmp/tamdan-p7-f5-adb-launch.log` |
| Emulator/log review | PASS | `adb devices`, PID and filtered logcat review | `/tmp/tamdan-p7-f5-adb-devices.log`, `/tmp/tamdan-p7-f5-adb-pid.log`, `/tmp/tamdan-p7-f5-adb-logcat-review.log` |

## Honest limitations

- No physical-device test was run.
- No real device reboot was run; process-stop/relaunch was verified, and persistence was verified through recreation of the Android storage adapters.
- VPN/domain enforcement remains `UNSUPPORTED`; these tests assert that state and do not claim enforcement.
- No backend or OpenAPI changes were made.
- No merge or deployment was performed.
