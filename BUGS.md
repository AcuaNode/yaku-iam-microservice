# Known Issues — iam-service

## Documentation

- `README.md` is still a placeholder string (`"# yaku-iam-microservice"`) with no setup or usage instructions.

## Historical Note

The synchronous `EquipmentExternalService` coupling that previously blocked IAM startup has been resolved. See `DECISIONS.md` (ADR-001) for the full architectural decision record on farm-token validation, the options considered, and why async Kafka-based validation was chosen.