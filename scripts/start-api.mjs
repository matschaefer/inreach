import { spawn } from "node:child_process";
const child = spawn(
  process.env.JAVA ?? "java",
  [
    "-jar",
    new URL(
      "../services/api/target/inreach-api-0.1.0-SNAPSHOT.jar",
      import.meta.url,
    ).pathname.replace(/^\/(\w:)/, "$1"),
  ],
  { stdio: "inherit", windowsHide: true },
);
child.on("exit", (code) => (process.exitCode = code ?? 1));
child.on("error", () => {
  console.error("Java 25 oder neuer erforderlich; zuerst Backend bauen.");
  process.exitCode = 1;
});
for (const signal of ["SIGINT", "SIGTERM"])
  process.on(signal, () => child.kill());
