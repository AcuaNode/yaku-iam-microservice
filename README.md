# yaku-iam-microservice

IAM (Identity and Access Management) microservice for the Yaku platform. Handles user registration, authentication, JWT token management, and role-based access control.

## Overview

This service provides:
- User signup and signin with JWT Bearer token authentication
- Role-based access control (ADMIN, OPERATOR)
- Async farm-token validation via Kafka (integrates with Equipment microservice)
- OpenAPI/Swagger documentation

## Tech Stack

- Java 17
- Spring Boot 3.4.3
- Spring Security (JWT)
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Maven

## Prerequisites

- Java 17
- Docker & Docker Compose (for local Postgres and Kafka)
- Maven (or use the provided wrapper: `./mvnw`)

## Quick Start

1. Start infrastructure services:
```bash
docker compose up -d postgres
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8082`.

## API Documentation

Swagger UI: `http://localhost:8082/swagger-ui.html`

### Public Endpoints (no authentication)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users/signup` | Register a new user |
| POST | `/api/v1/users/signin` | Authenticate and receive JWT token |
| GET | `/api/v1/users/available-roles` | Get available registration roles |

### Authenticated Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | Get all users (optional `farmId` filter) |
| GET | `/api/v1/users/by-username` | Get user by username |

## Configuration

Key properties in `application.properties`:

```properties
server.port=8082
spring.profiles.active=dev
authorization.jwt.secret=<your-secret>
authorization.jwt.expiration.days=7
```

Dev profile datasource (`application-dev.properties`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/yaku_iam
spring.datasource.username=root
spring.datasource.password=password
spring.kafka.bootstrap-servers=localhost:9094
```

## Architecture

DDD/CQRS-style layering:

- `application/internal` — command & query handlers
- `domain` — aggregates, value objects, events
- `infrastructure` — JPA repos, security config, Kafka publishers
- `interfaces` — REST controllers
- `shared` — common utilities, OpenAPI config

## Testing

```bash
# Run all tests (requires Postgres running on localhost:5434)
./mvnw test

# Run a single test class
./mvnw test -Dtest=YakubackendApplicationTests
```

## Key Design Decisions

See `DECISIONS.md` for architecture decision records, including the farm-token validation strategy (ADR-001).

## Integration with Equipment Microservice

See `EQUIPMENT.md` for the integration specification with the Equipment microservice.
