# Validation record — 2026-11-02

## Passed

- Java 25 Docker build, Spring Boot 4.1.2, Maven 3.9.11: `mvn -B -ntp verify`.
  JUnit **5.14.1**, 3 tests, zero skipped: 2 HTTP/PostgreSQL integration tests plus
  lifecycle guard regression. Real Testcontainers PostgreSQL 16, isolated from local data.
  Coverage includes health/DB reads, literal search and percent escaping, stable
  multi-page results without overlap, invalid page sizes/types, missing detail,
  stats, unauthorized writes, idempotent imports, feed duplicates, URL tracking,
  empty-feed rejection, incomplete-feed safety and lifecycle eligibility guards.
- Windows Maven Wrapper: `./mvnw.cmd -version` successfully downloaded Maven 3.9.11.
  Native host Java is 27; compilation/integration checks ran with Java 25 in Docker.
- Go: `go test ./...`, 5 tests: rich parser validation, token rejection,
  mixed/private DNS rejection, HTTP success/gone, stable IDs and completeness metadata.
- Frontend: `npm run lint`, `npm run typecheck`, `npm test` (2 tests), `npm run build`.
- Browser smoke using headless Microsoft Edge: 20 list cards, second page, job detail,
  employer link, Security search, no-results state, mobile 390px without horizontal
  overflow. Desktop/mobile screenshots are local `.local/` artifacts, not committed.
- Compose image build and `docker compose up -d --wait`: API, PostgreSQL and Redis healthy.
- Existing DB: 407 rows, IDs **1 through 407** before and after migration/Compose handover.
  Flyway baseline 0, V1, V2 applied successfully. Live Go → Java → PostgreSQL crawl
  imported 407 Elastic jobs, complete=true, duplicate count 0; repeat preserved row count.
- Root `.env`, web local env, dump, upstream Git history, target and local archive
  are ignored. Initial-commit candidates checked for known token/private-key patterns;
  none found. No Git remote, commit or push was created.
- `npm audit --omit=dev`: **0 vulnerabilities** reported.

## Open findings and limits

- Full npm audit reports **16 moderate** findings in the lint/development dependency
  chain (minimatch/ESLint). Nonbreaking `npm audit fix` leaves them unresolved; the
  suggested forced downgrade is incompatible and was not applied. Advisory:
  https://github.com/advisories/GHSA-g3hv-75wg-m249 .
- Spring's JUnit extension requires JUnit 6; JUnit 5 tests deliberately bootstrap
  Spring and manage the Testcontainer directly. Initial incompatible attempts were
  corrected; they are not counted as successful checks.
- A native running JAR initially prevented repackaging on Windows; it was stopped
  and packaging passed. Do not build over a running native JAR; use Compose or stop it.
- No production auth, public gateway rate limiting, public deployment penetration
  test, full software-composition vulnerability scan or exhaustive license/SBOM audit.
- Import errors schema is prepared; external failures are currently runner stderr.
  No automatic job closing, missed-observation persistence, account system or second ATS.
- Legacy C# sources/scripts are archived locally; an open terminal still holds its
  old compiled directory. The legacy API process was stopped and the directory ignored.
- Third-party MIT notices are included. Redis 7.4 licensing and job-content rights
  require review before public operation; choose an original-code license before release.

## Reproduce containerized Java checks from PowerShell

```powershell
docker run --rm `
  -v E:/repo/job-platform/services/api:/work `
  -v inreach_maven_cache:/root/.m2 `
  -v /var/run/docker.sock:/var/run/docker.sock `
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal `
  -w /work maven:3.9.11-eclipse-temurin-25 mvn -B -ntp verify
```

Docker checks use fresh disposable PostgreSQL containers, not the existing volume.
The GitHub Actions workflow was authored but has not run remotely (nothing pushed).
