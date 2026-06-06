# AGENTS.md — iam-service

High-signal notes for OpenCode sessions working in this repo.

## Build & Run

- **Tooling:** Maven wrapper (`./mvnw`), Java 17, Spring Boot 3.4.3.
- **Entrypoint:** `io.github.rafaviv.yakubackend.YakubackendApplication`
- **Common commands:**
  - Compile: `./mvnw compile -q`
  - Run app: `./mvnw spring-boot:run`
  - Run tests: `./mvnw test`
  - Single test: `./mvnw test -Dtest=YakubackendApplicationTests`

## Local Dev Environment

- **Active profile:** `dev` is hardcoded in `application.properties` (`spring.profiles.active=dev`).
- **Database:** Postgres on `localhost:5434`, db `yaku_iam`, user `root`, password `password`.
  - Start it: `docker compose up -d postgres`
  - `compose.yaml` is at repo root.
- **Server port:** `8082`
- **Kafka:** `spring-kafka` is now on the classpath and `compose.yaml` defines a broker on `localhost:9094`, but there is **no producer/consumer code** yet.
- **CORS:** Explicitly configured for `http://localhost:4200` only.

## Testing Quirks

- Only one test exists: `YakubackendApplicationTests` (context-loads smoke test).
- It boots the full Spring context with the `dev` profile active.
- **H2 is on the test classpath** (`com.h2database:h2` scope=test), but there is **no `application-test.properties`** override, so the datasource still points to Postgres unless you add one.
- The app currently fails to start in tests because `EquipmentExternalService` has no bean implementation (see `BUGS.md`). Once that is resolved, Postgres must be running on `localhost:5434` for the test to pass.

## Architecture

DDD/CQRS-style layering under `io.github.rafaviv.yakubackend.iam`:

- `application/internal` — command & query handlers, domain services impl.
- `domain` — aggregates (`User`), commands, queries, valueobjects, entities (`Role`), events, exceptions.
- `infrastructure` — JPA repos, Spring Security config, JWT filter pipeline (`BearerAuthorizationRequestFilter`), BCrypt hashing, external service interfaces.
- `interfaces` — REST controllers. `UsersController` is the main surface at `/api/v1/users`.
- `shared` — OpenAPI config (`/swagger-ui.html`), global exception handler, JPA physical naming strategy (`SnakeCaseWithPluralizedTablePhysicalNamingStrategy`).

On startup, `ApplicationReadyEventHandler` seeds the `Role` table with `ADMIN` and `OPERATOR` if they are missing.

## API & Security

- JWT Bearer token authentication.
- Public endpoints (no auth):
  - `POST /api/v1/users/signup`
  - `POST /api/v1/users/signin`
  - `GET /api/v1/users/available-roles`
  - Swagger/OpenAPI docs (`/swagger-ui.html`, `/v3/api-docs/**`)
- All other requests require a valid JWT.

## Notable Dependencies

Recent POM additions worth knowing about:
- `spring-boot-starter-validation`
- `spring-boot-devtools`
- `spring-kafka`
- `com.h2database:h2` (test scope)
- `io.github.encryptorcode:pluralize`
