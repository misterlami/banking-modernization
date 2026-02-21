# Agentic Modernization: Legacy Banking System

## Objective
Demonstrate a repeatable AI-augmented SDLC workflow to modernize a legacy servlet/JSP banking system using a spec-first, agent-driven approach.

Focus: one vertical slice (Login + Transfer) with depth, governance, and measurable outcomes.

## What this repo shows
- Spec-first modernization using OpenAPI
- AI agent orchestration (prompting, iteration, validation)
- Spring Boot 3 service generated from contract
- Contract-derived tests + CI validation loop
- Container-first delivery
- Security baseline (JWT, validation, dependency scan)
- Evidence of prompts, decisions, and verification steps

## Deliverables
- Legacy banking application (reference only)
- Modernized vertical slice (Login + Transfer)
- OpenAPI contract + examples
- Spring Boot implementation
- Integration + unit tests
- Docker + docker-compose
- CI pipeline with quality and security gates
- Prompt library and execution log
- “Before vs After” architecture and metrics

## Repository structure
- `/legacy-system` → source of truth for behavior
- `/modern-slice` → AI-driven modernization implementation
- `/modern-slice/prompts` → prompt patterns and orchestration
- `/modern-slice/openapi` → OpenAPI contract
- `/modern-slice/infra` → Docker and CI assets

## Success criteria
- Login + Transfer run end-to-end
- Spec and implementation aligned
- Tests pass locally and in CI
- Container reproducible from clean clone
- Security and validation enforced
- Clear evidence of AI orchestration and verification

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
