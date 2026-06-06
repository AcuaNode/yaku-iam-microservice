# Known Issues — iam-service

## Startup

- **`EquipmentExternalService` has no implementation.** `UserCommandServiceImpl` autowires it, so the Spring context fails with `NoSuchBeanDefinitionException` on every startup and test run. Either provide a bean (e.g. a REST client adapter or a no-op stub) or remove the dependency from the command service.

## Documentation

- `README.md` is still a placeholder string (`"# yaku-iam-microservice"`) with no setup or usage instructions.
