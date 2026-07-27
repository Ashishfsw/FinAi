"use client";

import { useState } from "react";
import { DashboardOverview } from "@/components/dashboard-overview";
import { MobileNav } from "@/components/mobile-nav";
import { PortfolioOverview } from "@/components/portfolio-overview";
import { Sidebar, View } from "@/components/sidebar";
import { MarketWatch } from "@/components/market-watch";
import { AIResearchDesk } from "@/components/ai-research-desk";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { LoginCard } from "@/components/login-card";
import { useAuth } from "@/lib/auth-context";
import { LogOut, User } from "lucide-react";

export default function HomePage() {
  const { isAuthenticated, isLoading, username, logout } = useAuth();
  const [activeView, setActiveView] = useState<View>("overview");
  const [userId, setUserId] = useState<string>("1");
  const numericUserId = parseInt(userId, 10) || 1;

  if (isLoading) {
    return <div className="flex min-h-screen items-center justify-center bg-background" />;
  }

  if (!isAuthenticated) {
    return <LoginCard />;
  }

  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar active={activeView} onChange={setActiveView} />
      <div className="flex flex-1 flex-col">
        <MobileNav active={activeView} onChange={setActiveView} />
        <header className="sticky top-0 z-20 border-b bg-card/80 px-6 py-4 backdrop-blur">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-xl font-bold tracking-tight">Agentic Wealth Dashboard</h1>
              <p className="text-sm text-muted-foreground">
                Real-time portfolio intelligence powered by FinAI microservices.
              </p>
            </div>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm">
                <User className="h-4 w-4 text-emerald-400" />
                <span className="text-muted-foreground">{username ?? "Investor"}</span>
              </div>
              <label htmlFor="userId" className="text-sm text-muted-foreground">
                User ID
              </label>
              <Input
                id="userId"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && e.currentTarget.blur()}
                className="h-9 w-24"
                type="number"
                min={1}
              />
              <Button variant="outline" size="icon" onClick={logout} title="Sign out">
                <LogOut className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </header>
        <main className="flex-1 p-6">
          {activeView === "overview" && (
            <DashboardOverview
              userId={numericUserId}
              onNavigate={(view) => setActiveView(view)}
              onUserCreated={(user) => user.id && setUserId(String(user.id))}
            />
          )}
          {activeView === "portfolio" && <PortfolioOverview userId={numericUserId} />}
          {activeView === "market" && <MarketWatch />}
          {activeView === "research" && <AIResearchDesk userId={numericUserId} />}
        </main>
      </div>
    </div>
  );
}
