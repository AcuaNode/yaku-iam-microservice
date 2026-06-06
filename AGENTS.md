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
- **Kafka:** `spring-kafka` is on the classpath. `KafkaProducerConfig` and `KafkaDomainEventPublisher` are wired. `UserRegisteredEvent` is published to topic `iam.user-registered`.
- **CORS:** Explicitly configured for `http://localhost:4200` only.

## Testing Quirks

- Only one test exists: `YakubackendApplicationTests` (context-loads smoke test).
- `@ActiveProfiles("test")` is set on the test class, so it uses `application-test.properties` with H2 in-memory DB instead of Postgres.
- **H2 is on the test classpath** (`com.h2database:h2` scope=test), and `application-test.properties` overrides the datasource to `jdbc:h2:mem:testdb;MODE=PostgreSQL`.
- Kafka auto-configuration is excluded in tests (`spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration`).

## Architecture

DDD/CQRS-style layering under `io.github.rafaviv.yakubackend.iam`:

- `application/internal` — command & query handlers, domain services impl.
- `domain` — aggregates (`User`), commands, queries, valueobjects, entities (`Role`), events, exceptions.
- `infrastructure` — JPA repos, Spring Security config, JWT filter pipeline (`BearerAuthorizationRequestFilter`), BCrypt hashing, Kafka event publishing.
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

## Farm-Token Validation (Option 4 — Pre-Validation + Async)

The team selected **Option 4** (see `DECISIONS.md`):

- **Frontend** calls Equipment's `GET /equipment/farm-tokens/validate?token=X` **before** signup for immediate UX feedback.
- **IAM** publishes `UserRegisteredEvent` (with `farmToken`) to Kafka topic `iam.user-registered`.
- **Equipment** consumes the event to validate the token and link the user to the farm asynchronously.
- IAM only enforces the **structural** rule: OPERATORs must provide a `farmToken` in the request (null/blank check). It does not validate token validity.

IAM backend is complete and ready. Equipment team needs to implement the validation endpoint and Kafka consumer (see `EQUIPMENT.md`).

## Notable Dependencies

Recent POM additions worth knowing about:
- `spring-boot-starter-validation`
- `spring-boot-devtools`
- `spring-kafka`
- `com.h2database:h2` (test scope)
- `io.github.encryptorcode:pluralize`
