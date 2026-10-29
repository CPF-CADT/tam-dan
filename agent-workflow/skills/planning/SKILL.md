---
name: planning
description: Turn approved product requirements into bounded, independently executable development tasks with acceptance criteria and dependencies.
---

# Planning

Use this skill when decomposing an approved feature or PRD. Do not implement code.

1. Read the relevant PRD and existing repository instructions.
2. Identify backend, mobile, contract, testing, migration, and integration boundaries.
3. Split work so each task has one owner, a small file boundary, explicit dependencies, and observable acceptance criteria.
4. Mark API work as blocked until the OpenAPI contract is approved.
5. Define actual verification commands for each task and record environment prerequisites.
6. Create task records from `agent-workflow/workflows/task-template.md` and send the dependency graph to the coordinator.

Reject ambiguous tasks, overlapping ownership, missing approval references, or criteria that cannot be tested.
