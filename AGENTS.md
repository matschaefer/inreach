# Inreach coding instructions

- Java 25 / Spring Boot API owns validation, persistence and lifecycle. No C# or direct frontend SQL.
- Preserve existing data, backups, schema compatibility and external volume names.
- Additive Flyway migrations only; never edit applied migrations or enable clean/drop.
- Identity: provider + board + provider job ID. Failed/incomplete feeds must never close jobs.
- Keep internal ingestion authenticated and published ports on loopback.
- Preserve upstream MIT notices and shadcn notices; never import upstream job datasets.
- Go connector tests: `cd services/crawler; go test ./...`.
- Java: `cd services/api; ./mvnw.cmd verify` (Docker required for Testcontainers).
- Web: `cd apps/web; npm ci; npm run lint; npm run typecheck; npm test; npm run build`.
- Do not commit secrets, dumps, local archives, build outputs or upstream clones.
- Do not commit or push unless the user explicitly requests it.
