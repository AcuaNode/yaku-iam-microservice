# Architecture Decisions — iam-service

## ADR-001: Farm-Token Validation Strategy for Microservices

### Status

**Option 4 selected** — IAM backend is ready. Pending Equipment microservice team implementation of the validation endpoint.

### Context

The IAM service originally validated farm tokens synchronously via `EquipmentExternalService` — a direct call from signup to what is now a separate Equipment microservice. This created tight coupling: a broken or missing Equipment service implementation blocked IAM startup entirely (`NoSuchBeanDefinitionException`).

This was documented in `BUGS.md` as a startup blocker (see historical note in that file). The root cause was that `EquipmentExternalService` was an unimplemented interface with no bean in the Spring context, yet `UserCommandServiceImpl` autowired it as a mandatory constructor dependency.

### Problem Statement

In a monolith, calling an internal service directly is fine. In microservices:
- A missing dependency in Service B should not prevent Service A from starting
- Synchronous HTTP calls during signup add latency and availability risk (if Equipment is down, nobody can sign up)
- Domain boundaries blur when IAM owns both identity and farm-token business rules

### Options Considered

#### Option 1 — Full decoupling
Remove all farm concepts from IAM (`assignedFarmId`, `farmToken`, `GetUsersByFarmIdQuery`, the `GET /users?farmId` endpoint). Equipment service fully owns the user-to-farm relationship.

- **Pros:** Cleanest microservices boundaries. IAM = identity only. Equipment = farms + assignments.
- **Cons:** Requires frontend changes (remove farmToken from signup form, move farm user queries to Equipment). `GET /users?farmId` must be reimplemented in Equipment or a BFF.

#### Option 2 — Keep assignedFarmId, add REST client
Replace `EquipmentExternalService` with a synchronous HTTP client that calls the Equipment service's API.

- **Pros:** Immediate token validation feedback during signup. Straightforward to implement if Equipment already has a validation endpoint.
- **Cons:** Replaces one coupling with another. Signup latency and availability now depend on Equipment service. Defeats the purpose of microservices autonomy.

#### Option 2b — Remove sync coupling, keep the data model (implemented as stepping stone)
Delete `EquipmentExternalService`. IAM still accepts `farmToken` at signup and enforces that OPERATORs must provide one (structural validation — "a token must exist"). Actual token validity becomes Equipment's responsibility via an async Kafka event.

- **Pros:** Fixes the startup blocker immediately. Removes synchronous cross-service coupling. Preserves API surface and DB schema. Leverages Kafka infrastructure already in place.
- **Cons:** Signup no longer rejects invalid tokens synchronously. Frontend must handle the fact that farm access is validated asynchronously (may need status tracking, notifications, or degraded UX).

#### Option 4 — Pre-validation endpoint (selected)
Equipment exposes `GET /equipment/farm-tokens/validate?token=X`. Frontend calls this before signup. IAM still publishes `UserRegisteredEvent` to Kafka for Equipment to actually link the farm.

- **Pros:** Best UX — immediate feedback on invalid tokens. Keeps async decoupling for the actual farm-linking side effect. Smaller scope than Option 1.
- **Cons:** Requires one new Equipment endpoint and one extra frontend call before form submission.

### Decision

**Option 4.**

The team evaluated all options and selected Option 4 because:
1. It provides the best user experience (immediate token validation feedback)
2. It preserves the async Kafka decoupling for the actual farm-linking side effect
3. It has smaller scope than Option 1 (full decoupling)
4. It avoids the synchronous coupling problems of Option 2

Option 2b was implemented as an immediate stepping stone to remove the startup blocker. The IAM backend is already compatible with Option 4 — no further changes are needed in IAM.

### IAM Implementation (Complete)

- `EquipmentExternalService` interface deleted.
- `UserCommandServiceImpl` no longer holds a reference to `EquipmentExternalService`.
- `ApplicationEventPublisher` replaced with `KafkaDomainEventPublisher` for `UserRegisteredEvent` publication.
- The null/blank `farmToken` check for OPERATOR role is retained (structural validation — IAM enforces "a token must be provided", not "this token is valid").
- `UserRegisteredEvent` (including `farmToken`) is published to Kafka topic `iam.user-registered`.

### Equipment Team Responsibilities

1. **Expose validation endpoint:** `GET /equipment/farm-tokens/validate?token=X`
   - Returns `200 OK` if token is valid and unused
   - Returns `404 Not Found` or `400 Bad Request` if token is invalid or already used
   - This endpoint is called by the frontend before signup (UX only)

2. **Consume `UserRegisteredEvent` from Kafka topic `iam.user-registered`:**
   - Validate the `farmToken` from the event
   - Link the user (by `userId`) to the farm associated with the token
   - Handle "token already used" race conditions (two users validating same token simultaneously)
   - Optionally: publish a callback event or notification if token is invalid post-signup

See `EQUIPMENT.md` for the full integration specification.

### Consequences

- IAM signup no longer validates farm tokens synchronously.
- The frontend will call Equipment's validation endpoint before submitting signup to IAM.
- The `GET /api/v1/users?farmId=X` endpoint remains in IAM for now.
- `assignedFarmId` on the User entity stays but is not populated during signup. Equipment will handle assignment asynchronously via Kafka.
- Kafka topic: `iam.user-registered`.

### Alternatives Not Chosen

- **Option 1 (full decoupling):** Cleaner microservices boundaries, but requires frontend changes and moving the farm-id user query to Equipment. Deferred due to scope.
- **Option 2 (REST client):** Introduces synchronous HTTP coupling and latency into the signup flow, which defeats the purpose of microservices autonomy.
- **Option 2b (async only):** Implemented as stepping stone, but lacks immediate UX feedback. Upgraded to Option 4.

### Next Steps

- Equipment team implements the validation endpoint and Kafka consumer (see `EQUIPMENT.md`).
- Frontend team adds the pre-validation call before signup form submission.
- IAM team is ready — no further backend changes required.
