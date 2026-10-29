# Multi-Agent Development Workflow Proposal

Status: Proposed — archived for approval

This proposal defines the development process and repository structure for Tam Dan. It does not implement application features, agent instructions, or reusable skills yet.

## Goals and non-negotiable gates

1. A coordinator owns scope, dependencies, task assignment, and final integration status.
2. Development cannot start until the requirements are approved.
3. API work cannot start until the OpenAPI contract is approved and versioned.
4. Backend and mobile work stay isolated in separate worktrees or branches.
5. Every developer task runs its actual automated verification before review.
6. A failed task returns to its responsible developer with the failing command and logs.
7. No merge or deployment occurs without integration review approval.

## Proposed repository structure

```text
AGENTS.md                              # shared rules for every agent
.agents/
  skills/
    planning/SKILL.md
    design/SKILL.md
    backend-development/SKILL.md
    mobile-development/SKILL.md
    backend-testing/SKILL.md
    mobile-testing/SKILL.md
    integration-review/SKILL.md
  workflows/
    development-workflow.md            # coordinator state machine and gates
    task-template.md                    # standard task, evidence, and handoff format
  contracts/
    openapi.yaml                        # approved API contract; versioned changes only
tasks/
  active/                               # approved work items in progress
  archive/                              # completed, rejected, or superseded proposals/tasks
  ...existing product PRDs...
backend/AGENTS.md                       # backend-specific rules, retained and extended
mobile/AGENTS.md                        # mobile-specific rules
```

The existing product PRDs remain the source for product scope. The shared root `AGENTS.md` supplies cross-cutting rules; more specific `backend/AGENTS.md` and `mobile/AGENTS.md` rules apply within their directories.

## Workflow

### 1. Plan

The Planning agent converts an approved product request into independently executable tasks. Each task includes scope, acceptance criteria, dependencies, affected boundary, verification commands, and a rollback or recovery note.

The coordinator rejects tasks that are ambiguous, overlap without ownership, or lack acceptance criteria.

### 2. Design and contract

The Design agent produces the technical design and identifies data, security, UI, and integration implications. For API changes, it updates a proposed OpenAPI contract and records compatibility impact.

The coordinator requests explicit approval for both requirements and the OpenAPI contract. No implementation task is dispatched before both approvals are recorded.

### 3. Parallel implementation

After the gates pass, the coordinator dispatches independent tasks in parallel:

- Backend Development works only in the backend worktree/branch.
- Mobile Development works only in the mobile worktree/branch.
- Backend Testing and Mobile Testing may work in parallel when their test scope is independent; otherwise testing follows the implementation dependency.

Each task reports changed files, commands run, test results, known risks, and an integration handoff. Agents must not modify another boundary's files without a new coordinator assignment.

### 4. Verification and failure loop

The responsible testing skill runs real automated tests, not only static inspection or mocked success claims. The report includes the exact command, exit code, and relevant logs.

If verification fails, the coordinator marks the task `needs-fix`, attaches the logs, and returns it to the responsible developer. The developer fixes the task and reruns the required checks. The task cannot advance on a partial or skipped test result unless the coordinator records an explicit environment blocker.

### 5. Integration review

The Integration Review agent checks the combined changes for:

- requirements and OpenAPI compatibility;
- backend/mobile boundary violations;
- authentication, authorization, privacy, and data-contract risks;
- migration and backward-compatibility concerns;
- actual test evidence from every affected component.

The reviewer either approves integration or returns specific findings to the responsible agent. Approval is required before merge.

### 6. Merge and deployment

The coordinator merges only after Integration Review approval and green required checks. Deployment is a separate approval gate and is never performed automatically as part of development or review.

## Agent responsibilities

| Agent | Owns | Must produce |
|---|---|---|
| Coordinator | State, dependencies, approvals, parallel dispatch, failure routing, merge/deploy gates | Task graph, status, assignments, evidence, final decision |
| Planning | Requirements decomposition and acceptance criteria | Approved task plan |
| Design | Architecture, UX/data-flow decisions, API impact | Design note and proposed contract changes |
| Backend Development | Spring Boot, PostgreSQL, migrations, backend API implementation | Isolated backend changes and handoff |
| Mobile Development | Flutter parent/child apps and platform integration | Isolated mobile changes and handoff |
| Backend Testing | Backend unit, integration, contract, security, and migration verification | Executed test report and logs |
| Mobile Testing | Flutter/unit/widget/integration and device/platform verification | Executed test report and logs |
| Integration Review | Cross-boundary correctness and release readiness | Review decision with findings or approval |

Agents may advise outside their ownership, but only the responsible developer changes that boundary unless the coordinator explicitly reassigns the task.

## Coordinator state model

```text
proposed
  -> requirements-approved
  -> contract-approved
  -> ready
  -> in-progress
  -> testing
  -> needs-fix -> in-progress
  -> review
  -> approved
  -> merged
  -> deployed (separate explicit approval)
```

Every transition records the actor, timestamp, evidence, and—when applicable—the approval or failing log that caused the transition.

## Reusable skill requirements

Each skill should define: scope, inputs, forbidden actions, required artifacts, verification commands, handoff format, and escalation rules. Skills should be procedural and reusable across the five existing product workstreams rather than embedding feature-specific behavior.

- Planning: requirements gate, task decomposition, dependency graph, acceptance criteria.
- Design: architecture and UX/data-flow decisions, threat/privacy review, OpenAPI proposal.
- Backend Development: Spring Boot conventions, migrations, security, DTO boundaries, backend handoff.
- Mobile Development: Flutter structure, platform boundaries, state/error handling, mobile handoff.
- Backend Testing: exact Maven/test commands, database/security/contract coverage, failure evidence.
- Mobile Testing: exact Flutter/test commands, widget/integration/device coverage, failure evidence.
- Integration Review: contract compatibility, isolation, security/privacy, migration, test evidence, merge gate.

## Approval requested

Approve or amend this proposal before the active `AGENTS.md`, `.agents/skills/`, workflow templates, and OpenAPI governance files are created.
