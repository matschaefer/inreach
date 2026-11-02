# Decisions

1. Java 25 + Spring Boot 4.1.2 is the central backend; no parallel C# implementation.
   Stable version verified against https://docs.spring.io/spring-boot/system-requirements.html .
2. Adopt the existing database schema, not a replacement. Explicit legacy external
   Docker volume names preserve data. Never run `docker compose down -v`.
3. Only the dependency-free MIT HTTP Greenhouse module is incorporated with notices;
   no upstream dataset or browser components.
4. Import normalized provider IDs through authenticated Java ingestion. Retain prior
   raw payload and first_seen_at; lifecycle closing stays disabled.
5. Minimal server-rendered frontend; no accounts, matching, Typesense or cloud.
6. Archive superseded work locally and ignore it. Initialize Git only, with no
   remote, commit or push. Choose an original-code license with the owner before release.
7. No automatic fallback to Node or C# persistence if Java fails. Surface unavailable
   states instead of serving inconsistent data.

8. JUnit 5.14.1 is explicit. Spring Framework 7.1 SpringExtension requires JUnit 6 APIs. Integration tests therefore bootstrap the real Spring application and PostgreSQL Testcontainer directly with JUnit 5 lifecycle hooks instead of using SpringExtension.
