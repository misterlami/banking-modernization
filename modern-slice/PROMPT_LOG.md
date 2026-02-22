# AI-Augmented Modernization Prompt Log

This document records how AI tools were used in a controlled, verifiable modernization workflow.
Each entry documents:
- Intent
- Prompt pattern
- AI output
- Human validation
- Verification steps

Only architecture-impacting steps are logged.

---

## Phase 1: Legacy Runtime Enablement

Goal:
Make the legacy banking system runnable locally to analyze login and transfer flows.

Prompt (summary):
Requested Codex to help run the legacy system using Docker Compose, including necessary configuration adjustments for containerized execution.

AI Actions:
- Generated docker-compose.yml
- Generated Dockerfile
- Adjusted database configuration to use environment variables
- Enabled PostgreSQL + Mongo container wiring

Human Review:
- Confirmed changes were limited to runtime/environment configuration
- Verified no business logic changes to login or transfer flows
- Ensured servlet mappings remained unchanged

Verification:
- `docker compose up --build` succeeds
- http://localhost:8082/banking/Home loads
- Login successful with provided credentials
- Database initialized via schema script

Decision:
Accepted containerized legacy runtime as baseline reference system for modernization.

---

## Phase 1.5: Legacy Flow Comprehension

Goal:
Identify entrypoints, data models, and validation behavior for login and transfer flows.

Prompt (summary):
Asked AI to locate login and transfer servlets, URL mappings, DB tables touched, and validation logic.

AI Findings:
- Login form posts to `/login`
- Servlet mapped via web.xml to `CustomerLogin`
- Credentials validated against `customer` table
- Transfer flow implemented via transfer-related servlet
- JDBC calls update balance and record transaction

Human Review:
- Confirmed servlet mappings in web.xml
- Verified request parameter names
- Identified tables: `customer`, `transaction`
- Confirmed session-based auth model

Verification:
- Login works via UI
- Transfer behavior observable in legacy flow

Decision:
Proceed with contract-first modernization (stateless + JSON).

---

## Phase 2: Spec-First Modernization (OpenAPI Contract)

Goal:
Define modernization contract before writing new code.

Prompt:
Requested generation of OpenAPI 3.0 specification with:
- POST /auth/login
- POST /accounts/{accountId}/transfer
- JWT-based authentication
- Standardized error schema
- Validation constraints
- Success and failure examples

AI Output:
- Generated complete OpenAPI 3.0 YAML
- Defined reusable schemas
- Included bearerAuth security scheme
- Included validation constraints
- Included example payloads

Human Edits:
- Tightened validation rules (amount > 0, ISO currency)
- Ensured consistent error model across endpoints
- Verified transfer endpoint requires JWT

Verification:
- Swagger UI renders successfully
- Endpoints and schemas visible
- Examples load correctly
- Error schema reusable

Modernization Decisions:
- Replace session auth with stateless JWT
- Replace form submission with JSON
- Normalize error responses
- Separate transfer business logic from servlet concerns

Decision:
OpenAPI contract accepted as authoritative source of truth.
Tagged as `phase2-openapi-contract`.
