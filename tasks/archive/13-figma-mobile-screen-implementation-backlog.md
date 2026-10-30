# Archive — PRD 13 Figma/SVG Mobile Follow-up Backlog

## Record metadata

- Task ID: `13-figma-mobile-screen-implementation`
- Role: `coordinator`
- State: `archived — unresolved follow-up items`
- Branch/worktree: `codex/13-figma-mobile-foundation` / `/Users/nak/.codex/worktrees/16a4/tam-dan`
- Requirements reference: `tasks/active/13-figma-mobile-screen-implementation.md`
- OpenAPI reference: approved baseline `v1.1.0` in `agent-workflow/contracts/openapi-v1.yaml`; `v1.2.0` remains a proposal in `agent-workflow/contracts/openapi-v1.2.0-proposal.yaml` and is not treated as approved here
- Figma reference: root `14:8`, page `Final • Parent + Child Prototype`; screen nodes `15:8`–`15:35` and `16:488`–`16:515`
- Isolated application work: none; this commit changes documentation only
- Scope: record concrete deferred work from the Figma/SVG review and PRD 13 implementation evidence. No application code, API contract, merge, or deployment changes.

## Why this record exists

The current branch contains the 20 public screen entry points, four-item parent/child navigation, shared Figma-aligned widgets/tokens, and truthful unsupported/error behavior covered by the existing mobile work. Those completed items are intentionally not repeated below.

The repository does not contain an exported Figma SVG or other screenshot baseline; the visual source currently remains the linked Figma file and the node inventory in `agent-workflow/skills/ui-design/references/tam-dan-figma-handoff.md`. The following items remain deferred because the current backend/mobile contracts, platform support, or verification evidence cannot yet satisfy the designed behavior.

## Deferred items

### B13-01 — Activity read models for apps, websites, and games

- **Screens / Figma requirement:** P03 `15:14`, P06 `15:23`, P08 `15:29`, C06 `16:503`, C08 `16:509`; show screen-time history, most-used apps, recent websites, safe/caution/blocked counts, and game-related activity.
- **Current limitation:** Backend `SafetyEvent` accepts generic `ACTIVITY` and `DOMAIN` payloads and `UsageReportResponse` exposes time/risk/completeness/coverage metadata, but there is no contract-backed list or aggregation for app names, website history, game activity, or the designed count/chart rows. The mobile parent repository currently reads only the usage-report summary; it has no activity-feed API. Do not fabricate rows from generic payloads.
- **Dependency:** A reviewed/versioned report/activity schema and API operation; backend aggregation/read-model implementation; then mobile repository/ViewModel mapping. The current approved v1.1.0 contract is insufficient for the designed detail rows.
- **Proposed owner:** backend safety/reporting + contract owner, followed by mobile parent/child feature owners.
- **Acceptance criteria:** Approved schemas define app/domain/game identifiers, time range, aggregation, freshness/completeness, and `UNKNOWN` behavior; backend returns ownership-scoped data; P03/P06/P08/C06/C08 render real rows when data exists and explicit empty/partial/unavailable states otherwise; no synthetic activity is used.
- **Priority:** `P1 — product capability`

### B13-02 — Settings capability contract and persistence

- **Screens / Figma requirement:** P10 `15:35` and C10 `16:515`; protection/sync/permissions/device information plus notifications, privacy, help/support, and about rows.
- **Current limitation:** Account read/update and device reads exist, but there is no product settings model or contract for notification preferences, privacy/support links, about/version data, or persisted protection/sync preferences. Platform permission state is local runtime data, not a backend settings resource. The settings screens therefore cannot make every Figma row functional or persist its intent.
- **Dependency:** Product decisions for setting ownership, privacy/retention, notification behavior, support content, and reauthentication; then an approved additive OpenAPI contract and coordinated backend/mobile implementation.
- **Proposed owner:** coordinator/product for decisions; backend identity/device owner and mobile settings owner for implementation.
- **Acceptance criteria:** Each P10/C10 row is classified as backed, local-only, informational, or deferred; any persisted setting has an approved request/response, ownership and audit behavior; screens show current values, save/failure/retry states, and never imply a setting changed when it did not.
- **Priority:** `P1 — product capability`

### B13-03 — Policy semantics for schedule, access mode, and duration

- **Screens / Figma requirement:** P05 `15:20` YouTube access mode/schedule/block-now; P07 `15:26` domain duration; P08 `15:29` game allow/schedule/pause; related P02/P04/C07/C09 status cards.
- **Current limitation:** Backend policy types are `APPLICATION`, `GAME`, and `DOMAIN` with a single `target`; the current request does not carry access mode, schedule/time zone, duration, or block-now semantics. The mobile repository exposes create/pause but not the full update/remove/delivery interaction needed by the screens. A policy type alone cannot safely represent these controls.
- **Dependency:** Approved policy schema extension covering schedule/time zone/DST, mode, duration, conflicts, lifecycle, and device delivery; backend and mobile implementation after contract approval.
- **Proposed owner:** backend safety/policy owner with mobile controls owner; coordinator owns the contract decision.
- **Acceptance criteria:** Every control maps to an approved field and operation; pending/active/failed/unsupported states reflect device acknowledgement; unsupported application/game/domain enforcement remains visible; validation prevents ambiguous or expired schedules and no action is reported as active before delivery confirmation.
- **Priority:** `P1 — behavior correctness`

### B13-04 — Android permission and VPN capability matrix

- **Screens / Figma requirement:** C01 `16:488`, C03 `16:494`, C04 `16:497`, C05 `16:500`, plus P02/P10 platform status rows; show VPN status, usage access, draw-over-apps, notifications, and battery permission with granted/denied/unavailable/retry behavior.
- **Current limitation:** The Android runtime reports monitoring/sync/capability state and can open usage-access settings, but the Flutter adapter only requests `usageAccess`; draw-over-apps, notification, and battery recovery actions are not all implemented behind the adapter. VPN/domain enforcement is explicitly `UNSUPPORTED`, and the approved Flutter–Kotlin decision record uses a different channel/method contract from the current `com.tamdan.mobile/android_runtime` implementation. No Android support/version/vendor matrix is recorded.
- **Dependency:** Product/platform decision for supported Android versions and permission scope; reconcile and approve the Flutter–Kotlin v1 contract; implement platform adapters and runtime evidence. VPN must remain unsupported unless separately proven and approved.
- **Proposed owner:** mobile Android/runtime owner, reviewed by mobile testing and coordinator.
- **Acceptance criteria:** A versioned matrix lists each permission/capability as supported, unsupported, denied, unavailable, or retryable; every C04 row has deterministic read/request/re-read behavior; C01/C03/C05 never fake VPN connection; channel methods and error envelopes match the approved contract; emulator and, when required, physical-device evidence is attached.
- **Priority:** `P0 — safety/platform truthfulness`

### B13-05 — Figma loading, empty, error, offline, unsupported, and permission variants

- **Screens / Figma requirement:** All data-backed parent and child screens; PRD 13 requires honest loading, empty, offline, unsupported, denied-permission, and retry states even though the prototype shows only the designed success visuals.
- **Current limitation:** The Figma file/handoff has no visual variants for these states. The branch has state models and selected widget coverage, but there is no approved frame-level specification for copy, hierarchy, accessibility semantics, retry placement, or which states are applicable to each of the 20 screens. This makes visual conformance and state completeness ambiguous.
- **Dependency:** UI-design state matrix and approval, including localized copy and accessibility decisions; then mobile implementation and widget tests.
- **Proposed owner:** UI Design for the state specification; mobile development for implementation; mobile testing for coverage.
- **Acceptance criteria:** A matrix maps every data-backed screen to applicable states and recovery actions; approved designs/copy exist for each required state; state transitions are test-covered; error messages are redacted and status is not communicated by color alone; screens preserve the Figma success layout where applicable.
- **Priority:** `P1 — UX/accessibility`

### B13-06 — Visual verification against Figma/SVG baselines

- **Screens / Figma requirement:** All 20 frames at 390×844, including spacing, typography, colors, radii, icons, navigation, and transition destinations.
- **Current limitation:** No Figma SVG export, local frame screenshots, or automated screenshot-diff baseline is present in this repository. Existing tests verify entry points and interactions, not pixel/layout conformance. Visual review cannot yet distinguish an intentional responsive adaptation from an accidental deviation.
- **Dependency:** Figma export or an approved screenshot capture set, a deterministic capture environment, and an available Android emulator/device; state-variant designs from B13-05 are also required for non-success views.
- **Proposed owner:** UI Design supplies the reference set; mobile development resolves deviations; mobile testing records captures and review evidence.
- **Acceptance criteria:** A per-node checklist links each frame to a captured implementation image and records disposition for material differences; the review covers all 20 success frames plus approved state variants, large text/semantics where applicable, and shared components; unresolved deviations become bounded follow-up items rather than being declared visually complete.
- **Priority:** `P1 — release evidence`

### B13-07 — Current-revision emulator/device verification

- **Screens / Figma requirement:** PRD 13 definition of done requires Flutter integration tests, Android runtime checks, debug APK validation, and visual verification for the implemented flows.
- **Current limitation:** On this branch, `tasks/active/06-mobile-testing.md` records `flutter test integration_test` exit `1` because no supported Android device was connected; macOS and Chrome targets are unsupported for this Android integration project. Historical emulator evidence exists for earlier revisions, but it is not evidence for this current Figma branch, and no physical-device evidence is recorded.
- **Dependency:** A connected supported Android emulator or physical device, installed APK, and a rerun on the current commit after B13-04 is settled.
- **Proposed owner:** mobile testing, with mobile development fixing failures returned by the test gate.
- **Acceptance criteria:** On the current revision, record `adb devices`, `flutter test integration_test -d <device>`, Android instrumentation/runtime checks, APK install/launch, and logs; verify setup, permission recovery, offline/sync recovery, policy unsupported states, and navigation; if the device remains unavailable, retain an explicit blocker and do not claim integration or visual verification passed.
- **Priority:** `P0 — verification gate`

## Explicitly not deferred here

- The 20 screen entry points and parent/child four-item navigation are covered by the current mobile tests.
- Shared Figma-aligned colors, spacing, typography, radii, primary button, navigation, metric/status cards, tabs, policy cards, and setup stepper already exist in the current branch.
- Generic activity/domain ingestion, typed usage-report metadata, alert review/dismissal, policy lifecycle/delivery, and truthful `UNSUPPORTED` capability states are existing implementation work. This record tracks their remaining product/read-model/verification gaps, not a duplicate implementation task.

## Verification record

Required documentation checks:

```text
git diff --check
rg -n "B13-0[1-7]|Figma/SVG|emulator|UNSUPPORTED" tasks/archive/13-figma-mobile-screen-implementation-backlog.md
```

Actual results for this archive commit:

- `git diff --check` — exit `0`.
- `rg -n ...` — exit `0`; all seven deferred records and required limitation categories were present.
- Application tests — not run; no application code or contract was changed.

## Handoff

- Changed files: `tasks/archive/13-figma-mobile-screen-implementation-backlog.md`
- Risks/compatibility: documentation-only; no API, backend, mobile, platform, merge, or deployment changes
- Review status: pending coordinator/integration review; do not merge or deploy from this record
