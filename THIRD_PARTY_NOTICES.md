# Third-party notices

Inreach contains a **derived** HTTP Greenhouse monitor from
https://github.com/colophon-group/jobseek at revision
`a515fb91381a0f836ac5f8ea690fc100cabb9839`.
Copyright (c) 2025 Viktor Shcherbakov. MIT license in
`services/crawler/LICENSE-UPSTREAM` accompanies all incorporated source.
Files: `services/crawler/greenhouse/greenhouse.go`, `live.go`, `greenhouse_test.go`.
Inreach modifications: provider IDs, completeness verification, transparent user agent;
new CLI/Java integration, regression tests and independent build layout.
Renaming and refactoring do not remove the derivation or copyright obligations.

The Go module has **no third-party Go dependencies**. No upstream data collection,
company catalogue, browser renderer, Lightpanda component, Python pipeline or
nested upstream history is shipped. Local `upstream/` remains an ignored reference.
The upstream `LICENSE-JOB-DATA` is separate from the MIT source license and does
not grant an unrestricted right to redistribute its collection. No such data is included.

`apps/web/src/components/ui/button.tsx` adapts the shadcn/ui Button pattern.
Copyright (c) 2023 shadcn, MIT; full text: `licenses/shadcn-ui-MIT.txt`.
Source: https://github.com/shadcn-ui/ui .

## Runtime and development dependencies

Frontend: Next.js, React, React DOM, Tailwind CSS, Radix Slot,
class-variance-authority, clsx and tailwind-merge use MIT licenses.
TypeScript is Apache-2.0; ESLint and its plugins carry their own package notices.
The exact transitive versions and notices are retained by npm in package-lock.json
and the installed dependency packages; do not strip notices when distributing bundles.

Backend: Spring Boot / Spring Framework, Flyway Community, Maven Wrapper,
Jackson and Testcontainers use Apache-2.0; Hibernate ORM 7.4 uses Apache-2.0 (since 7.0.0.Beta5);
PostgreSQL JDBC uses BSD-2-Clause; JUnit uses EPL-2.0. Their transitive dependencies
retain separate licenses. The Maven dependency tree is reproducible with `./mvnw dependency:tree`.

Infrastructure: PostgreSQL uses the PostgreSQL License. Redis 7.4 is under
RSALv2 / SSPLv1 alternatives, **not the old Redis BSD license**. Redis is optional
in this foundation and unused by application code. Review the selected license
before offering hosted Redis functionality or redistributing it. Docker images and
JDK distributions contain additional notices; no container layers are committed.

This inventory identifies obligations, not a completed legal review of every
transitive distribution artifact. Recheck notices and produce a distribution SBOM
before releasing binaries or operating a public service.

## Job content

Existing local Elastic jobs and fresh Greenhouse responses are retained only in the
local database. Direct availability does not establish redistribution rights. Review
provider terms, privacy, retention, robots/TDM policy and company permissions before
public deployment. The crawler respects TDM reservation and does not bypass blocks.

License sources: https://hibernate.org/orm/ and https://redis.io/legal/licenses/ .
