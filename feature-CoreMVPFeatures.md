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
- Fixed API Gateway route matching/rewrite for base collection endpoints:
  - Replaced brittle `RewritePath` regex routes with explicit base path matching + `StripPrefix=1`.
  - Resolved create-flow failures for endpoints like `/api/categories` and `/api/expenses`.
- Added RabbitMQ-driven email event pipeline:
  - `auth-service` publishes welcome events on successful signup.
  - `recurring-service` publishes autopay reminder events for due-soon recurring entries.
  - `expense-service` publishes big-expense alerts when expense amount exceeds configured threshold.
  - `notification-service` consumes events from queue, stores notifications, and dispatches email.
- Implemented big-expense business rule:
  - Added `income-service` endpoint for average monthly income over trailing months.
  - `expense-service` now compares each expense against `BIG_EXPENSE_THRESHOLD_PERCENT` of average monthly income.
- Implemented Redis usage:
  - `auth-service` token revocation now writes and checks Redis keys with TTL (with safe in-memory fallback).
  - `analytics-service` now uses Redis-backed caching for summary/trend/forecast/health endpoints.
- Expanded backend env templates under `env/example` for RabbitMQ, Redis, alert rule, and SMTP settings.

## Verification
- Compile verification:
  - `./mvnw -pl auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service -DskipTests compile`
- Test verification:
  - `./mvnw -pl auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service test`
  - Result: `BUILD SUCCESS`
  - `./mvnw -pl config-server,discovery-server,api-gateway,auth-service,expense-service,income-service,category-service,budget-service,analytics-service,recurring-service,notification-service test`
  - Result: `BUILD SUCCESS`
- Post integration compile verification:
  - `./mvnw -f SpendSmart-SpringBootMicroservices/pom.xml -pl auth-service,expense-service,income-service,recurring-service,notification-service,analytics-service,api-gateway -am test -DskipTests`
  - Result: `BUILD SUCCESS`

## Next Branch Origin
- Suggested next branch should originate from: `feature/CoreMVPFeatures`
- Suggested next branch focus: `feature/PlatformAdminAndExports`
