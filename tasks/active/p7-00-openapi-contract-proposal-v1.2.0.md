# P7-00 — OpenAPI v1.2.0 proposal

## Status

`PROPOSED — NOT APPROVED — IMPLEMENTATION BLOCKED`

The current approved contract remains [OpenAPI v1.1.0](../../agent-workflow/contracts/openapi-v1.yaml). The proposed contract is [openapi-v1.2.0-proposal.yaml](../../agent-workflow/contracts/openapi-v1.2.0-proposal.yaml). No backend, Flutter, Android, migration, or test implementation is approved by this document.

## Purpose and scope

This proposal resolves only the contract gaps identified in [P7-01](p7-01-contract-gap-matrix.md): typed report evidence, alert dismissal/review semantics, policy lifecycle and device delivery acknowledgement, child/device lifecycle, consent, and device health readback. Existing v1.1.0 operation IDs and numeric `int64` IDs are retained wherever possible.

## Contract decisions proposed

| Area | Proposed routes | Decision |
|---|---|---|
| Child lifecycle | `PATCH /families/me/children/{childId}`, `POST .../deactivation` | Parent JWT; server derives family; idempotent deactivation; no reassignment field. |
| Device lifecycle | child-scoped rename, revoke, credential rotation | Parent JWT; child and device relationship are checked together; rotation invalidates the old credential. |
| Consent | parent read/write and device disclosure confirmation | Consent version and timestamp are required; withdrawal is explicit; device confirmation cannot grant parent consent. |
| Device health | existing device update plus parent readback | Device may report only its own state; parent readback is child/family-scoped. `UNSUPPORTED` remains a truthful capability state. |
| Reports | typed `UsageReport` and `SourceDeviceCoverage` | `UNKNOWN`, `STALE`, `DELAYED`, and `PARTIAL` are first-class values. Missing evidence never becomes `SAFE`. |
| Alerts | typed list, review, and separate dismissal action | Review and dismissal are idempotent; alert ownership is resolved from the parent principal. |
| Policies | typed list/create/update/pause/remove/delivery and device acknowledgement | Version is monotonic; `ACTIVE` requires device acknowledgement; unsupported enforcement remains `UNSUPPORTED`. |

## Ownership and authorization rules

1. `familyId`, `parentId`, `childId`, `deviceId`, `policyId`, and `alertId` supplied by a client are selectors only. The server must resolve ownership from the authenticated parent or `X-Device-Credential` relationship.
2. Parent routes may access only children, devices, reports, alerts, consent records, and policies in the authenticated parent family.
3. Device routes may access only the paired device, its child, and policies assigned to that child. A device credential never grants parent operations.
4. Cross-family and revoked/inactive resources must use the repository's approved non-disclosing `404`/`403` behavior consistently.
5. No response may expose JPA entities, raw pairing codes after issuance, credentials after rotation, passwords, tokens, or internal ownership identifiers.

## Validation and state rules

- All IDs are positive `int64`; all timestamps are RFC 3339 date-time values in UTC.
- Free text is bounded and rejects control characters; enum values are closed unless explicitly marked otherwise.
- Page size is 1–100; report ranges and batch sizes are bounded by service policy; event IDs are stable for deduplication.
- Repository queries are parameterized and ownership-scoped. No client input is concatenated into SQL, JPQL, shell, URL, or log formats.
- Child states are `ACTIVE` or `DEACTIVATED`; device states are `PAIRING_PENDING`, `ACTIVE`, or `REVOKED`.
- Policy versions are positive and monotonic. `DRAFT -> PENDING_DELIVERY -> ACTIVE`, with `PAUSED`, `REMOVED`, `FAILED`, and `UNSUPPORTED` terminal/visible states as applicable.
- Report `risk` is `UNKNOWN` when evidence is insufficient. `completeness` and every source-device coverage entry must explain missing or delayed evidence where known.
- Acknowledgement of an older policy version cannot overwrite a newer version. `ACTIVE` is not valid without a matching device acknowledgement.

## Status-code decisions

- `200`: typed successful reads and state transitions that return a resource.
- `201`: creation, pairing completion, pairing-code creation, and credential rotation.
- `204`: idempotent logout, deactivation, revocation, removal, consent disclosure confirmation, health update, and other successful commands with no response body.
- `400`: malformed, out-of-range, invalid enum, invalid state transition, or validation failure.
- `401`: missing or invalid authentication/device credential.
- `404`: ownership-safe resource absence or cross-family access.
- `409`: replay, stale policy version, already revoked/deactivated resource, or conflicting lifecycle transition.
- `413`: batch exceeds the contract limit.
- `429`: pairing or authentication throttling.
- Errors use the existing RFC 7807-compatible `Problem` shape.

## Compatibility and breaking risks

### Additive and low-risk

- New lifecycle, consent, health readback, dismissal, delivery, and typed response schemas are additive routes.
- Existing operation IDs and `LongId` numeric IDs remain unchanged.
- v1.1.0 device credential and parent JWT security schemes remain unchanged.

### Requires explicit approval

- Existing untyped `ApiResponse.data` responses become typed for affected operations. Clients relying on arbitrary fields may need serializer updates.
- `PolicyAcknowledgement` adds required `policyVersion`; this is a request-shape change for `acknowledgePolicy` and requires coordinated client rollout.
- `AlertReview.state` is narrowed to `REVIEWED`; dismissal is a separate route. Clients sending `DISMISSED` to the review route must migrate.
- Authentication/logout response status and response envelopes are made explicit. Existing clients must tolerate the documented `204` command responses.
- The proposal uses `version: 1.2.0-proposal`; it must not replace v1.1.0 until approved, implemented, and compatibility-tested.

## Approval gate and implementation order

1. Coordinator and product owner review this proposal and record approval or requested changes.
2. Contract lint and compatibility checks pass against v1.1.0.
3. Backend implementation may begin only on an approved revision.
4. Mobile and Android implementations consume the approved schemas without inventing fields.
5. Backend native PostgreSQL tests, Flutter tests, Android runtime tests, and Integration Review are required before merge.

## Verification record

Required checks for this proposal:

```text
ruby -e "require 'yaml'; YAML.load_file('agent-workflow/contracts/openapi-v1.2.0-proposal.yaml'); puts 'yaml-ok'"
rg -n "path:|operationId:|components:|schemas:" agent-workflow/contracts/openapi-v1.2.0-proposal.yaml
git diff --check
```

Contract implementation and runtime tests are intentionally not run because this task changes documentation/contract proposal files only.

## Handoff

`PLANNING-COMPLETE / WAITING-FOR-CONTRACT-APPROVAL`

Changed files are limited to the proposal YAML and this proposal record. Do not dispatch P7-02/P7-03 API implementation for the proposed additions until approval is recorded.
