---
name: mobile-testing
description: Verify Flutter parent, child, and platform-facing changes with real automated tests and explicit device limitations.
---

# Mobile testing

Use for mobile verification after requirements and relevant API contracts are approved. Read `mobile/AGENTS.md`. Run the narrowest useful tests first, then the required package-level suite. Cover MVVM ViewModel state transitions, repository/API mapping, widget flows, error/offline/permission states, and integration or platform behavior when applicable.

Run the repository's actual formatter, analyzer, unit/widget, and integration commands; do not substitute a review of source for execution. Confirm views do not directly perform data access and ViewModels expose the states required by the design. Record commands, exit codes, environment/device details, and logs. A missing emulator or platform service is a documented blocker. Return failures to the responsible mobile developer as `needs-fix` with a minimal reproduction.
