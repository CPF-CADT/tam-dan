# PRD — Identity and Device Backend

## 1. Purpose

Build the Spring Boot module that owns parent identity, family ownership, child profiles, pairing, device identity, device credentials, consent, and security audit records.

This module establishes who may access each protected resource. It does not own activity reports, risk assessments, alerts, or policies.

## 2. Goals

- Secure parent accounts and sessions.
- Create a strict family ownership boundary.
- Pair child devices without sharing parent credentials.
- Support device credential rotation and revocation.
- Provide trusted identity and authorization to other components.
- Preserve an auditable history of sensitive actions.

## 3. Core Features

### Parent Accounts

- Parent registration.
- Email or approved identity verification.
- Password hashing and secure credential validation.
- Login and logout.
- Short-lived access tokens.
- Secure refresh-token rotation.
- Account recovery.
- Account status and lock handling.
- Sign out from all sessions.

### Family Ownership

- Create a family boundary for parent-owned resources.
- Associate authenticated parents with the correct family.
- Prevent cross-family resource access.
- Provide ownership checks to the Safety and Policy module.
- Support future additional guardian roles without weakening authorization.

### Child Profiles

- Create a minimal child profile.
- List profiles belonging to the authenticated family.
- View and update a profile.
- Deactivate or request deletion of a profile.
- Associate authorized child devices with the profile.
- Avoid storing unnecessary child personal information.

### Pairing Codes

- Generate a cryptographically secure pairing code.
- Store only a protected representation of the code.
- Apply a short expiration time.
- Make the code single-use.
- Bind successful pairing to the correct family and child profile.
- Rate-limit generation and submission.
- Reject expired, reused, invalid, and unauthorized codes.
- Record pairing success and failure audit events.

### Child Device Registration

- Create a unique device identity after successful pairing.
- Store platform, application version, display name, and capability summary.
- Issue a credential used only by that device.
- Never issue the parent's session token to the child device.
- Return only the minimum child and family context required by the device.

### Device Authentication

- Authenticate every protected device request.
- Bind the credential to the correct device identity.
- Support credential rotation.
- Support immediate revocation.
- Reject expired, malformed, replayed, or revoked credentials.
- Expose trusted device identity to the Safety and Policy module.

### Device Management

- List devices belonging to a child profile.
- View device identity and latest reported health summary.
- Rename a device.
- Revoke a device.
- Prevent revoked devices from uploading events or downloading policies.
- Preserve the security audit trail after revocation.

### Consent Records

- Record the consent-document version.
- Record acknowledgement time and relevant actor.
- Record important permission-state changes reported by the device.
- Preserve required history when consent wording changes.

### Security Audit

- Record registration, login, recovery, pairing, credential rotation, revocation, consent, and deletion actions.
- Include actor, action, resource, result, time, and safe request context.
- Prevent secrets and raw credentials from entering audit records.
- Restrict audit access to authorized internal operations.

## 4. Core Data Entities

- `ParentAccount`
- `ParentSession`
- `Family`
- `FamilyMember`
- `ChildProfile`
- `PairingCode`
- `ChildDevice`
- `DeviceCredential`
- `ConsentRecord`
- `SecurityAuditEvent`

Database migrations must be versioned and tested.

## 5. API Groups

### Parent APIs

- Registration, verification, login, refresh, logout, and recovery.
- Parent-account read and update.
- Family and child-profile management.
- Pairing-code creation.
- Device list, details, rename, and revocation.

### Child Device APIs

- Pairing-code submission.
- Device credential rotation where supported.
- Device identity and pairing-state retrieval.
- Device health identity validation.

### Internal Module Contract

The Safety and Policy module requires trusted operations to:

- Resolve the authenticated parent and family.
- Resolve the authenticated child device.
- Verify ownership of a child profile or device.
- Verify that a device is active and not revoked.
- Receive relevant device-revocation events.

The contract must not expose password hashes, refresh tokens, or raw device credentials.

## 6. Business Rules

- Every child profile belongs to exactly one family ownership boundary.
- Every child device belongs to an authorized child profile.
- Pairing codes expire and may be used only once.
- Parent sessions and device credentials are separate.
- Revocation takes effect for subsequent protected requests.
- Ownership checks are mandatory even when an identifier exists.
- Deactivated profiles and revoked devices cannot create new activity.

## 7. Security Requirements

- Use a current approved password-hashing algorithm and safe parameters.
- Rotate refresh tokens and detect invalid reuse where designed.
- Store only hashed or otherwise protected pairing secrets.
- Rate-limit registration, login, recovery, pairing, and refresh operations.
- Validate all request payloads.
- Use HTTPS in production.
- Keep secrets outside source control.
- Do not place tokens, passwords, pairing codes, or child-sensitive data in logs.
- Test object-level authorization and family isolation.

## 8. Reliability Requirements

- Make pairing completion transactional.
- Make credential rotation and revocation atomic.
- Use database constraints to preserve ownership invariants.
- Return stable, documented error codes.
- Provide liveness and readiness health endpoints.
- Record operational metrics without exposing personal data.

## 9. Acceptance Criteria

The task is complete when:

- A parent can register, verify, authenticate, refresh, log out, and recover access.
- A parent can create and manage only their own child profiles.
- An expiring pairing code can register one physical child device exactly once.
- The paired device receives a separate credential and can authenticate.
- Credential rotation and device revocation work correctly.
- Revoked devices cannot upload data or retrieve policies.
- Cross-family access attempts fail in automated tests.
- Pairing replay, brute-force, token reuse, and authorization tests pass.
- Database migration, integration, and audit tests pass.

## 10. Out of Scope

- Activity-event storage and reporting.
- Risk classification and ML integration.
- Parent alert generation.
- Application, game, or domain policy management.
- Mobile UI implementation.
- Monitoring or VPN enforcement.
