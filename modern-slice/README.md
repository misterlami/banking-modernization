# Modern Slice

Spring Boot modernization of the Login + Transfer vertical slice.

Runtime configuration is environment-driven via:
- `LEGACY_PG_HOST`
- `LEGACY_PG_PORT`
- `LEGACY_PG_DB`
- `LEGACY_PG_USER`
- `LEGACY_PG_PASSWORD`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_TTL_SECONDS`

## How To Run (Shared DB Default)

Default mode starts:
- Modern API on `http://localhost:8080`
- Swagger UI on `http://localhost:8081`

```bash
docker compose -f modern-slice/docker-compose.yml up --build -d
```

In this default mode, modern-slice connects to an external/shared Postgres on host port `5432`.
For side-by-side demos, start legacy first (it brings Postgres).

## Run Legacy + Modern Side-By-Side

From repository root:

```bash
# 1) Start legacy stack (provides Postgres + Mongo + legacy UI)
docker compose -f legacy-system/docker-compose.yml up --build -d

# 2) Start modern stack (API + Swagger UI, using shared Postgres from legacy)
docker compose -f modern-slice/docker-compose.yml up --build -d
```

URLs:
- Legacy UI: `http://localhost:8082/banking/Home`
- Modern API: `http://localhost:8080`
- Modern Swagger UI: `http://localhost:8081`

Login and transfer smoke test:

```bash
JWT=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userid":"Adi","password":"yaji"}' | jq -r '.jwt')

curl -s -X POST http://localhost:8080/accounts/1700120011/transfer \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"targetAccountId":"1700120043","amount":200,"currency":"USD","reference":"invoice"}'
```

## Standalone Modern Mode (with local Postgres profile)

Run modern-slice without legacy by enabling the `standalone` profile and pointing the app to the profile Postgres service:

```bash
MODERN_SLICE_PG_HOST=postgres docker compose -f modern-slice/docker-compose.yml --profile standalone up --build -d
```

## Docker Desktop Note

If you are using Docker Desktop (instead of Podman), shared-DB mode should usually set:

```bash
MODERN_SLICE_PG_HOST=host.docker.internal docker compose -f modern-slice/docker-compose.yml up --build -d
```

## CI Gates

Workflow: `.github/workflows/ci.yml`

On each `push` and `pull_request`, CI runs:
- Maven tests in `modern-slice/app`
- Trivy filesystem vulnerability scan

Gate behavior:
- CI fails when Trivy finds vulnerabilities at severities in `TRIVY_SEVERITY` (default: `HIGH,CRITICAL`)
- CI fails when tests fail

How to adjust the vulnerability threshold:
- Edit `TRIVY_SEVERITY` in `.github/workflows/ci.yml` (for example `CRITICAL` only, or `MEDIUM,HIGH,CRITICAL`)

## Local Security Scan

From repository root (requires Trivy installed):

```bash
trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 .
```

## Measurable Outcomes

| Metric                                       | Value                                                                                                                  | Source                                                                                               |
| -------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| Endpoints implemented                        | 2 contract endpoints (`POST /auth/login`, `POST /accounts/{accountId}/transfer`) + 1 service status endpoint (`GET /`) | OpenAPI contract and controller implementation                                                       |
| Files changed since modernization began      | 63                                                                                                                     | Git history                                                                                          |
| Lines added/removed (approx from git)        | +3823 / -58                                                                                                            | Git history                                                                                          |
| Modules/packages created                     | 10 Java package directories                                                                                            | Java source tree change set                                                                          |
| Total test count                             | 7                                                                                                                      | Maven/JUnit test reports                                                                             |
| Unit tests count                             | 4                                                                                                                      | Maven/JUnit test reports                                                                             |
| Integration tests count                      | 3                                                                                                                      | Maven/JUnit test reports                                                                             |
| Coverage %                                   | 14.42%                                                                                                                 | Scoped JaCoCo report (`service`/`repository`/`controller`)                                           |
| `mvn test` duration                          | 12.80s                                                                                                                 | Maven test run output                                                                                |
| `mvn package` duration                       | 4.99s                                                                                                                  | Local Maven package timing output                                                                    |
| Build success rate (local)                   | 100% (latest local test/package runs succeeded)                                                                        | Local Maven build outcomes                                                                           |
| Docker image size (modern-slice app)         | 320 MB                                                                                                                 | Container image metadata                                                                             |
| Time to start app container                  | 0.19s                                                                                                                  | Container restart timing                                                                             |
| Compose startup success (PASS/FAIL)          | PASS                                                                                                                   | Compose runtime result                                                                               |
| Dependency scan result (PASS/FAIL)           | PASS                                                                                                                   | Trivy vulnerability scan                                                                             |
| Number of HIGH vulnerabilities               | 0                                                                                                                      | Trivy scan results                                                                                   |
| Number of CRITICAL vulnerabilities           | 0                                                                                                                      | Trivy scan results                                                                                   |
| CI pipeline status (last run)                | PASS                                                                                                                   | GitHub Actions run log artifact (`logs_58195667247.zip`)                                             |
| CI duration (last run)                       | 55.59s                                                                                                                 | GitHub Actions job log timestamps (`2026-02-22T21:22:15.0705143Z` to `2026-02-22T21:23:10.6586973Z`) |
| Steps executed in CI (build, test, scan)     | Test + Scan executed (Maven test, Trivy gate, Trivy SARIF scan, artifact upload)                                       | GitHub Actions run log artifact (`logs_58195667247.zip`)                                             |
| Legacy auth vs new auth model                | Session-based vs JWT                                                                                                   | Legacy servlet flow and stateless auth implementation                                                |
| Legacy transfer vs new transfer architecture | Servlet/JDBC vs layered API                                                                                            | Controller/service/repository implementation                                                         |
| Validation approach change                   | Manual vs contract + bean validation                                                                                   | OpenAPI contract and Jakarta validation annotations                                                  |
| Deployment model change                      | Manual app server vs docker-compose                                                                                    | Container-first runtime configuration                                                                |

### What these metrics demonstrate

- Modernization safety: high-risk flow changes are constrained to a defined slice (auth + transfer) with measurable change volume and integration tests in place.
- Delivery repeatability: container startup is deterministic (compose PASS, fast container restart), and CI workflow codifies test/scan execution.
- Security posture: dependency gating is active and current scan output shows 0 HIGH and 0 CRITICAL vulnerabilities.
- Engineering quality: the implementation is contract-driven, layered, and test-backed (7 tests split across unit/integration).
- Readiness for scale: the architecture moved from servlet/JDBC coupling to API + service + repository boundaries and containerized deployment.

### Known gaps (tracked)

- None currently in the measured metrics set.

## Shutdown

```bash
docker compose -f modern-slice/docker-compose.yml down
docker compose -f legacy-system/docker-compose.yml down
```

Use `down -v` when you want a clean database re-initialization.
