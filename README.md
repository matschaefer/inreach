# Inreach

Inreach collects job listings from company career feeds and makes them searchable in a single web interface. It currently supports Greenhouse, with a server-rendered Next.js frontend, a Java API and a Go connector.

The project is under active development. The current implementation covers importing, storing, searching and browsing jobs, with applications handled on the employer's website.

## Features

- Search job titles and browse paginated results.
- Open individual listings and follow the original application link.
- View job counts, registered sources and the latest import timestamp.
- Import Greenhouse boards manually or through a local recurring watcher.
- Update existing listings using stable provider IDs, without creating duplicates when titles or tracking URLs change.
- Validate and record imports with authenticated writes, database transactions and per-source locks.

Missing listings are not automatically closed. Full job descriptions, advanced filters, user accounts and matching are not yet available in the web interface. Public deployment and production access controls remain outside the current setup.

## Architecture

```text
Browser → Next.js → Java API → PostgreSQL
Greenhouse → Go connector → Node.js runner → Java API
```

The Java API owns validation, persistence and job lifecycle rules. The frontend reads through the API and has no direct database access. The Go connector fetches and normalizes external data; Node.js scripts handle execution and delivery to the authenticated import endpoint.

| Component | Stack |
| --- | --- |
| Web | Next.js 16, React 19, TypeScript, Tailwind CSS |
| API | Java 25, Spring Boot 4.1.2, JPA/Hibernate, Spring JDBC, Flyway |
| Connector | Go 1.24+ |
| Local infrastructure | PostgreSQL 16, Redis 7.4, Docker Compose |

Redis is included in the local infrastructure but is not currently used by the application. Compose runs the API, PostgreSQL and Redis; the frontend and import scripts run separately.

## Getting started

The commands below use PowerShell and assume the repository root as the working directory.

### Requirements

- Docker Desktop with Linux containers
- Node.js 24
- Go 1.24 or later
- Java 25 for native API development; the container build provides its own JDK

### First-time setup

Create local configuration files only if they do not already exist:

```powershell
Copy-Item .env.example .env
Copy-Item apps/web/.env.example apps/web/.env.local
```

Set `POSTGRES_PASSWORD` in `.env` and generate a random `INREACH_INGESTION_TOKEN` of at least 24 characters:

```powershell
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

Copy the generated value into `INREACH_INGESTION_TOKEN`. Keep local environment files out of version control.

For a new installation, create the external volumes expected by Compose:

```powershell
docker volume create job-platform_postgres_data
docker volume create job-platform_redis_data
```

For an existing installation, retain its credentials, backups and volume names. Follow the [development guide](docs/DEVELOPMENT.md) for container handover rather than replacing the configuration or data volumes.

### Start the application

```powershell
docker compose up -d --build
npm ci --prefix apps/web
npm run dev --prefix apps/web -- --hostname 127.0.0.1
```

| Service | Local address |
| --- | --- |
| Web | http://127.0.0.1:3000 |
| API health | http://127.0.0.1:8080/api/health |
| PostgreSQL | `127.0.0.1:5433` |
| Redis | `127.0.0.1:6379` |

The API health endpoint checks database connectivity. The web health endpoint at `/api/health` checks only the Next.js service.

### Import jobs

In a second terminal, import a Greenhouse board by its token:

```powershell
node --env-file=.env scripts/crawl-greenhouse.mjs elastic
```

To repeat the import:

```powershell
node --env-file=.env scripts/watch-greenhouse.mjs elastic
```

The watcher waits six hours after each completed run by default. Set `CRAWL_INTERVAL_MINUTES` to a value between 15 and 1440 to change the interval. Each watcher handles one board; stop it with Ctrl+C.

## Repository layout

```text
apps/web/             Next.js pages, components and API client
services/api/         Java API, database migrations and integration tests
services/crawler/     Greenhouse connector, CLI and Go tests
scripts/              Import runner, watcher and native API launcher
docs/                 Architecture, setup, decisions and validation records
licenses/             Third-party license texts
.github/workflows/    CI checks for the API, web and connector
```

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/jobs?q=&page=1&pageSize=20` | Search and list jobs |
| GET | `/api/jobs/{id}` | Retrieve a listing |
| GET | `/api/jobs/stats` | Retrieve job and import statistics |
| GET | `/api/health` | Check API and database readiness |
| POST | `/internal/ingestion/greenhouse` | Import a feed using Bearer authentication |

Search matches title substrings without case sensitivity. Results exclude closed jobs and are ordered by latest observation, then ID. See the [architecture documentation](docs/ARCHITECTURE.md) for request limits and import behavior.

## Development checks

```powershell
Push-Location services/api
./mvnw.cmd verify
Pop-Location

Push-Location services/crawler
go test ./...
Pop-Location

Push-Location apps/web
npm ci
npm run lint
npm run typecheck
npm test
npm run build
Pop-Location
```

API integration tests require Docker and use an isolated PostgreSQL Testcontainer. GitHub Actions defines the corresponding checks for pushes and pull requests. Recorded results and known issues are documented in [VALIDATION.md](docs/VALIDATION.md).

Database changes use additive Flyway migrations. Preserve applied migrations and existing data; Hibernate validates the schema rather than recreating it.

## Documentation

- [Project overview (German)](docs/PROJEKTUEBERBLICK.md) — detailed walkthrough of the implementation and its limits
- [Architecture](docs/ARCHITECTURE.md) — service boundaries, identity and lifecycle rules
- [Development](docs/DEVELOPMENT.md) — local configuration, existing installations and native API development
- [Decisions](docs/DECISIONS.md) — technical choices and rationale
- [Validation](docs/VALIDATION.md) — test records and outstanding findings
- [Audit](docs/AUDIT.md) — project assessment

## Attribution and licensing

The Greenhouse connector includes code derived from the MIT-licensed JobSeek project. The UI includes an adaptation of the shadcn/ui Button. Their notices and license texts are retained in the repository; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

No upstream job dataset is included. A license for the project's original code has not yet been selected. Job-content usage rights and dependency distribution requirements must be reviewed before a public release.
