# Mobile instructions

Follow these rules for Flutter parent and child applications.

The preferred reference pattern is documented in `agent-workflow/skills/mobile-development/references/velo-pp-patterns.md`, based on the Velo_PP project. Apply it to new or touched features without copying Velo_PP code or dependencies.

## Architecture: MVVM

- Use MVVM for feature code: `View` renders state and forwards user intent; `ViewModel` owns presentation state and commands; `Model`/repositories represent data and API or local persistence access.
- Views must not call HTTP clients, repositories, or platform services directly.
- ViewModels must not contain widget/layout code.
- Keep API DTOs and data models separate from presentation state when their lifecycles differ.
- Expose immutable state where practical and make loading, success, empty, offline, permission, and error states explicit.
- Keep navigation and dependency wiring at the composition/root boundary; do not create service locators inside widgets.
- Platform-specific code belongs behind a small adapter/service boundary.

## UI constraints

- Follow the approved UI design and accessibility acceptance criteria.
- Reuse shared theme, spacing, typography, and components before adding new ones.
- Do not hard-code colors, text styles, or dimensions in feature widgets when a design token exists.
- Support loading, error, empty, offline, and permission-recovery states for every user-visible data flow.
- Keep widgets small and testable; extract repeated UI into named components.

## API and testing

- Use only approved OpenAPI operations and schemas; do not invent endpoints or silently change response shapes.
- Keep secrets and credentials out of logs and persisted presentation state.
- Run formatting, analysis, and the relevant Flutter tests after changes. Report unavailable emulator/device checks as blockers, not passes.
