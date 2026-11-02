import type { ReactNode } from "react";
import "./globals.css";

export const metadata = {
  title: "Inreach | Dein nächster Schritt",
  description: "Entdecke Stellen direkt bei Unternehmen.",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="de">
      <body>{children}</body>
    </html>
  );
}
