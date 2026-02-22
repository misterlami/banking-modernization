
| Metric                    | How you will measure                                    |
| ------------------------- | ------------------------------------------------------- |
| Contract coverage         | # endpoints with examples + tests derived from examples |
| Tests added               | `./mvnw test` count summary + test files                |
| Coverage                  | JaCoCo % for service layer                              |
| CI time                   | GitHub Actions run duration                             |
| Security scan             | Dependency scan output (pass/fail + findings count)     |
| Container reproducibility | `docker compose up` works from clean clone              |



### Docker/Podman commands to run mordern slice:
```
docker run --rm -it \
  -p 8080:8080 \
  -e LEGACY_PG_HOST=host.docker.internal \
  -e LEGACY_PG_PORT=5432 \
  -e LEGACY_PG_DB=banking_system \
  -e LEGACY_PG_USER=banking_user \
  -e LEGACY_PG_PASSWORD='Passw0rd!' \
  -e JWT_SECRET='replace-with-strong-secret' \
  -e JWT_ISSUER='banking-modernization' \
  -e JWT_TTL_SECONDS=3600 \
  -v "$PWD":/workspace \
  -w /workspace/modern-slice/app \
  maven:3.9-eclipse-temurin-21 \
  mvn -q org.springframework.boot:spring-boot-maven-plugin:run
```

```
JWT=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userid":"Adi","password":"yaji"}' | jq -r '.jwt')

curl -s -X POST http://localhost:8080/accounts/1700120011/transfer \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"targetAccountId":"1700120043","amount":200,"currency":"USD","reference":"invoice"}'
```

```
cd modern-slice/app
unset TESTCONTAINERS_HOST_OVERRIDE
export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
export TESTCONTAINERS_RYUK_DISABLED=true
./mvnw test
```
