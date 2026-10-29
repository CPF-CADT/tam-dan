---
name: backend-testing
description: Verify backend changes with real unit, integration, security, contract, migration, and persistence tests and return actionable failure evidence.
---

# Backend testing

Use for backend verification after requirements and the API contract are approved. Read `backend/AGENTS.md` and test the changed behavior, not only compilation.

Select focused tests first, then the required suite. Use the repository's existing JUnit, AssertJ, Mockito, MockMvc, Testcontainers, Liquibase, and architecture-test conventions. Include database constraints, ownership/security behavior, transaction boundaries, and contract compatibility when affected.

Record every command, exit code, pass/fail result, and relevant log. If a test fails, identify the responsible implementation task, set it to `needs-fix`, and return the exact reproduction and logs. Do not weaken production code or skip a test to make the task pass.
