# Backend Env Templates

Use these example files to create your local IntelliJ env files (for example under `env/intellij/`).
They are safe templates and should be committed.

Why separate files:
- Variables like `SERVER_PORT` and `DB_URL` are service-specific.
- A single shared file would cause collisions across services.

Files:
- `config-server.env`
- `discovery-server.env`
- `api-gateway.env`
- `auth-service.env`
- `expense-service.env`
- `income-service.env`
- `category-service.env`
- `budget-service.env`
- `analytics-service.env`
- `recurring-service.env`
- `notification-service.env`

Optional frontend reference:
- `frontend.env`

Infra notes:
- RabbitMQ env keys are included in services that publish/consume email events.
- Redis env keys are included for `auth-service` (token revocation) and `analytics-service` (cache).
