---
name: mobile-development
description: Implement approved Flutter parent or child mobile tasks within an isolated mobile boundary and API contract.
---

# Mobile development

Use only for approved mobile tasks in an isolated mobile branch or worktree. Read `mobile/AGENTS.md` before editing. Identify whether the task belongs to the parent app, child app, or Android platform integration before editing.

For Flutter feature structure and implementation conventions, read [references/velo-pp-patterns.md](references/velo-pp-patterns.md). It records the approved reference pattern derived from `/Users/nak/CADT/projects/Velo_PP`.

Follow MVVM: views render state, ViewModels handle presentation decisions and user commands, and models/repositories handle data access. Keep network, state, UI, permissions, and platform-specific code separated according to the existing mobile structure. Handle loading, offline, permission, API-error, and recovery states required by the acceptance criteria. Do not change backend code or invent undocumented endpoints.

Use the UI Design skill for visual decisions and do not bypass approved design tokens, accessibility requirements, or shared components.

Before handoff, inspect the diff and run the required Flutter formatting, analysis, and tests for the affected package. Report actual commands, exit codes, changed files, platform limitations, and risks. A device test that cannot run is an environment limitation, not a passing result.
