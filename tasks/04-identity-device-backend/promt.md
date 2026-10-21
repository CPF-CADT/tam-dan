## Step 1 — Establish PRD 4 contracts and database conventions

### Development prompt

```text
Implement Step 1 of PRD 4: establish the identity/device backend foundation.

Inspect the existing backend before changing files. Preserve the current package-by-layer structure and existing behavior.

Create or confirm these package responsibilities:

- com.tamdan.entities: JPA entities and persistence enums
- com.tamdan.repository: Spring Data repositories only
- com.tamdan.service: business logic and transactions
- com.tamdan.controller: HTTP endpoints only
- com.tamdan.dto.request: request records
- com.tamdan.dto.response: response records
- com.tamdan.mapper: entity-to-response mapping
- com.tamdan.security: authentication and identity resolution
- com.tamdan.exception: semantic exceptions
- com.tamdan.web: API response and route helpers

Define these future internal context records:

AuthenticatedParentContext:
- Long parentId
- Long familyId

AuthenticatedDeviceContext:
- Long deviceId
- Long childProfileId
- Long familyId

Define these future service interfaces/classes without implementing business behavior yet:

IdentityLookupService:
- AuthenticatedParentContext resolveParent(Long parentId)
- AuthenticatedDeviceContext resolveDevice(Long deviceId)
- boolean verifyChildOwnership(Long familyId, Long childId)
- boolean verifyActiveDevice(Long deviceId)

Add shared validation and error conventions:
- ResourceNotFoundException for missing or inaccessible resources
- BadRequestException for invalid input or invalid passwords
- ConflictException for duplicate resources
- ForbiddenException for authenticated but unauthorized access

Do not add generic base classes or speculative abstractions.
Do not modify unrelated modules.
```

### Verify

- Compile the project.
- Confirm package responsibilities.
- Confirm no business logic was added unnecessarily.
- Run:

```bash
mvn test
```

---

## Step 2 — Parent registration and account lifecycle

### Development prompt

```text
Implement Step 2 of PRD 4: parent account registration and account lifecycle.

Reuse the existing User, UserService, AccountResource, UserRepository, UserMapper, and current exception handling.

Add or complete these request records:

RegisterParentRequest:
- String login
- String email
- String password
- String firstName
- String lastName

UpdateParentRequest:
- String email
- String firstName
- String lastName

Add or complete these response records:

ParentAccountResponse:
- Long id
- String login
- String email
- Boolean activated
- Set<String> authorities

Add service methods:

UserService:
- ParentAccountResponse register(RegisterParentRequest request)
- ParentAccountResponse getCurrent(Long authenticatedParentId)
- ParentAccountResponse update(Long authenticatedParentId, UpdateParentRequest request)
- void activate(String activationKey)
- void requestActivationResend(String email)

Rules:
- Normalize login and email before lookup and storage.
- Duplicate login returns ConflictException and HTTP 409.
- Duplicate email returns ConflictException and HTTP 409.
- Passwords must be hashed.
- Never return password hashes.
- Never log passwords or activation tokens.
- Use database uniqueness constraints.
- Use constructor injection.
- Use transactional writes and read-only reads.

Logical flow:
1. Validate the request record.
2. Normalize login and email.
3. Check duplicate values.
4. Hash the password.
5. Create the parent account.
6. Persist the account.
7. Map it to ParentAccountResponse.
8. Return ApiResponse where required.

Keep existing API routes unless a route is missing.
```

### Verify

Test:

- Successful registration.
- Duplicate login returns `409`.
- Duplicate email returns `409`.
- Password is never returned.
- Login and email normalization.
- Account activation.
- Account update ownership.

Run:

```bash
mvn test
mvn verify
```

---

## Step 3 — Parent sessions, login, refresh, and logout

### Development prompt

```text
Implement Step 3 of PRD 4: secure parent session management.

Add the ParentSession entity:

Fields:
- Long id
- User parent
- String refreshTokenHash
- Instant issuedAt
- Instant expiresAt
- Instant revokedAt
- Long replacedBySessionId
- Instant createdAt

Use protected no-arg and private all-args constructors. Do not add entity setters.

Add ParentSessionRepository methods:

- Optional<ParentSession> findByRefreshTokenHashAndRevokedAtIsNull(String hash)
- List<ParentSession> findAllByParentIdAndRevokedAtIsNull(Long parentId)
- int revokeAllActiveSessions(Long parentId, Instant revokedAt)

Add request and response records:

LoginRequest:
- String login
- String password

RefreshRequest:
- String refreshToken

TokenResponse:
- String accessToken
- String refreshToken
- Instant accessTokenExpiresAt
- Instant refreshTokenExpiresAt

Add service methods:

AuthenticationService:
- TokenResponse login(LoginRequest request)
- TokenResponse refresh(RefreshRequest request)
- void logout(String refreshToken)
- void logoutAll(Long authenticatedParentId)

Rules:
- Incorrect passwords return BadRequestException and HTTP 400.
- Do not reveal whether a login exists during failed authentication.
- Store only refresh-token hashes.
- Rotate refresh tokens on every refresh.
- Revoke the previous session after successful rotation.
- Reject expired or revoked refresh tokens.
- Logout revokes one session.
- Logout-all revokes all active sessions.
- Never log raw tokens.

Logical flow:
1. Normalize the login.
2. Load the account.
3. Validate account status.
4. Validate the password.
5. Issue a short-lived access token.
6. Generate a refresh token.
7. Store only its hash.
8. Return TokenResponse.
```

### Verify

Test:

- Successful login.
- Incorrect password returns `400`.
- Refresh-token rotation.
- Previous refresh token is rejected.
- Logout.
- Logout-all.
- Expired session rejection.

Run:

```bash
mvn verify
```

---

## Step 4 — Family ownership boundary

### Development prompt

```text
Implement Step 4 of PRD 4: family ownership.

Add these entities:

Family:
- Long id
- Boolean active
- Instant createdAt
- Instant updatedAt

FamilyMember:
- Long id
- Family family
- User parent
- String role
- Boolean active
- Instant createdAt

Rules:
- A family represents an ownership boundary.
- A parent may access only families where they are an active member.
- Every child profile must belong to one family.
- Add foreign keys and uniqueness constraints.
- Keep entities free from public setters.

Add repositories:

FamilyRepository:
- Optional<Family> findByIdAndActiveTrue(Long id)

FamilyMemberRepository:
- Optional<FamilyMember> findByParentIdAndActiveTrue(Long parentId)
- boolean existsByFamilyIdAndParentIdAndActiveTrue(Long familyId, Long parentId)

Add service methods:

FamilyService:
- FamilyResponse createForParent(Long parentId)
- Family requireFamilyForParent(Long parentId)
- FamilyResponse getCurrent(Long parentId)

FamilyResponse:
- Long id
- Boolean active
- Instant createdAt

Logical flow:
1. Resolve the authenticated parent ID.
2. Load the active family membership.
3. Reject missing membership with ResourceNotFoundException or ForbiddenException.
4. Use the family ID for all child and device ownership checks.
5. Never accept a family ID from the client as a trusted owner identity.
```

### Verify

Test:

- A parent receives a family.
- A parent can resolve only their own family.
- Another family cannot be accessed.
- Inactive family membership is rejected.
- Database constraints work.

---

## Step 5 — Child profiles

### Development prompt

```text
Implement Step 5 of PRD 4: child profile management.

Add the ChildProfile entity:

Fields:
- Long id
- Family family
- String displayName
- Boolean active
- Instant createdAt
- Instant updatedAt

Add request records:

CreateChildRequest:
- String displayName

UpdateChildRequest:
- String displayName
- Boolean active

Add response record:

ChildProfileResponse:
- Long id
- String displayName
- Boolean active
- Instant createdAt
- Instant updatedAt

Add repository methods:

- List<ChildProfile> findAllByFamilyIdAndActiveTrue(Long familyId)
- Optional<ChildProfile> findByIdAndFamilyIdAndActiveTrue(Long childId, Long familyId)
- Optional<ChildProfile> findByIdAndFamilyId(Long childId, Long familyId)

Add service methods:

ChildProfileService:
- ChildProfileResponse create(Long parentId, CreateChildRequest request)
- List<ChildProfileResponse> list(Long parentId)
- ChildProfileResponse get(Long parentId, Long childId)
- ChildProfileResponse update(Long parentId, Long childId, UpdateChildRequest request)
- void deactivate(Long parentId, Long childId)

Rules:
- Always resolve the family from the authenticated parent.
- Never trust an owner ID from request data.
- A deactivated child cannot create new activity or pair new devices.
- Return response DTOs only.
- Keep controllers free of business logic.
```

### Verify

Test:

- Create child.
- List children.
- Update child.
- Deactivate child.
- Cross-family access fails.
- Deactivated child cannot be used for new operations.

---

## Step 6 — Pairing code generation

### Development prompt

```text
Implement Step 6 of PRD 4: pairing-code generation.

Add the PairingCode entity:

Fields:
- Long id
- Family family
- ChildProfile childProfile
- String codeHash
- Instant expiresAt
- Instant usedAt
- Integer failedAttempts
- Long createdByParentId
- Instant createdAt

Add PairingCodeRepository methods:

- Optional<PairingCode> findActiveForChild(Long childId, Instant now)
- Optional<PairingCode> findUsableByCodeHash(String codeHash, Instant now)
- int markUsed(Long id, Instant usedAt)
- int incrementFailedAttempts(Long id)

Add request and response records:

CreatePairingCodeRequest:
- Long childId

PairingCodeResponse:
- String code
- Instant expiresAt

Add service methods:

PairingService:
- PairingCodeResponse createPairingCode(Long parentId, Long childId)
- PairingCode requireUsableCode(String rawCode)
- String hashPairingCode(String rawCode)

Rules:
- Generate codes with a cryptographically secure random generator.
- Store only the hash.
- Use a short expiration.
- Bind the code to one family and one child.
- Reject inactive children.
- Apply generation rate limits.
- Return the raw code only once.
- Never log or audit the raw code.

Logical flow:
1. Resolve parent family.
2. Require the child within that family.
3. Generate a secure code.
4. Hash and persist it.
5. Return PairingCodeResponse.
6. Audit only that a code was generated.
```

### Verify

Test:

- Secure code generation.
- Hash is stored instead of raw code.
- Expiration.
- Child ownership.
- Inactive child rejection.
- Rate-limit behavior.
- No secret appears in logs.

---

## Step 7 — Device registration after pairing

### Development prompt

```text
Implement Step 7 of PRD 4: child-device registration.

Add these entities:

ChildDevice:
- Long id
- ChildProfile childProfile
- String deviceIdentifierHash
- String platform
- String appVersion
- String displayName
- String capabilitiesJson
- Boolean active
- Instant revokedAt
- Instant lastSeenAt
- Instant createdAt
- Instant updatedAt

DeviceCredential:
- Long id
- ChildDevice childDevice
- String credentialHash
- Instant issuedAt
- Instant expiresAt
- Instant revokedAt
- Long replacedByCredentialId

Add request records:

CompletePairingRequest:
- String pairingCode
- String deviceIdentifier
- String platform
- String appVersion
- String displayName
- Map<String,Object> capabilities

Add response records:

DeviceCredentialResponse:
- Long deviceId
- String credential
- Instant expiresAt
- ChildContextResponse child

ChildContextResponse:
- Long childId
- String displayName
- Long familyId

DeviceResponse:
- Long id
- String displayName
- String platform
- String appVersion
- Boolean active
- Instant lastSeenAt

Add service methods:

PairingService:
- DeviceCredentialResponse completePairing(CompletePairingRequest request)

DeviceService:
- List<DeviceResponse> list(Long parentId, Long childId)
- DeviceResponse get(Long parentId, Long deviceId)
- DeviceResponse rename(Long parentId, Long deviceId, String name)

Rules:
- Pairing completion must be transactional.
- A pairing code can be used exactly once.
- Device credentials are separate from parent JWTs.
- Store only credential hashes.
- Return the raw device credential only once.
- Return minimum child context.
- Do not expose entities directly.
```

### Verify

Test:

- Successful pairing.
- Pairing replay rejection.
- Expired code rejection.
- Device creation.
- Raw credential is not stored.
- Parent JWT is not returned as the device credential.
- Cross-family pairing rejection.
- Transaction rollback.

---

## Step 8 — Device authentication, rotation, and revocation

### Development prompt

```text
Implement Step 8 of PRD 4: device authentication and credential lifecycle.

Add DeviceCredentialRepository methods:

- Optional<DeviceCredential> findActiveByCredentialHash(String hash, Instant now)
- Optional<DeviceCredential> findCurrentByDeviceId(Long deviceId)
- int revokeByDeviceId(Long deviceId, Instant revokedAt)

Add AuthenticatedDeviceIdResolver:

- Long requireAuthenticatedDeviceId()
- AuthenticatedDeviceContext requireContext()

Add DeviceCredentialService methods:

- AuthenticatedDeviceContext authenticate(String rawCredential)
- DeviceCredentialResponse rotateCredential(Long deviceId)
- void revokeCredential(Long deviceId)
- void validateActiveDevice(Long deviceId)

Add a device authentication filter:

- Read the device credential from the agreed request header.
- Hash the credential.
- Load an active, unexpired, non-revoked credential.
- Build a device-specific SecurityContext.
- Reject malformed, expired, or revoked credentials.
- Never log raw credentials.

Add DeviceService methods:

- void revokeDevice(Long parentId, Long deviceId)
- DeviceResponse rename(Long parentId, Long deviceId, RenameDeviceRequest request)

Rules:
- Parent JWT and device credential authentication remain separate.
- Revocation must affect the next protected request.
- Rotating a credential invalidates the previous credential.
- Revoked devices cannot upload events or retrieve policies.
- Device ownership must be checked with family-aware queries.
```

### Verify

Test:

- Valid device authentication.
- Invalid credential.
- Expired credential.
- Revoked credential.
- Credential rotation.
- Previous credential rejection.
- Parent JWT rejected by device-only endpoint.
- Cross-family device access rejection.
- Revoked device rejection after logout/revocation.

---

## Step 9 — Consent and security audit

### Development prompt

```text
Implement Step 9 of PRD 4: consent records and security audit history.

Add ConsentRecord:

Fields:
- Long id
- Family family
- ChildProfile childProfile
- String documentVersion
- String actorType
- Long actorId
- Instant acknowledgedAt
- String permissionState
- Instant createdAt

Add SecurityAuditEvent:

Fields:
- Long id
- String actorType
- Long actorId
- Long familyId
- String action
- String resourceType
- Long resourceId
- String result
- String requestId
- String ipAddressHash
- String userAgentHash
- String safeMetadataJson
- Instant occurredAt

Add request records:

ConsentRequest:
- String documentVersion
- String permissionState

DevicePermissionRequest:
- String permissionState

Add response records:

ConsentResponse:
- Long id
- Long childId
- String documentVersion
- String actorType
- Instant acknowledgedAt
- String permissionState

AuditResponse:
- Long id
- String actorType
- String action
- String resourceType
- String result
- Instant occurredAt

Add services:

ConsentService:
- ConsentResponse acknowledge(Long parentId, Long childId, ConsentRequest request)
- List<ConsentResponse> history(Long parentId, Long childId)
- ConsentResponse recordDevicePermission(Long deviceId, DevicePermissionRequest request)

AuditService:
- void record(AuditCommand command)
- Page<AuditResponse> searchAuthorized(AuditSearchRequest request)

Audit these actions:
- registration
- login
- logout
- recovery
- pairing-code creation
- pairing completion
- credential rotation
- device revocation
- consent changes
- child deactivation

Never record passwords, tokens, pairing codes, credentials, or sensitive request bodies.
```

### Verify

Test:

- Consent acknowledgement.
- Consent history after document version changes.
- Device permission update.
- Audit success and failure events.
- Audit access authorization.
- No secret values in audit records.
- Family isolation for audit queries.

---

## Step 10 — Internal contract, rate limits, and final acceptance

### Development prompt

```text
Implement Step 10 of PRD 4: internal identity contract and final hardening.

Create the internal identity contract used by Safety and Policy modules.

IdentityLookupService:
- AuthenticatedParentContext resolveParent(Long parentId)
- AuthenticatedDeviceContext resolveDevice(Long deviceId)
- boolean verifyChildOwnership(Long familyId, Long childId)
- boolean verifyActiveDevice(Long deviceId)

Add DeviceRevocationEvent:

- Long deviceId
- Long childProfileId
- Long familyId
- Instant revokedAt
- String reason

Add DeviceRevocationListener:

- void onDeviceRevoked(DeviceRevocationEvent event)

Contract rules:
- Do not expose password hashes.
- Do not expose refresh tokens.
- Do not expose raw device credentials.
- Do not expose unnecessary child data.
- Return only trusted IDs, family IDs, child IDs, active status, and revocation state.

Complete security hardening:
- Add rate limits for registration, login, recovery, pairing-code generation, pairing submission, and refresh.
- Confirm spring.jpa.open-in-view=false.
- Confirm secrets are configuration-based.
- Confirm all migrations are versioned.
- Confirm liveness and readiness endpoints.
- Confirm stable error codes.
- Confirm no sensitive values are logged.
- Confirm ownership checks exist on every protected resource.

Add full acceptance tests:

1. Parent registration.
2. Parent verification.
3. Login.
4. Refresh-token rotation.
5. Logout.
6. Account recovery.
7. Family isolation.
8. Child-profile ownership.
9. Pairing-code expiration.
10. Pairing-code replay rejection.
11. Successful device registration.
12. Device credential rotation.
13. Device revocation.
14. Revoked device rejection.
15. Consent history.
16. Audit history.
17. Rate limits.
18. Database migration.
19. Parent/device authentication separation.
20. Internal identity contract.

Run:

mvn spotless:check
mvn test
mvn verify

Do not modify unrelated modules. Report changed files, endpoints, migrations, security behavior, and test totals.
```

### Verify

The final step is complete only when:

```text
mvn spotless:check  -> passed
mvn test            -> passed
mvn verify          -> passed
```

The ten-step dependency flow is:

```text
1. Contracts
   ↓
2. Parent accounts
   ↓
3. Parent sessions
   ↓
4. Family ownership
   ↓
5. Child profiles
   ↓
6. Pairing codes
   ↓
7. Device registration
   ↓
8. Device authentication
   ↓
9. Consent and audit
   ↓
10. Internal contract and final acceptance
```

This order keeps each change small, testable, and easy to review.
