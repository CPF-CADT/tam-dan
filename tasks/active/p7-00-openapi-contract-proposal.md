# Task: PRD 7 OpenAPI contract proposal

- ID: `P7-00`
- Role: `planning`
- State: `in-progress`
- Branch/worktree: isolated contract-documentation worktree, branch prefix `codex/p7-openapi-contract`
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29
- OpenAPI approval: current baseline `v1.1.0`; proposed revision must be reviewed before implementation
- Figma/design approval: existing approved UI decisions only
- Dependencies: `P7-01` complete

## Scope

Prepare a versioned OpenAPI proposal for only the gaps identified by P7-01: typed report responses and freshness/completeness/source-device coverage, alert review/dismissal semantics, policy update/pause/remove/delivery/acknowledgement semantics, child lifecycle, device lifecycle, consent, and device health where required. Preserve existing approved v1.1.0 operations unless a documented correction is necessary.

Do not implement backend, Flutter, or Android code. Do not mark the contract approved; the coordinator and user must approve the proposal before API implementation.

## Acceptance criteria

- [ ] Every new or changed route has operationId, security scheme, request schema, response schema, error responses, ownership rule, and status-code decision.
- [ ] IDs remain numeric `int64` unless an explicit decision changes the contract.
- [ ] Report freshness, completeness, unknown evidence, delayed data, missing devices, and source-device coverage are typed and deterministic.
- [ ] Policy lifecycle and acknowledgement versioning are explicit and monotonic.
- [ ] No route permits client-controlled ownership escalation.
- [ ] Contract diff and backward-compatibility risks are documented.

## Verification

```text
rg -n "path:|operationId:|components:|schemas:" agent-workflow/contracts/openapi-v1.yaml
git diff --check
```

## Handoff

- Changed files: proposal contract and task record only
- Risks/compatibility: implementation remains blocked until coordinator/user approval
- Review status: pending
