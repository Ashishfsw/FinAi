import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/lib/auth-context";

export const metadata: Metadata = {
  title: "FinAI — Agentic Wealth Dashboard",
  description: "Financial dashboard for the FinAI microservices platform",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
