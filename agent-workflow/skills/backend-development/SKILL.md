---
name: backend-development
description: Implement approved Spring Boot backend tasks while preserving API, security, persistence, and migration boundaries.
---

# Backend development

Use only for approved backend tasks in an isolated backend branch or worktree. Read `backend/AGENTS.md` before editing; those Spring Boot API instructions are authoritative for backend implementation.

Implement only the assigned acceptance criteria and approved OpenAPI contract. Keep controllers focused on HTTP concerns, services on business rules, repositories on persistence, and entities free of public setters. Add reviewed versioned migrations for schema changes. Never expose entities directly or log secrets, tokens, credentials, or sensitive request bodies.

Do not introduce a different architecture, feature-level package tree, generic base layer, speculative abstraction, or alternative DTO/entity pattern without an approved design decision.

Before handoff, inspect the diff, run the task's required Maven checks (at minimum the relevant tests; normally `./mvnw test`), and report exact commands, exit codes, changed files, migration notes, and remaining risks. If verification fails, return `needs-fix` with logs rather than claiming completion.
