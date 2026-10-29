# Real Runtime Verification Plan

## Objective

Verify Tam Dan without Docker. The developer runtime is:

- Native PostgreSQL on `localhost:5432`.
- Spring Boot running against that PostgreSQL instance.
- Flutter running on an Android emulator or physical device.
- Android monitoring and secure-storage channels running inside the app.

Docker/Testcontainers is optional CI infrastructure, not the primary local verification path.

## 1. Native PostgreSQL

Install PostgreSQL with the host operating system package manager or official installer, then start the service:

```bash
pg_isready -h localhost -p 5432
psql -h localhost -p 5432 -U postgres -d postgres -c 'select version();'
```

Using an administrator account, create the application role and database:

```sql
CREATE ROLE tamdan LOGIN PASSWORD 'tamdan';
CREATE DATABASE tamdan OWNER tamdan;
```

If they already exist, update the password and verify permissions instead of recreating them.

Verify the application account:

```bash
psql -h localhost -p 5432 -U tamdan -d tamdan -c 'select current_user, current_database();'
```

## 2. Backend runtime

From `backend/`:

```bash
./mvnw spotless:check
./mvnw test
./mvnw liquibase:validate
./mvnw spring-boot:run
```

Confirm register/login/refresh/logout, pairing ownership, cross-family rejection, invalid-input rejection, idempotent ingestion, and freshness-aware reports against the real database.

If tests are hard-coded to Testcontainers, change only the test profile to use local PostgreSQL. Do not weaken assertions.

## 3. Android runtime

```bash
adb kill-server
adb start-server
adb devices -l
```

The target must show state `device`.

Then run:

```bash
cd mobile
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
flutter test integration_test
```

Verify secure-storage write/read/delete, app restart, logout, pairing, revocation, permissions, reboot, network recovery, policy delivery, and truthful unsupported enforcement.

## 4. Evidence and final gate

Record command, exit code, commit, environment, and log path in `09-current-implementation-knowledge-base.md`.

Run dependency, secret, and OWASP ownership/injection checks. Request Integration Review only after native PostgreSQL and Android runtime evidence exists. Merge and deployment remain blocked until `APPROVED`.
