# Branch: feature/CoreMVPFeatures

## Origin
- Branched from: `feature/CoreDomainApiFoundation`
- Repository: `SpendSmart-SpringBootMicroservices`

## Scope
- Convert the domain foundation into a fully functional backend implementation.
- Make services production-runnable with PostgreSQL + env-based config.
- Wire critical inter-service orchestration (Budget, Analytics, Recurring, Notifications).

## Completed
- Added PostgreSQL runtime config via environment variables for all core services:
  - `auth-service`, `expense-service`, `income-service`, `category-service`, `budget-service`, `analytics-service`, `recurring-service`, `notification-service`
- Added PostgreSQL driver and test-profile H2 setup for all services.
- Added CORS configuration and global exception handling in all domain services.
- Implemented JWT token service and safer auth responses in `auth-service`.
- Implemented Expense -> Budget spent amount synchronization on create/update/delete.
- Implemented Budget -> Notification alert dispatch on threshold/exceeded.
- Implemented Recurring -> Expense/Income transaction generation.
- Implemented recurring due reminder dispatch (3-day reminder) to notification-service.
- Implemented scheduled budget period reset automation.
- Implemented live analytics aggregation from expense/income services:
  - Monthly summary, yearly summary, category breakdown, trends, cashflow, forecast, health score.
- Added and fixed service test profile properties for inter-service client placeholders.
- Integrated full platform services around domain microservices:
  - Enabled and configured `config-server` as Spring Cloud Config Server.
  - Enabled and configured `discovery-server` as Eureka Server.
  - Integrated `api-gateway` with Eureka + Config Server and service routes for all domain APIs.
- Migrated inter-service communication to discovery-aware load-balanced service IDs:
  - Removed localhost-bound service defaults from internal service-to-service URLs.
  - Added `@LoadBalanced RestTemplate` where required.
- Added Spring Cloud Config + Eureka client integration across all domain services.
- Standardized property handling with shell-injected environment variables and safe local fallbacks in `application.properties`.

## Verification
- Compile verification:
  - `./mvnw -pl auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service -DskipTests compile`
- Test verification:
  - `./mvnw -pl auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service test`
  - Result: `BUILD SUCCESS`
  - `./mvnw -pl config-server,discovery-server,api-gateway,auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service test`
  - Result: `BUILD SUCCESS`

## Next Branch Origin
- Suggested next branch should originate from: `feature/CoreMVPFeatures`
- Suggested next branch focus: `feature/PlatformAdminAndExports`
