# Tam Dan Backend

Spring Boot REST API for Tam Dan.

## Technology

- Java 21
- Spring Boot 4
- Spring Security with JWT
- PostgreSQL
- Liquibase
- Maven

## Development

The default Spring profile is `dev`.

Start the application:

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

Spring Boot DevTools is enabled for development. When your IDE recompiles a saved Java file, the application restarts automatically.

Swagger UI is available in the `dev` profile at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at `http://localhost:8080/v3/api-docs`. Use the Swagger **Authorize** button to enter a JWT for protected endpoints.

## Select a profile

Use an environment variable:

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Or use a Maven option:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

For production:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/tamdan-*.jar
```

## Optional demo data

Demo data is development-only and disabled by default. Activate it explicitly with:

```bash
SPRING_PROFILES_ACTIVE=dev TAMDAN_DEMO_DATA_ENABLED=true ./mvnw spring-boot:run
```

The fixture is deterministic and safe to run repeatedly. The local-only parent login is `demo.parent@example.test` with password `TamDan-Demo-2026!`. Credentials are never written to logs. The `prod` profile never creates the fixture, even if `TAMDAN_DEMO_DATA_ENABLED=true` is present.

## Database

Development uses PostgreSQL running on the local machine at `localhost:5432`.
The expected database is `tamdan`, with username `tamdan` and an empty password.

## Test

```bash
./mvnw test
```

## Build

```bash
./mvnw clean package
```
