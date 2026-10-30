# PRD 13 — Tam Dan Figma Mobile Screen Implementation

Status: PROPOSED

Source: Tam Dan Mobile Prototype, Figma root `14:8`, page `Final • Parent + Child Prototype`.

Target: Flutter mobile application in `mobile/`.

## Goal

Implement the 20 designed parent/child mobile screens from the Figma prototype using the existing Flutter MVVM, repository, secure-storage, device-identity, and design-system foundations. The result must preserve real API contracts and show honest loading, empty, offline, unsupported, and permission states where Figma does not define them.

The Figma design is the visual target. Existing code and approved API contracts remain the behavioral source of truth. Do not invent backend behavior to make a screen appear complete.

## Design system

- Device frame: 390×844; standard horizontal inset 20px; content width 350px.
- Canvas: `#FBFDFF`; white surface: `#FFFFFF`.
- Ink: `#060B14`; navy: `#05295C`; primary blue: `#096BF5`; muted text: `#52637D`.
- Pale blue: `#E0F0FF`; pale mint: `#DEFAED`; success: `#05AD6B`; warning: `#F28C05`; error: `#EB292E`.
- Warning surface: `#FFF2D1`; error surface: `#FFE3E5`; divider: `#E0E8F2`.
- Typography: Roboto; Display 32/40 bold; Heading 24/30 bold; Title 16/22 bold; Body 14/20 regular; Label 12/16 medium.
- Common spacing: 8, 12, 16, 18, 20, 24, 34, 36, 38, 40px.
- Cards: 16–20px radius; hero/status cards 22–24px; screen frame 28px; pills 12–14px.
- Primary button: 350×52px. Shared component node: `14:19`.
- No static image/vector assets were found; keep icons in a reusable icon mapping rather than scattering glyphs.

## Parent screens

### P01 — Parent Home (`15:8`)

Greeting, selected child/device card, daily metrics, alert summary, and protection status. Device card opens P02; alert card opens P09. Bottom navigation opens P03, P04, and P10. Use existing child, device, report, and alert view models; show safe empty/offline states.

### P02 — Child Overview (`15:11`)

Protection status, monitoring status, quick links for activity, controls, and website safety, and policy sync. Links open P03, P04, and P06. Do not claim VPN/domain enforcement is active when the current backend contract says `UNSUPPORTED`.

### P03 — Activity Summary (`15:14`)

Today/7 Days/30 Days segmented tabs, screen-time chart, and most-used apps. Use `UsageReportViewModel`; if app-level history is unavailable, render a truthful unavailable/empty state rather than fabricated data.

### P04 — Controls Hub (`15:17`)

Apps, websites, games, schedules, and active rules. Links open P05, P06, and P08. Use policy data and make unsupported policy types explicit.

### P05 — App Rule (`15:20`)

YouTube access mode, schedule toggle, and block-now action. Persist only through an existing supported policy API. If no app-rule contract exists, keep controls disabled with an explanatory state.

### P06 — Website Safety (`15:23`)

Summary/blocked/allowed tabs, safety counts, and recent websites. P07 is opened from the primary action. Use existing alert/policy data only; do not invent website event endpoints.

### P07 — Block Domain (`15:26`)

Domain field, duration choices, privacy/access explanation, validation, and save action returning to P06. Use existing policy validation and ownership checks. Never log submitted domains as sensitive request bodies.

### P08 — Game Internet (`15:29`)

Game access with allow/schedule/pause choices and active rule state. Use existing policy model when possible; otherwise label unsupported behavior honestly.

### P09 — Safety Alerts (`15:32`)

Medium/high/low alert groups and privacy note. Use `AlertViewModel`; alert detail navigation is not defined in Figma.

### P10 — Device Settings (`15:35`)

Protection, sync, permissions, device information, parent account, notifications, privacy, support, and bottom navigation. Use existing device/session data. Keep platform permission rows truthful.

Parent navigation must have four destinations: Home, Activity, Controls, Settings.

## Child screens

### C01 — Protection Active (`16:488`)

Protection status, monitoring permissions, VPN status, and policy sync. Bottom navigation opens C06, C07, and C10. VPN remains `UNSUPPORTED` when native enforcement is not implemented.

### C02 — Enable Protection (`16:491`)

Safety explanation and monitoring capabilities. Primary action opens C03.

### C03 — VPN Setup (`16:494`)

Setup stepper, VPN explanation, and privacy guarantees. Primary action opens C04. Do not fake a VPN connection.

### C04 — Permissions (`16:497`)

Usage access, draw-over-apps, notifications, and battery permission rows. Use Android platform adapters where available; each row needs granted, denied, unavailable, and retry behavior.

### C05 — Setup Complete (`16:500`)

Completed stepper and final protection state. Primary action opens C01 and must reflect actual device/platform state.

### C06 — Activity (`16:503`)

Activity tabs, screen time, and app usage. Use available child health data; show empty/unsupported states when history is unavailable.

### C07 — Restrictions (`16:506`)

Apps/websites/games tabs and restricted apps. C08 opens from the website tab; C09 opens from a restricted app. Preserve child-device ownership.

### C08 — Website Safety (`16:509`)

Safe/caution/blocked counts and recent activity. Use only supported backend data and label missing data.

### C09 — Game Restricted (`16:512`)

Restriction explanation, offline availability, and current-state warning. Primary action returns to C07. Do not claim active enforcement when the platform cannot verify it.

### C10 — Settings (`16:515`)

Protection, sync, permissions, device information, help/privacy, and about. Bottom navigation opens C01, C06, and C07.

Child navigation must have four destinations: Home, Activity, Restrictions, Settings. Setup flow is C02 → C03 → C04 → C05 → C01.

## Shared implementation ownership

Create or adapt reusable files under `mobile/lib/core/widgets` and `mobile/lib/core/theme`:

- primary button;
- bottom navigation;
- metric card;
- status card;
- icon badge;
- segmented tabs;
- app/website activity row;
- policy rule card;
- setup stepper;
- Figma-aligned colors, spacing, radii, typography, and status tokens.

Every public screen and reusable widget must have its own file. Private local widgets may remain in a file only when their names start with `_`.

## Feature ownership

- Parent: `ui/screens/parent/home`, `device`, `activity`, `controls`, `alerts`, and `settings`.
- Child: `ui/screens/child/home`, `setup`, `activity`, `restrictions`, and `settings`.
- View models stay beside their feature screens or in clearly shared feature view-model files.
- Do not place business logic in widgets.

## API and platform constraints

- Preserve `/api/v1` routes, DTO-only responses, JWT/device security, and ownership enforcement.
- Do not add Google/Apple authentication or password recovery; both require separate approved contracts.
- Do not add unsupported VPN/domain enforcement endpoints.
- Do not show raw SQL, JDBC, stack traces, tokens, or sensitive backend messages in the UI.
- Demo data may support local development only through the existing explicit, idempotent, development-only fixture.
- Figma has no designed loading, empty, offline, error, unsupported, denied-permission, or retry variants. Define accessible, consistent states using the existing view-model state model.

## Workstreams and dependencies

1. Shared design system and navigation shell.
2. Parent P01–P05 implementation.
3. Parent P06–P10 implementation.
4. Child C01–C05 setup/protection implementation.
5. Child C06–C10 activity/restrictions/settings implementation.
6. Flutter test and emulator validation.
7. Integration review and visual verification against each Figma frame.

Workstreams 2–5 may run in parallel after workstream 1 defines shared tokens/components and route composition. Testing agents must review each completed screen group before integration.

## Definition of done

- All 20 Figma screen frames have a corresponding Flutter screen or an explicitly documented contract-gated state.
- Parent and child four-item navigation works.
- Setup and detail flows follow the Figma transitions.
- Shared tokens/components are reused; no accidental Material-default styling remains where the Figma design specifies custom styling.
- Loading, empty, offline, unsupported, and denied states are safe and accessible.
- Flutter format, analyze, unit/widget tests, emulator integration tests, and debug APK build are recorded.
- No API, ownership, security, or logging regressions.
- Integration reviewer approves the combined commits before merge.
