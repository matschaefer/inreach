# Development (PowerShell)

Requirements: Docker Desktop Linux containers, Node 24, Git; Java 25 and Go >=1.24
for native development. Maven is downloaded by the committed wrapper. Go may be at
`C:\Program Files\Go\bin`; add it to PATH for the current terminal:

```powershell
$env:Path = "C:\Program Files\Go\bin;$env:Path"
```

## Preserve the existing installation

Keep the current root `.env`, password and database name. Never overwrite it with the
example. It now contains a locally generated ingestion token. Existing volumes are
`job-platform_postgres_data` and `job-platform_redis_data`. Compose uses these as
external volumes. The dump at the root is preserved and ignored; it predates this run.

The handover below has already been performed in this workspace; do not repeat it.
For another installation: old containers use the previous Compose project. To hand them over without deleting
volumes, stop/remove only those two containers (data stays in external volumes):

```powershell
docker stop job-platform-postgres-1 job-platform-redis-1
docker rm job-platform-postgres-1 job-platform-redis-1
docker compose up -d --build
```

Do not run this while another local app is using those services. Stop any native Java
API on 8080 before starting the Compose API. The legacy C# service on 5078 is unused.

## New installation

```powershell
Set-Location E:\repo\job-platform
Copy-Item .env.example .env
# Set POSTGRES_PASSWORD in .env.
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
# Paste the result into INREACH_INGESTION_TOKEN in .env (never commit it).
docker volume create job-platform_postgres_data
docker volume create job-platform_redis_data
docker compose up -d --build
```

For a different volume set, set INREACH_POSTGRES_VOLUME/INREACH_REDIS_VOLUME before
creation. Never point an existing install at empty volumes accidentally.

## Frontend and crawler

```powershell
Copy-Item apps/web/.env.example apps/web/.env.local # only if absent
npm ci --prefix apps/web
npm run dev --prefix apps/web
# A second terminal, from the project root:
node --env-file=.env scripts/crawl-greenhouse.mjs elastic
# Optional development scheduler; Ctrl+C stops it:
node --env-file=.env scripts/watch-greenhouse.mjs elastic
```

The runner uses `go` from PATH or INREACH_GO, a local per-source lock, and the Java
Bearer token. After a crash, remove `.local/locks/<board>` only after confirming no
crawl is running. It contains no data. Feeds stay in memory; stdout is not committed.

## Native Java instead of Compose API

```powershell
docker compose up -d postgres redis
Push-Location services/api
./mvnw.cmd -B verify
Pop-Location
node --env-file=.env scripts/start-api.mjs
```

Tests create an isolated PostgreSQL Testcontainer, never use the existing jobs DB.
Build without tests only when packaging after a successful verify:
`./mvnw.cmd -B package -DskipTests`.

## Ports and checks

Web 3000; Java 8080; PostgreSQL **5433**; Redis 6379. Published ports bind loopback.

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/health
Invoke-RestMethod 'http://127.0.0.1:8080/api/jobs?q=Security&page=1'
npm run lint --prefix apps/web
npm run typecheck --prefix apps/web
npm test --prefix apps/web
npm run build --prefix apps/web
Push-Location services/crawler; go test ./...; Pop-Location
```

Migrations run automatically when Java starts. An existing non-Flyway schema is
baselined at 0, then V1 checks existing tables and V2 adds metadata/history. Hibernate
validates rather than recreating schema. An unexpected schema stops startup.
