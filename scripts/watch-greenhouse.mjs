import { fileURLToPath } from "node:url";
import { spawn } from "node:child_process";
const interval = Number(process.env.CRAWL_INTERVAL_MINUTES ?? 360);
if (!Number.isFinite(interval) || interval < 15 || interval > 1440)
  throw new Error("Intervall muss 15–1440 Minuten betragen");
let child,
  timer,
  stopped = false;
function run() {
  child = spawn(
    process.execPath,
    [
      fileURLToPath(new URL("./crawl-greenhouse.mjs", import.meta.url)),
      process.argv[2] ?? "elastic",
    ],
    { stdio: "inherit", windowsHide: true },
  );
  child.on("error", () => console.error("Crawler konnte nicht starten"));
  child.on("close", () => {
    if (!stopped) timer = setTimeout(run, interval * 60000);
  });
}
for (const signal of ["SIGINT", "SIGTERM"])
  process.on(signal, () => {
    stopped = true;
    clearTimeout(timer);
    child?.kill();
  });
run();
