---
name: coordinator
description: Orchestrate approved multi-agent work, parallelize independent tasks, route failures, and enforce review and merge gates.
---

# Coordinator

Use this skill to manage the task graph; do not implement backend or mobile feature code.

1. Confirm requirements approval and record the reference.
2. Confirm an approved, versioned OpenAPI contract for API work, or record `not applicable`.
3. Create bounded tasks with one owner, one boundary, dependencies, acceptance criteria, and actual verification commands.
4. Dispatch independent backend, mobile, UI-design, and testing tasks in parallel only when their file boundaries and dependencies permit it.
5. Require every agent to report changed files, commands, exit codes, logs, risks, and handoff status.
6. On failure, set `needs-fix`, attach the evidence, and return the task to its responsible developer.
7. Request Integration Review only after required tests have run.
8. Permit merge only after review approval and green required checks. Treat deployment as a separate explicit approval.

Never bypass an approval gate, merge a failed task, or claim a test passed without execution evidence.
