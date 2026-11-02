import Link from "next/link";
export default function NotFound() {
  return (
    <main className="site-shell hero">
      <h1>Stelle nicht gefunden</h1>
      <Link href="/">Alle Stellen entdecken</Link>
    </main>
  );
}
