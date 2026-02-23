# Agentic Modernization: Legacy Banking System

## 60-Second Pitch
This repository shows how to modernize a legacy banking system with AI assistance without a full rewrite. It keeps domain behavior intact and modernizes one high-risk vertical slice (Login + Transfer) using contract-first APIs, Spring Boot service boundaries, automated tests, and CI security gates.

## Interview Flow (15 min)
1. Start legacy stack.
2. Start modern stack.
3. Login via API and capture JWT.
4. Execute transfer via API.
5. Open Swagger UI and map the calls to the contract.

```bash
# 1) Start legacy stack

docker compose -f legacy-system/docker-compose.yml up --build -d

# 2) Start modern stack

docker compose -f modern-slice/docker-compose.yml up --build -d

# 3) Login and capture token

JWT=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userid":"Adi","password":"yaji"}' | jq -r '.jwt')

# 4) Execute transfer

curl -s -X POST http://localhost:8080/accounts/1700120011/transfer \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"targetAccountId":"1700120043","amount":200,"currency":"USD","reference":"interview-demo"}'
```

5. Open:
- Legacy UI: `http://localhost:8082/banking/Home`
- Modern API: `http://localhost:8080`
- Swagger UI: `http://localhost:8081`

For Docker Desktop shared-DB runs, use:

```bash
MODERN_SLICE_PG_HOST=host.docker.internal docker compose -f modern-slice/docker-compose.yml up --build -d
```

## Objective
Demonstrate a repeatable AI-assisted modernization workflow on a legacy servlet/JSP system.

## What This Repository Demonstrates
- Contract-first API design (`OpenAPI` as source of truth)
- AI-assisted legacy comprehension and service scaffolding
- Migration of Login + Transfer into Spring Boot controller/service/repository layers
- Unit and integration test coverage for the modern slice
- Containerized local runtime for deterministic demos
- CI enforcement for tests and vulnerability scanning

## Scope
Modernized slice only:
- Login
- Transfer

Out of scope:
- Full legacy rewrite
- Domain/business rule redesign

## Outputs
- Legacy reference system (`legacy-system`)
- Modern Spring Boot slice (`modern-slice`)
- OpenAPI contract and examples
- Test suite (unit + integration)
- Docker Compose runtime
- CI gates for quality and security
- AI usage audit log

## Method
| Section | Content |
| --- | --- |
| Purpose | Repeatable AI-augmented modernization pattern for regulated systems |
| Inputs | Legacy code, schema, OpenAPI contract, API examples |
| Loop | Comprehend -> Spec -> Generate -> Validate -> Secure -> Package |
| Guardrails | Contract is source of truth, tests required, security scans required |
| Governance | AI-generated code requires tests, review checklist, and scan evidence |
| Scale-out | Template repo + CI policy + prompt library for team adoption |

## Repository Structure
- `legacy-system/`: legacy servlet/JSP reference implementation
- `modern-slice/`: Spring Boot modernization slice (app + OpenAPI + compose)
- `.github/workflows/ci.yml`: CI quality and security gates

## Modernization Summary
- Legacy HTTP/session flow replaced with JSON + JWT API auth
- Servlet/JDBC coupling replaced by controller/service/repository boundaries
- Manual flow checks replaced by automated tests and CI gates
- App-server-centric runtime replaced by container-first execution

## Success Criteria
- Login and transfer work end-to-end
- Implementation remains aligned with `modern-slice/openapi/openapi.yaml`
- Demo runs from a clean clone with Docker Compose
- CI enforces tests and security scan thresholds
- AI-assisted decisions and validation evidence are documented

## Acknowledgements
Original legacy applications:
- [banking-application by ayaji](https://github.com/ayaji/Banking-application)
- [banking-modernization by kush](https://github.com/kushmirc/banking-modernization)
