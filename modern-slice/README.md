# Modern Slice

Spring Boot modernization of the Login + Transfer vertical slice.

## Contracted Endpoints
- `POST /auth/login`
- `POST /accounts/{accountId}/transfer`
- `GET /` (service status)

OpenAPI source of truth: `modern-slice/openapi/openapi.yaml`

## Runtime Configuration
The app is environment-driven.

### Legacy Postgres connection
- `LEGACY_PG_HOST`
- `LEGACY_PG_PORT`
- `LEGACY_PG_DB`
- `LEGACY_PG_USER`
- `LEGACY_PG_PASSWORD`

### JWT settings
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_TTL_SECONDS`

Default compose values are set in `modern-slice/docker-compose.yml`.

## Run Side-by-Side (Recommended)
From repository root:

```bash
# 1) Start legacy stack (provides shared Postgres + legacy UI)
docker compose -f legacy-system/docker-compose.yml up --build -d

# 2) Start modern stack (API + Swagger)
docker compose -f modern-slice/docker-compose.yml up --build -d
```

URLs:
- Legacy UI: `http://localhost:8082/banking/Home`
- Modern API: `http://localhost:8080`
- Swagger UI: `http://localhost:8081`

## Smoke Test

```bash
JWT=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userid":"Adi","password":"yaji"}' | jq -r '.jwt')

curl -s -X POST http://localhost:8080/accounts/1700120011/transfer \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"targetAccountId":"1700120043","amount":200,"currency":"USD","reference":"invoice"}'
```

## Standalone Modern Mode
Run modern-slice without legacy UI, with the local `postgres` service profile:

```bash
MODERN_SLICE_PG_HOST=postgres docker compose -f modern-slice/docker-compose.yml --profile standalone up --build -d
```

If you use Docker Desktop in shared-DB mode, usually set:

```bash
MODERN_SLICE_PG_HOST=host.docker.internal docker compose -f modern-slice/docker-compose.yml up --build -d
```

## Quality and Security Proof
- CI workflow: `.github/workflows/ci.yml`
- Gates enabled:
  - Maven tests (`./mvnw -B test`)
  - Trivy vulnerability scan with fail threshold (`TRIVY_SEVERITY`, default `HIGH,CRITICAL`)
- Current status: green on latest run in this repository workflow history.

Optional local scan:

```bash
trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 .
```

## Shutdown

```bash
docker compose -f modern-slice/docker-compose.yml down
docker compose -f legacy-system/docker-compose.yml down
```

Use `down -v` when you need full DB re-initialization.
