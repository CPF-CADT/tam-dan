# Velo_PP mobile patterns

Reference project inspected: `/Users/nak/CADT/projects/Velo_PP`.

These are conventions observed in the reference project, not copied application code and not a dependency on that project.

## Directory boundaries

Use this general structure for Flutter feature work when it fits the target app:

```text
lib/
  core/
    constants/
    theme/
    utils/
    widgets/
  data/
    dtos/
    mock/
    repositories/<feature>/
  model/
  services/
  ui/
    screens/<feature>/
      <feature>_screen.dart
      view_model/<feature>_view_model.dart
      widgets/
    states/
    widgets/
```

Keep reusable application infrastructure in `core`, data access in `data`, domain models in `model`, feature presentation in `ui`, and cross-feature business helpers in `services`.

## MVVM implementation pattern

- A screen is the composition boundary and provides its ViewModel through `Provider`/`ChangeNotifier`.
- The ViewModel receives dependencies through its constructor.
- The ViewModel owns presentation state, user commands, loading/error/success transitions, and subscriptions to relevant shared state.
- Screens and widgets render ViewModel state and send events; they do not call repositories or Firebase directly.
- Feature widgets are kept below the screen level in a feature `widgets/` directory.
- ViewModels remove listeners and release resources in `dispose`.

## Data pattern

- Define repository interfaces in `data/repositories/<feature>/`.
- Keep concrete Firebase implementations and mock implementations separate from the interface.
- Keep DTOs in `data/dtos/` and convert them to domain models in an explicit `toModel()` step.
- Keep domain models in `model/` with immutable fields, `const` constructors where possible, and explicit map conversion when persistence requires it.
- Inject repositories and services rather than constructing them inside feature widgets.

## State pattern

- Use a small explicit async-state abstraction for loading, success, and error rather than scattering nullable flags.
- Add empty, offline, permission, and recovery states when the feature requires them.
- Use `ChangeNotifier` only for observable state changes; keep pure transformations in helpers/services.
- Notify listeners after state transitions and avoid exposing mutable collections directly when callers could mutate them.

## UI and design-system pattern

- Centralize colors, spacing, text styles, and theme configuration under `core/theme/`.
- Reuse shared widgets such as buttons, cards, and text fields from `core/widgets/`.
- Support localization through the app localization layer; do not hard-code user-facing strings in feature widgets.
- Keep screen layout, feature widgets, and shared widgets separate.
- Use the approved design tokens and accessibility requirements before introducing a new visual value or component.

## App composition and environments

- Keep app startup/composition in a common entry point and use separate environment entry points when mock and development behavior differ.
- Register providers at the composition boundary.
- Make mock data and mock repositories replaceable without changing feature UI code.

## Constraints for Tam Dan

- Prefer this structure for Tam Dan Flutter work, but follow the existing Tam Dan codebase if it has a more specific established convention.
- Do not add a state-management package or architectural layer only to copy Velo_PP; propose it first if the current project does not use it.
- Do not couple Tam Dan to Velo_PP imports, Firebase collections, models, assets, or service names.
- Do not move files globally just to match the reference; apply the pattern to new or touched features.
