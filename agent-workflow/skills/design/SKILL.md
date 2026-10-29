---
name: design
description: Produce implementation-ready architecture, data-flow, UX, security, and API decisions before development begins.
---

# Design

Use this skill after Planning and before implementation. Do not silently change approved requirements.

When a Figma design is supplied, coordinate with the UI Design skill. Treat Figma as the visual source of truth, but require an explicit screen/state/API handoff before implementation is dispatched.

Document the chosen design, alternatives rejected, affected boundaries, data ownership, failure states, privacy/security implications, and compatibility impact. For API changes, update a proposed versioned OpenAPI document and list request, response, error, authentication, and migration changes. Clearly label assumptions and unresolved decisions.

The coordinator must approve the requirements and proposed OpenAPI contract before developers start. Keep design changes separate from backend and mobile implementation work.
