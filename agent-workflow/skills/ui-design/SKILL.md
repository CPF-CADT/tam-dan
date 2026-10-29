---
name: ui-design
description: Define accessible, implementable mobile UI flows and design constraints for Tam Dan before Flutter implementation.
---

# UI design

Use for parent or child mobile screens, flows, states, components, and interaction decisions. This is distinct from technical architecture design.

For the current Tam Dan prototype, read [references/tam-dan-figma-handoff.md](references/tam-dan-figma-handoff.md) and inspect the linked Figma nodes before declaring the design ready.

## Required output

Document the user goal, screen hierarchy, primary and secondary actions, navigation, loading/empty/error/offline/permission states, validation and recovery behavior, accessibility requirements, and responsive/platform considerations. Identify reusable components and design tokens rather than specifying one-off styling.

For Figma handoff, also record exact node IDs, screenshots, component/asset usage, API/OpenAPI data dependencies, and a screen-to-ViewModel state mapping.

## Constraints

- Match the approved product requirements and privacy/consent behavior.
- Make monitoring, permissions, restrictions, and failures understandable to the user.
- Do not hide important status or destructive actions behind ambiguous icons or color alone.
- Provide semantics, labels, focus order, sufficient contrast, touch-target guidance, and dynamic-text behavior.
- Keep the design compatible with Flutter MVVM: views render ViewModel state and emit intents; they do not own business rules or data access.
- Do not invent API fields or backend behavior. Mark any needed contract change for Design and coordinator approval.

The output is a design decision record or approved screen specification that Mobile Development can implement and Mobile Testing can verify.

Use the project decisions in `references/tam-dan-figma-handoff.md` for baseline navigation, state behavior, accessibility, and responsive behavior. Mark the design `ready-for-planning` when the visual source and these defaults are sufficient to decompose work. Mark it `ready-for-implementation` only after requirements, OpenAPI, and task-level screen/state/API mapping are approved.
