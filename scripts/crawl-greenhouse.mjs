import { mkdir, rmdir } from "node:fs/promises";
import { execFile } from "node:child_process";
import { promisify } from "node:util";
const board = process.argv[2] ?? "elastic";
if (!/^[a-zA-Z0-9_-]{2,80}$/.test(board))
  throw new Error("Ungültiger Board-Token");
if (!process.env.INREACH_INGESTION_TOKEN)
  throw new Error("INREACH_INGESTION_TOKEN fehlt");
const lock = new URL(`../.local/locks/${board}/`, import.meta.url);
await mkdir(new URL("../.local/locks/", import.meta.url), { recursive: true });
try {
  await mkdir(lock);
} catch {
  throw new Error(
    "Quelle ist bereits gesperrt; nach Absturz Sperre erst nach Prozessprüfung entfernen",
  );
}
try {
  const cwd = new URL("../services/crawler/", import.meta.url);
  const { stdout } = await promisify(execFile)(
    process.env.INREACH_GO ?? "go",
    ["run", "./cmd/crawl", "-board", board],
    { cwd, timeout: 120000, maxBuffer: 64 * 1024 * 1024, windowsHide: true },
  );
  const result = await fetch(
    new URL(
      "/internal/ingestion/greenhouse",
      process.env.INREACH_API_BASE_URL ?? "http://127.0.0.1:8080",
    ),
    {
      method: "POST",
      redirect: "error",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${process.env.INREACH_INGESTION_TOKEN}`,
      },
      body: stdout,
      signal: AbortSignal.timeout(30000),
    },
  );
  if (!result.ok)
    throw new Error(`Import fehlgeschlagen: HTTP ${result.status}`);
  console.log(await result.json());
} finally {
  await rmdir(lock);
}
