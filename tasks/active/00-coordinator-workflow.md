# Coordinator task: Tam Dan multi-agent workflow

- ID: `00-coordinator-workflow`
- Role: `coordinator`
- State: `needs-fix`
- Branch: `codex/coordinator`
- Requirements approval: Figma prototype reviewed; product requirements and API contract remain required gates
- OpenAPI approval: `pending`
- Figma/design approval: `ready-for-planning`; implementation handoff still required

## Objective

Manage the Tam Dan development tasks across isolated role branches. Do not implement application features from this task.

## Branch assignments

- `codex/planning` — requirements decomposition and task graph
- `codex/ui-design` — Figma screen/state/component handoff
- `codex/backend-development` — approved Spring Boot implementation
- `codex/mobile-development` — approved Flutter MVVM implementation
- `codex/backend-testing` — backend automated verification
- `codex/mobile-testing` — mobile automated verification
- `codex/integration-review` — combined review and merge recommendation

## Current coordinator result

Planning and UI Design completed read-only review. Their results are recorded in `01-planning-result.md` and `02-ui-design-result.md`.

The full implementation audit is recorded in `03-implementation-audit.md`. Contract decisions are approved in `04-contract-decisions.md` and versioned under `agent-workflow/contracts/`.

No implementation agent may start until the outstanding approval items in those results are resolved.

Implementation waves have run, but Integration Review still rejects merge. Remaining findings are recorded in the latest review: auth route mismatch, unregistered secure-storage channel, unsupported native enforcement, contract ID-type mismatch, entity leakage, and incomplete report semantics. No merge or deployment is allowed.

## Required order

1. Planning and UI Design may work in parallel.
2. Coordinator approves requirements and the screen/state mapping.
3. Design/API work produces and obtains approval for the versioned OpenAPI contract.
4. Backend and mobile implementation work starts in separate branches.
5. Backend and mobile testing run against the responsible changes and return failures with logs.
6. Integration Review approves or returns findings.
7. Merge and deployment require separate approval gates.
