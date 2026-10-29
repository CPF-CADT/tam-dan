# Task: PRD 7 contract and implementation gap matrix

- ID: `P7-01`
- Role: `planning`
- State: `in-progress`
- Branch/worktree: coordinator planning boundary; documentation only
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29 by user request to process with multiple agents
- OpenAPI approval: `v1.1.0`, `agent-workflow/contracts/openapi-v1.yaml`
- Figma/design approval: `tasks/active/02-ui-design-result.md`; use existing approved design decisions
- Dependencies: none

## Scope

Compare PRD 7 functions, existing backend/mobile/Android code, and OpenAPI v1.1.0. Produce a route/schema/ownership gap matrix and bounded follow-up tasks. Do not implement application code or change the OpenAPI contract.

## Acceptance criteria

- [ ] Every PRD 7 backend/mobile/Android function is mapped to existing code or a bounded task.
- [ ] Every API gap identifies whether v1.1.0 already supports it or requires a new contract proposal.
- [ ] Ownership, validation, state-machine, and test gaps are explicit.
- [ ] No duplicate file ownership exists between follow-up tasks.

## Verification

Required commands:

```text
rg --files backend mobile agent-workflow/contracts
rg -n "path:|operationId:|class |Future<|fun |TODO|UNSUPPORTED" backend mobile agent-workflow/contracts/openapi-v1.yaml
```

## Handoff

- Changed files: task records and gap matrix only
- Risks/compatibility: API changes remain blocked until a versioned contract is approved
- Review status: pending
