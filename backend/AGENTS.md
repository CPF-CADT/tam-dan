# API instructions

Follow the Spring Boot rules below. Keep changes simple and focused.

## Architecture

- Organize code by technical layer under the root package: `controller`, `service`, `repository`, `dto/request`, `dto/response`, `mapper`, `entities`, `config`, `security`, `exception`, and `web`.
- Do not add feature-level parent packages unless the application becomes large enough to need clear module boundaries.
- Keep JPA entities and persistence enums in `entities`.
- Keep application-wide configuration, security, exceptions, and web helpers in their corresponding root-level packages.
- Controllers handle HTTP concerns, validation, authentication context, mapping, and responses.
- Services own business rules, normalization, orchestration, and transaction boundaries.
- Repositories contain persistence queries only.
- Entities own cohesive state changes through named methods.

## Java conventions

- Use constructor injection with `private final` fields and Lombok `@RequiredArgsConstructor`.
- Use Java records for request, response, and internal immutable data carriers.
- Use `@Getter`, `@Builder`, a protected no-arg constructor, and a private all-arg constructor for entities.
- Do not use entity setters or Lombok `@Data`.
- Introduce interfaces only for real interchangeable implementations; do not create an interface for every service.
- Prefer clear methods such as `require...`, `ensure...`, `normalize...`, and `toResponse`.

## API and validation

- Keep request and response DTOs separate. Never expose JPA entities directly.
- Validate request records with Jakarta constraints and controller `@Valid`.
- Use explicit, client-readable validation messages.
- Keep database-dependent and business validation in services.
- Map entities to response DTOs explicitly with a mapper when mapping is reused or hides internal fields.
- Wrap responses with `ApiResponse`; use `PageResponse` for pagination.
- Use `ResponseEntity` only when setting a non-default status or headers.
- Store reused routes in a focused, non-instantiable `*ApiPaths` class.

## Services and persistence

- Put `@Transactional` on public write use cases and `@Transactional(readOnly = true)` on pure reads.
- Normalize identifiers such as usernames, emails, and slugs before lookup and storage.
- Load required data through private `require...` helpers and throw semantic exceptions.
- Enforce resource ownership in repository queries using the trusted authenticated user ID.
- Back uniqueness checks with database constraints. Translate known `DataIntegrityViolationException` races to `ConflictException`.
- Prefer derived Spring Data queries. Use explicit JPQL, locks, or bulk updates only when required.
- Rely on JPA dirty checking for managed entity updates unless an immediate flush is needed.
- Use unusual propagation or rollback rules only for a proven security or consistency requirement.

## Security and errors

- Resolve the authenticated database ID through `AuthenticatedUserIdResolver`; never trust an owner ID from request data.
- Apply role checks with method security for restricted use cases.
- Throw `ResourceNotFoundException`, `BadRequestException`, `ConflictException`, or `ForbiddenException` as appropriate.
- Let `GlobalExceptionHandler` produce the HTTP error response.
- Never log passwords, production tokens, secrets, or sensitive request bodies.

## Configuration and database

- Use validated `@ConfigurationProperties` classes instead of scattered `@Value` fields.
- Keep secrets and environment-specific values outside source code.
- Do not treat `docs/sql/stage-1-schema.sql` as an applied migration history.
- Add a reviewed, versioned migration for production schema changes.
- Keep `spring.jpa.open-in-view=false`; load required data inside the service transaction.

## Testing and verification

- Use JUnit 5, AssertJ, and Mockito for focused unit tests.
- Name tests by behavior, such as `rejectsDuplicateCode`.
- Add integration tests when correctness depends on database constraints, JPQL, locks, transactions, or the security filter chain.
- Run `mvn test` after Java changes.
- Do not modify unrelated files or introduce generic base controllers, services, repositories, or speculative abstractions.

## Tam Dan backend index

### Source and test locations

- Java source: `src/main/java/com/tamdan`
- Tests: `src/test/java`
- Configuration: `src/main/resources/config`
- Liquibase: `src/main/resources/config/liquibase`

### Package responsibilities

- `entities`: JPA entities and persistence enums; no public setters.
- `repository`: Spring Data repositories and ownership-aware queries only.
- `service`: business logic, orchestration, and transaction boundaries.
- `controller`: HTTP endpoints, validation, authentication context, and DTO responses only.
- `dto/request`: request records.
- `dto/response`: response records and `ApiResponse` wrappers.
- `mapper`: reusable entity-to-response mapping.
- `security`: JWT authentication, device authentication, identity contexts, and resolvers.
- `exception`: semantic application exceptions.
- `exception/web`: RFC 7807-compatible error translation and stable error codes.
- `web`: route and HTTP response helpers.

### PRD 4 domain map

- Parent account and session lifecycle: `UserService`, `AuthenticationService`, `AccountResource`, `AuthenticateController`.
- Family ownership: `Family`, `FamilyMember`, `FamilyService`, `FamilyResource`.
- Child profiles: `ChildProfile`, `ChildProfileService`, `ChildProfileResource`.
- Pairing: `PairingCode`, `PairingService`, `PairingResource`, `DevicePairingResource`.
- Devices and credentials: `ChildDevice`, `DeviceCredential`, `DeviceService`, `DeviceCredentialService`, `DeviceResource`, `DeviceCredentialResource`.
- Consent and audit: `ConsentService`, `AuditService`, `ConsentResource`, `AuditResource`.
- Internal identity contract: `IdentityLookupService`, `IdentityLookupServiceImpl`, `AuthenticatedParentContext`, `AuthenticatedDeviceContext`.
- Rate limiting: `RateLimitService`, `RateLimitProvider`, `DatabaseRateLimitProvider`, `RateLimitBucket`.

### Security invariants

- Resolve parent and device identity only from authenticated security context.
- Parent JWTs and device credentials are separate authentication mechanisms.
- Enforce family ownership on every protected parent resource.
- Store only hashes for passwords, refresh tokens, pairing codes, and device credentials.
- Return raw pairing codes and device credentials only once.
- Never log passwords, tokens, pairing codes, credentials, authentication headers, or sensitive request bodies.
- Revoke old credentials before replacements become usable.
- Reject expired, revoked, inactive, or child-inactive devices.
- Keep inactive and revoked records queryable for audit/history.

### Liquibase migrations

- Master file: `src/main/resources/config/liquibase/master.xml`
- Initial schema: `00000000000000_initial_schema.xml`
- PRD 4 migrations: `20261021000100` through `20261021000900`.
- Add every schema change as a new versioned migration and include it exactly once.
- Preserve foreign keys, uniqueness constraints, ownership indexes, and rollback definitions.
- Hibernate schema generation is disabled with `ddl-auto: none`.

### Runtime configuration

- Local development uses PostgreSQL configured in `application-dev.yml`.
- `application-local.yml` is Git-ignored and is for local secrets only.
- Docker Compose is not required for local development.
- `spring.jpa.open-in-view=false` must remain enabled.
- Liveness: `/management/health/liveness`.
- Readiness: `/management/health/readiness`.

### Verification commands

Run from this directory:

```bash
./mvnw spotless:check
./mvnw test
./mvnw verify
./mvnw liquibase:validate
```

Use the existing Testcontainers, MockMvc, Liquibase, and architecture-test conventions. If PostgreSQL, Testcontainers, or the local JVM prevents a command from running, report the exact environment failure rather than weakening production code or tests.
