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

## Metrics

| Metric      | Current Value                                  |
| ----------- | ---------------------------------------------- |
| CI duration | TBD (populate from GitHub Actions run summary) |

## Shutdown

```bash
docker compose -f modern-slice/docker-compose.yml down
docker compose -f legacy-system/docker-compose.yml down
```

Use `down -v` when you want a clean database re-initialization.
