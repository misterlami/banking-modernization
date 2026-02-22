# Agentic Modernization: Legacy Banking System

## Objective

Demonstrate a repeatable, AI-assisted modernization workflow applied to a legacy banking system.

This repository modernizes one vertical slice (Login + Transfer) to prove the pattern.

The goal is not to rewrite the banking application.
The goal is to modernize how it is specified, implemented, tested, secured, and delivered.

---

## What this repository demonstrates

A practical modernization approach applied to a legacy servlet/JSP system:

- Define the API contract first (OpenAPI)
- Use AI to analyze legacy behavior and generate a service scaffold
- Move functionality into a Spring Boot service boundary
- Add contract-derived integration tests
- Add unit tests for core business logic
- Package and run via containers
- Enforce quality and security gates in CI
- Capture prompts, decisions, and validation steps

This serves as a template for incrementally modernizing legacy systems with measurable governance and reproducibility.

---

## Scope

Only one vertical slice is modernized:

- Login
- Transfer

Legacy behavior is preserved.
Business rules are not redesigned.
The engineering system around them is modernized.

---

## Deliverables

- Legacy application (reference implementation)
- Modernized Spring Boot service for Login + Transfer
- OpenAPI contract with examples
- Contract-aligned integration tests
- Unit tests for transfer logic
- Docker and docker-compose runtime
- CI pipeline with build, test, and dependency scanning
- Prompt library and execution log
- Before vs After architecture and metrics

---

## Repository Structure

- `/legacy-system`
  Original application used to understand behavior

- `/modern-slice`
  AI-assisted modernization implementation

- `/modern-slice/openapi`
  OpenAPI contract (source of truth)

- `/modern-slice/prompts`
  Prompt patterns and orchestration logs

- `/modern-slice/infra`
  Docker and CI configuration

---

## What “Modernization” Means Here

| Legacy                           | Modern                                 |
| -------------------------------- | -------------------------------------- |
| JSP + Servlets                   | API contract + Spring Boot service     |
| Session-based auth               | Stateless JWT                          |
| Manual verification              | Contract-derived automated tests       |
| App-server deployment            | Containerized runtime                  |
| Implicit request/response shapes | Versioned OpenAPI specification        |
| Ad hoc changes                   | CI-enforced quality and security gates |

This approach modernizes delivery, safety, and scalability without rewriting domain logic.

---

## Success Criteria

- Login and Transfer run end-to-end
- OpenAPI contract and implementation are aligned
- Tests pass locally and in CI
- Containers run from a clean clone
- Security validation and dependency scanning enforced
- Clear evidence of AI-assisted development and verification loop

---

## Run legacy with Docker
From the repository root:

```bash
cd legacy-system
docker compose up --build
```

Application URL:
- `http://localhost:8082/banking/Home`

Default legacy test credentials (see `legacy-system/Readme.txt`):
- Customer: `Adi` / `yaji`
- Banker: `banker` / `urvi`
- Admin: `admin` / `admin`

Troubleshooting:
- First startup can take extra time while PostgreSQL initializes and imports `schema_postgresql.sql`.
- If you update schema/init SQL and need a clean re-init, run:

```bash
docker compose down -v
docker compose up --build
```

---

## Acknowledgements
Original Legacy Applications: [banking-application by ayaji](https://github.com/ayaji/Banking-application) → [banking-mordernization by kush](https://github.com/kushmirc/banking-modernization)

*This project is for learning purposes.*
