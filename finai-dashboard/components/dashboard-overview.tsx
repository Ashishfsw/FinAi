"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { getPortfolioSummary } from "@/lib/api";
import { PortfolioSummary, User } from "@/lib/types";
import { formatCurrency, formatPercent, cn } from "@/lib/utils";
import { ArrowRight, BarChart3, BrainCircuit, TrendingUp, Wallet } from "lucide-react";
import { ErrorAlert } from "./error-alert";
import { CreateUserCard } from "./create-user-card";

interface DashboardOverviewProps {
  userId: number;
  onNavigate: (view: "portfolio" | "market" | "research") => void;
  onUserCreated?: (user: User) => void;
}

export function DashboardOverview({ userId, onNavigate, onUserCreated }: DashboardOverviewProps) {
  const [summary, setSummary] = useState<PortfolioSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    getPortfolioSummary(userId)
      .then(setSummary)
      .catch((e) => setError(e instanceof Error ? e.message : "Failed to load dashboard"))
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading) return <OverviewSkeleton />;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Dashboard Overview</h2>
        <p className="text-muted-foreground">Your FinAI command center at a glance.</p>
      </div>

      {error && <ErrorAlert message={error} onDismiss={() => setError(null)} />}

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Current Value"
          value={formatCurrency(summary?.totalCurrentValue)}
          subtitle={formatPercent(summary?.totalUnrealizedPnLPercentage)}
          icon={Wallet}
          positive={Number(summary?.totalUnrealizedPnLPercentage ?? 0) >= 0}
        />
        <StatCard
          title="Unrealized P&L"
          value={formatCurrency(summary?.totalUnrealizedPnL)}
          subtitle={formatPercent(summary?.totalUnrealizedPnLPercentage)}
          icon={TrendingUp}
          positive={Number(summary?.totalUnrealizedPnL ?? 0) >= 0}
        />
        <StatCard
          title="Holdings"
          value={String(summary?.totalHoldings ?? 0)}
          subtitle={`Risk: ${summary?.riskProfile ?? "—"}`}
          icon={BarChart3}
          positive
        />
        <StatCard
          title="AI Reports"
          value="Ready"
          subtitle="Evaluator-Optimizer enabled"
          icon={BrainCircuit}
          positive
          variant="gradient"
        />
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Jump into analytics, market data, or agentic research.</CardDescription>
          </CardHeader>
          <CardContent className="grid gap-3 sm:grid-cols-3">
            <ActionButton icon={BarChart3} label="Portfolio Analytics" onClick={() => onNavigate("portfolio")} />
            <ActionButton icon={TrendingUp} label="Market Watch" onClick={() => onNavigate("market")} />
            <ActionButton icon={BrainCircuit} label="AI Research Desk" onClick={() => onNavigate("research")} />
          </CardContent>
        </Card>

        <CreateUserCard onUserCreated={onUserCreated} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>System Health</CardTitle>
          <CardDescription>Backend microservices status</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-3 text-sm sm:grid-cols-2 lg:grid-cols-4">
          <HealthRow label="API Gateway" status={error ? "warn" : "ok"} port={8080} />
          <HealthRow label="Portfolio Service" status={error ? "warn" : "ok"} port={8081} />
          <HealthRow label="Market Data Service" status="ok" port={8082} />
          <HealthRow label="Insights Service" status="ok" port={8083} />
        </CardContent>
      </Card>
    </div>
  );
}

function StatCard({
  title,
  value,
  subtitle,
  icon: Icon,
  positive,
  variant = "default",
}: {
  title: string;
  value: string;
  subtitle: string;
  icon: React.ElementType;
  positive: boolean;
  variant?: "default" | "gradient";
}) {
  return (
    <Card>
      <CardContent className="pt-6">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <p className="text-sm text-muted-foreground">{title}</p>
            <p className="text-2xl font-bold tracking-tight">{value}</p>
            <p
              className={cn(
                "text-xs font-medium",
                positive ? "text-emerald-400" : "text-rose-400"
              )}
            >
              {subtitle}
            </p>
          </div>
          <div
            className={cn(
              "flex h-10 w-10 items-center justify-center rounded-full",
              variant === "gradient"
                ? "bg-gradient-to-br from-emerald-400 to-teal-600 text-zinc-900"
                : positive
                ? "bg-emerald-500/10 text-emerald-400"
                : "bg-rose-500/10 text-rose-400"
            )}
          >
            <Icon className="h-5 w-5" />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function ActionButton({
  icon: Icon,
  label,
  onClick,
}: {
  icon: React.ElementType;
  label: string;
  onClick: () => void;
}) {
  return (
    <Button variant="outline" className="h-auto justify-between py-4" onClick={onClick}>
      <span className="flex items-center gap-2">
        <Icon className="h-4 w-4 text-emerald-400" />
        {label}
      </span>
      <ArrowRight className="h-4 w-4 text-muted-foreground" />
    </Button>
  );
}

function HealthRow({ label, status, port }: { label: string; status: "ok" | "warn" | "error"; port: number }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-muted-foreground">{label}</span>
      <div className="flex items-center gap-2">
        <span className="text-xs text-muted-foreground">:{port}</span>
        <span
          className={cn(
            "h-2.5 w-2.5 rounded-full",
            status === "ok" ? "bg-emerald-500" : status === "warn" ? "bg-amber-500" : "bg-rose-500"
          )}
        />
      </div>
    </div>
  );
}

function OverviewSkeleton() {
  return (
    <div className="space-y-6">
      <Skeleton className="h-8 w-56" />
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Skeleton className="h-28" />
        <Skeleton className="h-28" />
        <Skeleton className="h-28" />
        <Skeleton className="h-28" />
      </div>
      <div className="grid gap-4 lg:grid-cols-3">
        <Skeleton className="h-48 lg:col-span-2" />
        <Skeleton className="h-48" />
      </div>
    </div>
  );
}
