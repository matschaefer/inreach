import { Button } from "@/components/ui/button";
import Link from "next/link";
import { JobCard } from "@/components/job-card";
import type { Metadata } from "next";
import { getJobStats, searchJobs } from "@/lib/jobs";

export const metadata: Metadata = {
  title: "Jobs | Inreach",
  description: "Jobangebote direkt von den Karriereseiten der Unternehmen",
};

export const dynamic = "force-dynamic";

type SearchParams = Promise<{ q?: string; page?: string }>;

function shortDate(value: string | null): string {
  if (!value) return "–";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "–";
  return new Intl.DateTimeFormat("de-DE", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "Europe/Berlin",
  }).format(date);
}

function pageHref(query: string, page: number) {
  const params = new URLSearchParams();
  if (query) params.set("q", query);
  if (page > 1) params.set("page", String(page));
  const suffix = params.toString();
  return suffix ? `/?${suffix}` : "/";
}

export default async function Home({
  searchParams,
}: {
  searchParams: SearchParams;
}) {
  const params = await searchParams;
  const requestedQuery = typeof params.q === "string" ? params.q : "";
  const requestedPage = Number(params.page ?? "1");

  let data: Awaited<ReturnType<typeof searchJobs>> | null = null;
  let stats: Awaited<ReturnType<typeof getJobStats>> | null = null;
  let failed = false;
  try {
    [data, stats] = await Promise.all([
      searchJobs(requestedQuery, requestedPage),
      getJobStats(),
    ]);
  } catch (error) {
    failed = true;
    console.error("Jobs-Seite: Stellen gerade nicht verfügbar:", error);
  }

  return (
    <main className="site-shell">
      <header className="topbar">
        <Link className="brand" href="/" aria-label="Inreach Startseite">
          <span className="brand-mark">in</span>
          <span>inreach</span>
        </Link>
        <span className="environment-label">Dein nächster Schritt.</span>
      </header>

      <section className="hero">
        <div className="eyebrow">Direkt von Unternehmenskarriereseiten</div>
        <h1>
          Finde Jobs an der <span>Quelle.</span>
        </h1>
        <p>
          Entdecke neue Perspektiven. Aktuelle Stellen direkt von den
          Karriereseiten der Unternehmen.
        </p>
        <form method="GET" action="/" className="searchbar" role="search">
          <span className="search-icon" aria-hidden="true">
            ⌕
          </span>
          <input
            type="search"
            name="q"
            maxLength={120}
            placeholder="Jobtitel suchen, z. B. Security Engineer"
            defaultValue={data?.query ?? requestedQuery}
            aria-label="Stellen durchsuchen"
          />
          <Button type="submit">Suchen</Button>
        </form>
      </section>

      <section className="stats" aria-label="Datenbankstatistik">
        <div className="stat">
          <strong>{stats?.active.toLocaleString("de-DE") ?? "–"}</strong>
          <span>Aktive Stellen</span>
        </div>
        <div className="stat">
          <strong>{stats?.sources ?? "–"}</strong>
          <span>Überwachte Quellen</span>
        </div>
        <div className="stat stat-date">
          <strong>{shortDate(stats?.last_import ?? null)}</strong>
          <span>Letzter Import</span>
        </div>
      </section>

      <section className="list-section">
        <div className="list-heading">
          <div>
            <div className="eyebrow">DEINE MÖGLICHKEITEN</div>
            <h2>Stellenangebote</h2>
          </div>
          <span className="result-count">
            {data
              ? `${data.total.toLocaleString("de-DE")} Treffer`
              : "Gerade nicht verfügbar"}
          </span>
        </div>

        {failed ? (
          <div className="empty-state error-state">
            <h3>Stellen gerade nicht verfügbar</h3>
            <p>Bitte versuche es in wenigen Augenblicken erneut.</p>
          </div>
        ) : data?.jobs.length === 0 ? (
          <div className="empty-state">
            <h3>Keine passenden Stellen gefunden</h3>
            <p>Versuche einen allgemeineren Suchbegriff.</p>
            <Link href="/" className="reset-link">
              Alle Stellen anzeigen
            </Link>
          </div>
        ) : (
          <div className="jobs-list">
            {data?.jobs.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>
        )}

        {data && data.pages > 1 && (
          <nav className="pagination" aria-label="Seitennavigation">
            {data.page > 1 ? (
              <a href={pageHref(data.query, data.page - 1)}>← Zurück</a>
            ) : (
              <span className="disabled-page">← Zurück</span>
            )}
            <span>
              Seite {data.page} von {data.pages}
            </span>
            {data.page < data.pages ? (
              <a href={pageHref(data.query, data.page + 1)}>Weiter →</a>
            ) : (
              <span className="disabled-page">Weiter →</span>
            )}
          </nav>
        )}
      </section>
      <footer className="site-footer">
        Inreach · Neue Perspektiven beginnen hier.
      </footer>
    </main>
  );
}
