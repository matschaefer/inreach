// Server-side API client. Persistence and business rules belong to Java.
export type Job = {
  id: string;
  title: string;
  source_url: string;
  provider: string;
  board_token: string;
  external_job_id: string;
  last_seen_at: string;
};

export type JobSearch = {
  jobs: Job[];
  total: number;
  page: number;
  pages: number;
  query: string;
  pageSize: number;
};

export type JobStats = {
  active: number;
  sources: number;
  last_import: string | null;
};

const API_BASE = process.env.INREACH_API_BASE_URL ?? "http://127.0.0.1:8080";

async function backendGet<T>(path: string): Promise<T> {
  const response = await fetch(new URL(path, API_BASE), {
    cache: "no-store",
    signal: AbortSignal.timeout(10_000),
  });
  if (!response.ok) {
    throw new Error(`Inreach API antwortet mit HTTP ${response.status}`);
  }
  return (await response.json()) as T;
}

export function cleanQuery(query: unknown): string {
  return typeof query === "string" ? query.trim().slice(0, 120) : "";
}
export function cleanPage(page: unknown): number {
  const parsed = Number(typeof page === "string" ? page : "1");
  return Number.isSafeInteger(parsed) && parsed >= 1 && parsed <= 10000
    ? parsed
    : 1;
}
export async function searchJobs(
  search: string,
  pageNumber: number,
): Promise<JobSearch> {
  const params = new URLSearchParams({
    q: cleanQuery(search),
    page: String(cleanPage(String(pageNumber))),
  });
  return backendGet<JobSearch>(`/api/jobs?${params.toString()}`);
}
export async function getJobStats(): Promise<JobStats> {
  return backendGet<JobStats>("/api/jobs/stats");
}
export function safeJobUrl(sourceUrl: string): string | null {
  try {
    const url = new URL(sourceUrl);
    return !url.username && !url.password && (url.protocol === "https:" || url.protocol === "http:")
      ? url.href
      : null;
  } catch {
    return null;
  }
}

export function getJob(id: string): Promise<Job> {
  return backendGet<Job>(`/api/jobs/${encodeURIComponent(id)}`);
}
