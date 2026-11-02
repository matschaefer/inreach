# Inreach

Dein nächster Schritt: eine eigenständige Jobplattform mit Stellen direkt von
Unternehmenskarriereseiten. Das Fundament verbindet eine serverseitige Next.js-App,
einen zentralen Java-Monolithen und einen schlanken Go-Connector für Greenhouse.

## Architektur und Stack

- Next.js 16 / React / TypeScript, Tailwind CSS, shadcn/ui Button.
- Java 25, Spring Boot 4.1.2, MVC, JPA/Hibernate, PostgreSQL JDBC, Flyway, Validation.
- Go HTTP-Crawler mit transparenter MIT-Attribution.
- PostgreSQL 16, Redis 7.4, Docker Compose; GitHub Actions.
- JUnit, PostgreSQL Testcontainers, Node-Tests und Go-Regressionstests.

## Lokal starten

Voraussetzungen: Docker Desktop, Node 24, Go >=1.24; Java 25 für native API-Entwicklung.
**Bestehende `.env` und Daten-Volumes erhalten.** Die ausführlichen Schritte für
bestehende und neue Installationen stehen in [DEVELOPMENT](docs/DEVELOPMENT.md).

```powershell
Set-Location E:\repo\job-platform
# Nach Einrichtung der .env und Übergabe der vorhandenen Container:
docker compose up -d --build
npm ci --prefix apps/web
npm run dev --prefix apps/web
# Zweites Terminal:
node --env-file=.env scripts/crawl-greenhouse.mjs elastic
```

Web: http://localhost:3000 · API: http://127.0.0.1:8080/api/health
PostgreSQL: 127.0.0.1:5433 · Redis: 127.0.0.1:6379.

## Struktur

```text
apps/web/             Next.js, UI-Komponenten, API-Client, Node-Tests
services/api/         Java-Monolith, Maven Wrapper, Flyway, Integrationstests
services/crawler/     Go-Connector, CLI, Tests und Upstream-MIT-Lizenz
scripts/              Crawl-/Watcher-Orchestrierung, nativer Java-Start
licenses/             Zusätzliche Pflicht-Lizenztexte
docs/                 Audit, Architektur, Entscheidungen, Start und Testnachweise
.github/workflows/    CI für Java, Frontend und Go
```

## Funktionsumfang

Jobsuche, stabile Pagination, Jobkarten und Detailansicht mit Link zum Arbeitgeber;
Lade-, Leer- und Fehlerzustände. Java liefert bestehende Jobs unverändert über die
API. Greenhouse wird mit stabilen Provider-IDs importiert, URL-Tracking erzeugt keine
Duplikate. Geschützter Import, Transaktion, Quellen-Sperren und Importzählwerte.
407 vorhandene Elastic-Stellen blieben beim Umbau erhalten.

Fehlende Jobs werden nicht automatisch geschlossen. Detailseiten zeigen noch keine
importierten Vollbeschreibungen. Weitere ATS-Systeme, Benutzerkonten, Matching,
Typesense und produktive Cloud-Infrastruktur sind nicht implementiert.

## Tests

```powershell
Push-Location services/api
./mvnw.cmd -B verify
Pop-Location
npm run lint --prefix apps/web
npm run typecheck --prefix apps/web
npm test --prefix apps/web
npm run build --prefix apps/web
Push-Location services/crawler
go test ./...
Pop-Location
```

Siehe [Testnachweise](docs/VALIDATION.md), [Architektur/API](docs/ARCHITECTURE.md),
[Audit](docs/AUDIT.md), [Entscheidungen](docs/DECISIONS.md) und
[Drittanbieterhinweise](THIRD_PARTY_NOTICES.md).

## Nächste Schritte

Quellenverwaltung und Qualitätsmetriken ausbauen; fehlende Beobachtungen pro Job
modellieren, bevor automatische Schließungen aktiviert werden; weitere Connectoren
mit Vertragsprüfungen ergänzen. Vor Veröffentlichung Nutzungsrechte der Stellen,
Redis-Lizenz und vollständige Distributionspflichten prüfen; öffentliche Authentifizierung,
Rate-Limits und Deployment-Härtung ergänzen. Kein Produktionsbetrieb zugesichert.

## GitHub-Vorbereitung

Repository lokal initialisiert, keine Remotes gesetzt, kein Commit und kein Push.
Secrets, Dumps, Upstream-Kopie, Archive und Build-Ausgaben werden ignoriert.
Lizenz für eigenen Code vor Veröffentlichung bewusst wählen.
Empfohlene Initial-Commit-Message: `feat: establish Inreach foundation`.
