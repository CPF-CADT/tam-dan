# Coordinator workflow

## Approval gates

The coordinator creates a task only after requirements are approved. If the task changes an API, the coordinator also requires an approved, versioned OpenAPI contract before dispatching implementation.

Required task states:

```text
proposed -> requirements-approved -> contract-approved -> ready
  -> in-progress -> testing -> review -> approved -> merged
                     ^       |
                     |       v
                  needs-fix <- failed verification or review finding
```

`contract-approved` may be skipped only when the coordinator records `not applicable`. Deployment is a separate explicit approval after `merged`.

## Parallel dispatch

After gates pass, dispatch independent tasks in parallel:

- backend implementation and backend testing when test work does not depend on unfinished code;
- mobile implementation and mobile testing under the same condition;
- UI design or contract documentation that has no write overlap with implementation.

Use separate branches or worktrees for backend and mobile. The coordinator owns the dependency graph and prevents two agents from editing the same files without an explicit handoff.

## Failure loop

When a test or review fails, set the task to `needs-fix`, preserve the exact command, exit code, and log excerpt/path, and return it to the responsible developer. The task returns to `testing` after the fix and cannot enter `review` on skipped or simulated verification.

## Handoff checklist

The coordinator requires the task record to contain:

- changed files and boundary;
- requirements and OpenAPI references;
- commands actually executed and results;
- known risks, migrations, compatibility notes, and follow-up work;
- reviewer decision and evidence.

Merge is allowed only for `approved` tasks with required checks passing and Integration Review approval. Deployment requires a separate user or release-owner decision.
