# NeoBank Backend

Spring Boot backend for the NeoBank platform.

## Run

- Build: `mvn clean install`
- Run: `mvn spring-boot:run`
- Health: `http://localhost:8080`

## Sprint 4 Highlights

- Insights API: user financial summaries and 6-month trend data.
- Admin APIs: platform KPIs, pending approvals, system health, user status controls.
- Admin activity: recent transactions and login events.
- Audit logging: admin write actions logged under the `AUDIT_LOG` logger.

## Swagger / OpenAPI

- UI: `http://localhost:8080/swagger-ui.html`
- JSON: `http://localhost:8080/v3/api-docs`

## Sprint 4 Endpoints

- `GET /api/insights/{userId}`
- `GET /api/admin/dashboard`
- `GET /api/admin/pending-approvals`
- `GET /api/admin/system-health`
- `GET /api/admin/users`
- `PATCH /api/admin/users/{userId}/status`
- `GET /api/admin/users/{userId}/activity`
