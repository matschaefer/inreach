import { NextResponse } from "next/server";
import { searchJobs } from "@/lib/jobs";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const query = url.searchParams.get("q") ?? "";
  const page = Number(url.searchParams.get("page") ?? "1");

  try {
    const results = await searchJobs(query, page);
    return NextResponse.json(results, {
      headers: { "Cache-Control": "no-store" },
    });
  } catch (error) {
    console.error("GET /api/jobs failed:", error);
    return NextResponse.json(
      { error: "Stellen vorübergehend nicht verfügbar." },
      { status: 503 },
    );
  }
}
