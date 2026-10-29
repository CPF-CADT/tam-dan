# Tam Dan Figma handoff

Source file: [Tam Dan Mobile Prototype](https://www.figma.com/design/miR7yF1HtklTBK2QMEgFWZ/Tam-Dan-Mobile-Prototype?node-id=14-8)

Reviewed node: `14:8`, `Final • Parent + Child Prototype`.

## Current inventory

- 10 parent frames: `FINAL P01` through `FINAL P10`.
- 10 child frames: `FINAL C01` through `FINAL C10`.
- Screen size: 390×844.
- One reusable component: `Final Components/Button Primary`.
- Nine instances of that button component.
- Parent and child prototype sections are present on the same page.

## Handoff workflow

The UI Design agent must use the Figma file as the visual source of truth and produce:

1. a screen inventory with node-specific links;
2. a navigation and interaction flow;
3. a state matrix for loading, success, empty, error, offline, permission, unsupported, and recovery states where applicable;
4. component and design-token mapping for Flutter;
5. asset and icon requirements;
6. API/OpenAPI data requirements per screen;
7. accessibility and localization notes;
8. explicit `ready` or `needs-clarification` status.

The agent must inspect the selected frame and relevant child nodes, capture screenshots for visual reference, and use exact node IDs in the handoff. It must not start implementation while required states, interactions, or data dependencies are unclear.

## Decisions for this project

The coordinator may use these defaults without waiting for another design decision:

- The 390×844 frames are the visual baseline; implementation must remain adaptive for smaller and larger phones and large text settings.
- Parent navigation is Home, Activity, Controls, and Settings.
- Child navigation is Status/Home, Activity, Restrictions, and Settings where the child flow needs navigation; setup screens remain a guided flow.
- Every data-backed screen must define loading, success, empty, error, offline, permission, unsupported, and recovery behavior as applicable.
- Every parent action that changes a policy must show pending, confirmed, failed, and retry states. `ACTIVE` means the child device confirmed enforcement, not merely that the parent request was sent.
- Figma visual values become centralized Flutter design tokens for color, typography, spacing, radius, and component states. New one-off values require UI Design approval.
- Use standard Flutter/Material components where they satisfy the design, with semantic labels and a minimum 48×48 logical-pixel touch target.
- Target at least 4.5:1 contrast for normal text, support TalkBack and VoiceOver, and verify large text scaling and color-independent status communication.
- User-visible text must be localizable; monitoring, permissions, restrictions, and data collection must be explained transparently to the child-device user.

These decisions follow the [Flutter architecture guidance](https://docs.flutter.dev/app-architecture/guide), [Flutter accessibility guidance](https://docs.flutter.dev/ui/accessibility), [Flutter accessibility testing guidance](https://docs.flutter.dev/ui/accessibility/accessibility-testing), and [Material app-bar guidance](https://m3.material.io/components/app-bars/).

## Current readiness status

`ready-for-planning`; not yet `ready-for-implementation`.

The visual prototype is sufficient for Planning and UI Design breakdown. Mobile implementation remains gated on:

- approved product requirements for each screen and action;
- approved, versioned OpenAPI operations and schemas;
- a task-level screen/state/API mapping derived from this file;
- confirmation that required assets/icons can be implemented or supplied.

The coordinator may now create planning and design tasks. After requirements, OpenAPI, and the handoff mapping are approved, it may dispatch Mobile Development and Mobile Testing.
