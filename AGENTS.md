# Tam Dan agent instructions

You are working in a coordinated multi-agent repository. Read this file first, then the nearest directory-specific `AGENTS.md`.

## Required gates

- Do not implement application code until requirements are approved by the coordinator.
- Do not implement API changes until the OpenAPI contract is approved and versioned.
- Keep backend and mobile work in separate branches or worktrees.
- Run real automated tests for every changed boundary; never report unrun tests as passing.
- If a check fails, return the task to its responsible developer with the exact command, exit code, and relevant log.
- Do not merge or deploy without Integration Review approval.

## Coordination

Use exactly the reusable skill matching the assigned role in [`agent-workflow/skills/`](agent-workflow/skills/). Use `coordinator` for orchestration, `ui-design` for mobile UI decisions, `mobile-development` for MVVM Flutter implementation, and `mobile-testing` for mobile verification. The coordinator follows [`agent-workflow/workflows/development-workflow.md`](agent-workflow/workflows/development-workflow.md) and uses [`agent-workflow/workflows/task-template.md`](agent-workflow/workflows/task-template.md) for every task.

Before starting work, record:

1. task ID and assigned role;
2. approved requirements reference;
3. approved OpenAPI version, or `not applicable`;
4. isolated branch/worktree;
5. acceptance criteria and verification commands.

Keep changes focused. Do not modify another agent's boundary without an explicit coordinator reassignment. Report assumptions, changed files, test commands, results, risks, and handoff status.

## Directory ownership

- `backend/`: backend agents; follow `backend/AGENTS.md`.
- `mobile/`: mobile agents; follow `mobile/AGENTS.md` when present.
- `tasks/`: requirements, task records, and archived proposals.
- `agent-workflow/`: shared workflow and reusable role skills.
