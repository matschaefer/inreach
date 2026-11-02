import Link from "next/link";
import type { Job } from "@/lib/jobs";
export function JobCard({ job }: { job: Job }) {
  return (
    <article className="job-card">
      <div className="job-topline">
        <span className="source-tag">{job.board_token}</span>
        <span className="source-label">Direkt vom Unternehmen</span>
      </div>
      <h3>
        <Link href={`/jobs/${job.id}`}>{job.title}</Link>
      </h3>
      <div className="job-footer">
        <span>Entdecke deine nächste Möglichkeit</span>
        <Link className="job-link" href={`/jobs/${job.id}`}>
          Details ansehen ↗
        </Link>
      </div>
    </article>
  );
}
