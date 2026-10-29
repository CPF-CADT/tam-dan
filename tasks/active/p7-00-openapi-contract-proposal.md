# Task: PRD 7 OpenAPI contract proposal

- ID: `P7-00`
- Role: `planning`
- State: `approved`
- Branch/worktree: isolated contract-documentation worktree, branch prefix `codex/p7-openapi-contract`
- Requirements approval: `tasks/07-remaining-product-completion/README.md`, approved 2026-10-29
- OpenAPI approval: `v1.2.0`, approved by user message `ok aprove` on 2026-10-29
- Figma/design approval: existing approved UI decisions only
- Dependencies: `P7-01` complete

## Scope

Prepare a versioned OpenAPI proposal for only the gaps identified by P7-01: typed report responses and freshness/completeness/source-device coverage, alert review/dismissal semantics, policy update/pause/remove/delivery/acknowledgement semantics, child lifecycle, device lifecycle, consent, and device health where required. Preserve existing approved v1.1.0 operations unless a documented correction is necessary.

Do not implement backend, Flutter, or Android code. Do not mark the contract approved; the coordinator and user must approve the proposal before API implementation.

## Acceptance criteria

- [x] Every new or changed route has operationId, security scheme, request schema, response schema, error responses, ownership rule, and status-code decision.
- [x] IDs remain numeric `int64` unless an explicit decision changes the contract.
- [x] Report freshness, completeness, unknown evidence, delayed data, missing devices, and source-device coverage are typed and deterministic.
- [x] Policy lifecycle and acknowledgement versioning are explicit and monotonic.
- [x] No route permits client-controlled ownership escalation.
- [x] Contract diff and backward-compatibility risks are documented.

## Verification

```text
rg -n "path:|operationId:|components:|schemas:" agent-workflow/contracts/openapi-v1.yaml
git diff --check
```

Proposal-specific commands:

```text
ruby -e "require 'yaml'; YAML.load_file('agent-workflow/contracts/openapi-v1.2.0-proposal.yaml'); puts 'yaml-ok'"
rg -n "path:|operationId:|components:|schemas:" agent-workflow/contracts/openapi-v1.2.0-proposal.yaml
```

### Actual results

```text
ruby -e "require 'yaml'; YAML.load_file('agent-workflow/contracts/openapi-v1.2.0-proposal.yaml'); puts 'yaml-ok'"
exit 0; PASS; log: /tmp/p7-00-openapi-yaml.log

rg -n "path:|operationId:|components:|schemas:" agent-workflow/contracts/openapi-v1.2.0-proposal.yaml
exit 0; PASS; log: /tmp/p7-00-openapi-rg.log

git diff --check
exit 0; PASS; log: /tmp/p7-00-openapi-diff-check.log

Ruby compatibility check comparing operationIds in v1.1.0 and v1.2.0 proposal
exit 0; PASS; baseline 20 operationIds, proposal 34, no baseline operation missing; log: /tmp/p7-00-openapi-compat.log

Ruby local-reference check for the proposal
exit 0; PASS; 77 unique local references, none missing; log: /tmp/p7-00-openapi-ref-check.log

Application/backend/mobile tests
not run; documentation/contract proposal only
```

## Handoff

- Changed files: `agent-workflow/contracts/openapi-v1.2.0-proposal.yaml`, `tasks/active/p7-00-openapi-contract-proposal-v1.2.0.md`, this task record
- Risks/compatibility: see the versioned proposal; implementation remains blocked until coordinator/user approval
- Review status: pending coordinator/product-owner approval
