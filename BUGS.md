# Known Issues — iam-service

## Historical Note

The synchronous `EquipmentExternalService` coupling that previously blocked IAM startup has been resolved. See `DECISIONS.md` (ADR-001) for the full architectural decision record on farm-token validation, the options considered, and why async Kafka-based validation was chosen.