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

Prompt (summary):
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

---

## Phase 3: Spring Boot 3 Scaffold (Contract Implementation Shell)

Goal:
Generate a Spring Boot 3 service skeleton from the OpenAPI contract that compiles, runs, and enforces request validation + standardized errors.

Prompt (summary):
Requested generation of a Spring Boot 3 project from `modern-slice/openapi/openapi.yaml` with controllers aligned to the spec, a service-layer seam for later business logic, and consistent error handling.

AI Actions:
- Generated Spring Boot 3 scaffold from OpenAPI using openapi-generator
- Produced controllers/interfaces and DTO models based on the contract
- Wired bean validation based on OpenAPI constraints
- Implemented/produced global error handling returning `{code, message, details}`

Human Review:
- Confirmed endpoints exist and match OpenAPI paths/methods
- Confirmed transfer endpoint requires request body fields defined in spec
- Confirmed error response matches the contract schema
- Confirmed no DB integration attempted yet (deferred to Phase 4)

Verification:
- Generator command:
  - `[paste your openapi-generator docker command]`
- App starts:
  - `mvn test` passes
  - `mvn spring-boot:run` starts successfully
- Contract enforcement via curl:
  - `curl -i -X POST http://localhost:8080/auth/login -H 'Content-Type: application/json' -d '{}'`
    - returns 400 with VALIDATION_ERROR and missing field details
  - `curl -i -X POST http://localhost:8080/accounts/123/transfer -H 'Content-Type: application/json' -d '{}'`
    - returns 400 with VALIDATION_ERROR and missing field details

Modernization Decisions:
- Keep OpenAPI as source of truth; do not hand-edit controllers
- Separate HTTP layer from business logic via service seam (AuthService, TransferService)
- Enforce validation at the edge before touching persistence

Decision:
Spring scaffold accepted as contract-compliant shell.
Tagged as `phase3-spring-scaffold`.

---

## Phase 4: Data layer + real login + JWT

Prompt (summary):
Implemented Phase 4 end-to-end via LLM
- configure Spring datasource from LEGACY_PG_* env vars
- add JDBC + Postgres + JWT dependencies
- implement CustomerRepository (JdbcTemplate) querying customer(userid,pword,actno)
- implement AuthService issuing signed JWT with claims {userId, roles, accountId, exp}
- wire login controller to DB-backed service
- return 401 with standard error schema on invalid credentials
- keep OpenAPI contract unchanged
- provide mvn + curl verification steps

Verification commands and results:
- ./mvnw test
  - Result: PASS/FAIL
- ./mvnw spring-boot:run with env vars
  - Result: PASS/FAIL
- curl login success
  - Status:
  - Body snippet:
- curl login failure
  - Status:
  - Body snippet:

Notes:
- DB schema used: customer(userid, pword, actno)
- Auth model: JWT Bearer, claims: userId, roles, accountId, exp
- Assumptions:
  - (example: plaintext password in legacy schema)

---

## Phase 5: Transfer logic + tests

Prompt (summary):
Implement transfer endpoint end-to-end using legacy Postgres schema with JdbcTemplate.
- Require Bearer JWT and enforce account ownership via accountId claim.
- Execute atomic balance update using a single DB transaction and SELECT ... FOR UPDATE.
- Insert a matching record into the legacy transaction table.
- Return transactionId, status, and balanceAfter.
- Handle errors with standard schema:
  - 401 invalid/missing token
  - 403 ownership mismatch
  - 404 account not found
  - 409 insufficient funds

Add:
- unit tests for TransferService
- integration tests with Testcontainers Postgres using legacy schema

Verification:
- mvn test
- curl login then transfer

Output:
- files changed
- commands
- assumptions about transaction table mapping
