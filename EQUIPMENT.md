# Equipment Microservice Integration — IAM

## Current Situation

The IAM (Identity and Access Management) microservice has removed its synchronous dependency on the Equipment service. Previously, IAM called `EquipmentExternalService` directly during user signup to validate farm tokens. This created tight coupling: a missing Equipment implementation blocked IAM from starting.

**This coupling is now removed.** IAM publishes a `UserRegisteredEvent` to Kafka, and Equipment handles farm-token validation and user-to-farm linking asynchronously.

## What the Frontend Team is Implementing (for context)

The frontend will call your validation endpoint **before** submitting the signup form to IAM:

```
GET /equipment/farm-tokens/validate?token=X
```

This is purely for UX — so the user gets immediate feedback if their farm token is invalid. However, the frontend validation is not authoritative. IAM still publishes the event to Kafka, and your Kafka consumer must still validate the token when processing the event.

## What You Need to Implement

### 1. Validation Endpoint (for Frontend UX)

Expose an endpoint that the frontend calls before signup:

```
GET /equipment/farm-tokens/validate?token=X
```

**Expected responses:**
- `200 OK` — token is valid and unused
- `404 Not Found` — token does not exist
- `400 Bad Request` — token is invalid or already used

**Important:** This endpoint is read-only. It must not consume/modify the token. The actual token consumption happens in the Kafka consumer.

### 2. Kafka Consumer

Consume events from the Kafka topic **`iam.user-registered`**.

**Event payload (JSON):**
```json
{
  "userId": 123,
  "username": "john.doe",
  "email": "john@example.com",
  "farmToken": "abc-def-123"
}
```

**What to do when you receive the event:**
1. Validate the `farmToken` (check if it exists and is unused)
2. Link the user (identified by `userId`) to the farm associated with the token
3. Mark the token as used
4. Handle the case where the token is invalid or already used:
   - Option A: Publish a callback event to a topic that IAM consumes (e.g., `equipment.farm-assignment-failed`)
   - Option B: Send a notification/email to the user
   - Option C: Mark the user's account as "pending farm verification" in your own database

**Race condition handling:** Two users might validate the same token simultaneously (frontend race). Your Kafka consumer must handle this gracefully — if the token is already used by the time you process the second event, handle it as an invalid token.

### 3. Kafka Configuration

**Bootstrap servers:** `localhost:9094` (dev environment)

**Topic:** `iam.user-registered`

**Key:** userId (String)
**Value:** JSON event payload (String)

## Open Questions / TODOs

### For the Equipment Team

1. **Invalid token handling:** How should the system notify the user (and IAM) if the farm token provided during signup is invalid or already used?
   - Do you want to publish a callback event to a Kafka topic that IAM listens to?
   - Or will you handle user notification independently (email, push notification)?

2. **Token reservation:** Does the `GET /equipment/farm-tokens/validate` endpoint need to reserve the token temporarily (e.g., mark as "pending") to prevent race conditions? If so, how long should the reservation last, and what happens if the user never completes signup?

3. **User-to-farm link query:** The IAM service currently exposes `GET /api/v1/users?farmId=X`. Should this query move to the Equipment service in the future, or is it acceptable for IAM to store `assignedFarmId` on the User entity?

## Contact

For questions about the Kafka event schema or IAM API, contact the IAM team or check the `DECISIONS.md` file in the IAM repository.
