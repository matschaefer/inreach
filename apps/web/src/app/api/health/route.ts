import { NextResponse } from "next/server";

export function GET() {
  // Testet ausschliesslich den Next.js-Dienst, nicht Postgres oder den Crawler.
  return NextResponse.json({ service: "inreach-web", status: "ok" });
}
