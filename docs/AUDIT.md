# Foundation audit — 2026-11-02

Inspected `E:\repo\job-platform`, not the earlier portfolio repository.
Initially no root Git repository. Upstream HEAD matched the specified revision;
its only untracked item was a local crawl JSON. PostgreSQL held exactly **407**
Elastic jobs, IDs 1–407, unique on `(provider, board_token, external_job_id)`.
Postgres was actually on **5433**, despite outdated README instructions.

Next.js 16 already called an ASP.NET Core API on 5078. The Node importer retained
a second write path, creating schema at import time. `src/lib/db.ts` was unused.
Go was installed at `C:\Program Files\Go\bin` but absent from PATH. Maven was
missing; Java 27 was available. Builds and integration tests use Java 25 in Docker.
The Go monitor had no external dependencies but omitted provider IDs and did not
verify Greenhouse's reported total. Its HTTP transport already blocked private IPs,
redirects, oversized responses and TDM reservation. Those safeguards were preserved.

The SQL schema is retained, with Flyway baseline **0**, idempotent V1 and additive
V2. `ddl-auto=validate`, Flyway clean disabled. Legacy credential/database/volume
identifiers are deliberately retained; changing them would orphan existing data.

Historical README files, setup scripts, Node importer and C# sources are preserved
under ignored `.local/archive/`. A running C# process held compiled binaries open;
the legacy API was stopped after Java validation. Its remaining directory (held open by a terminal) is ignored and is not part of the initial commit. Local
backups and the upstream reference are retained and ignored. No datasets are staged.

Security: parametrized JPA/JDBC queries; bounded pagination/search; authenticated
internal ingestion; loopback-only published ports; constant-time token comparison;
transactional source locks; URL scheme validation; no raw HTML rendering. Production
identity, public rate limits and deployment hardening are not implemented.

Successful live migration preserved all 407 existing IDs. A live Go → Java import
processed 407 jobs without increasing their count. Baseline, V1 and V2 were recorded
successfully. See VALIDATION.md for the final check record.
