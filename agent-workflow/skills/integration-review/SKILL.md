---
name: integration-review
description: Review combined backend and mobile changes for contract compatibility, isolation, security, test evidence, and merge readiness.
---

# Integration review

Use only after implementation and required testing reports exist. Review the combined diff and verify that the requirements and approved OpenAPI version are satisfied.

Check backend/mobile boundary isolation, request/response and error compatibility, authentication and authorization, privacy and sensitive logging, migrations and backward compatibility, offline/retry behavior, and evidence that actual automated tests ran for every affected boundary.

Return one of:

- `approved`: required checks are green and merge is allowed;
- `needs-fix`: list each finding, owner, severity, and reproduction/evidence;
- `blocked`: identify the missing approval, unavailable environment, or unresolved dependency.

Do not merge or deploy. Deployment remains a separate explicit approval after merge.
