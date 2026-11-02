import { Button } from "@/components/ui/button";
import Link from "next/link";
import { notFound } from "next/navigation";
import { getJob, safeJobUrl } from "@/lib/jobs";
export const dynamic = "force-dynamic";
export default async function Detail({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  if (!/^[1-9][0-9]*$/.test(id)) notFound();
  let job;
  try {
    job = await getJob(id);
  } catch (e) {
    if (e instanceof Error && e.message.includes("404")) notFound();
    throw e;
  }
  const url = safeJobUrl(job.source_url);
  return (
    <main className="site-shell">
      <header className="topbar">
        <Link className="brand" href="/">
          inreach
        </Link>
        <Link href="/">← Alle Stellen</Link>
      </header>
      <section className="hero">
        <p className="eyebrow">{job.board_token}</p>
        <h1>{job.title}</h1>
        <p>
          Alle Informationen zu dieser Position und zur Bewerbung findest du
          direkt beim Unternehmen.
        </p>
        {url && (
          <Button asChild>
            <a href={url} target="_blank" rel="noopener noreferrer">
              Beim Unternehmen bewerben ↗
            </a>
          </Button>
        )}
      </section>
    </main>
  );
}
