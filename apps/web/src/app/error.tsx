"use client";
export default function ErrorPage({ reset }: { reset: () => void }) {
  return (
    <main className="site-shell hero">
      <h1>Gerade nicht erreichbar</h1>
      <p>Bitte versuche es erneut.</p>
      <button onClick={reset}>Erneut laden</button>
    </main>
  );
}
