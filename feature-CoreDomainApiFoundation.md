# Branch: feature/CoreDomainApiFoundation

## Origin
- Branched from: `feature/AppInitialSetup`
- Repository: `SpendSmart-SpringBootMicroservices`

## Scope
- Build the backend core domain and REST API foundation for SpendSmart services as defined in the case-study.
- Establish entities, enums, repositories, service interfaces/implementations, and REST resources for the first implementation pass.

## Progress
- Branch created from `feature/AppInitialSetup`.
- Requirement extraction completed from case-study PDF.
- Implemented layered backend foundation for:
  - `auth-service`
  - `expense-service`
  - `income-service`
  - `category-service`
  - `budget-service`
  - `analytics-service`
  - `recurring-service`
  - `notification-service`
- Added entities, enums, repositories, service interfaces, service implementations, DTOs, and REST resources aligned to the class-diagram contracts.
- Added per-service H2 datasource and service ports:
  - Auth `8081`
  - Expense `8082`
  - Income `8083`
  - Category `8084`
  - Budget `8085`
  - Analytics `8086`
  - Recurring `8087`
  - Notification `8088`
- Upgraded service module dependencies to Spring Boot `3.5.5` and added required Spring Web/JPA/Validation dependencies.
- Enabled recurring scheduler execution with `@EnableScheduling`.
- Fixed analytics schema naming (`snapshot_year`, `snapshot_month`) to avoid SQL reserved keyword collisions.
- Verification completed:
  - `./mvnw -pl auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service -DskipTests compile`
  - `./mvnw -pl auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service test`
  - `./mvnw -pl analytics-service test -Dtest=AnalyticsServiceApplicationTests`

## Next Branch Origin
- Suggested next branch should originate from: `feature/CoreDomainApiFoundation`
- Suggested next branch focus: `feature/SecurityAndInterServiceOrchestration`
