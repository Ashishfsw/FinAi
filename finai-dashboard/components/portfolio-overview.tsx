"use client";

import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { getPortfolioSummary } from "@/lib/api";
import { AssetHolding, AssetType, PortfolioSummary } from "@/lib/types";
import { formatCurrency, formatPercent, cn } from "@/lib/utils";
import { AlertCircle, Briefcase, TrendingDown, TrendingUp, Wallet } from "lucide-react";
import { ErrorAlert } from "./error-alert";
import { AddHoldingCard } from "./add-holding-card";
import dynamic from "next/dynamic";

const PieChart = dynamic(() => import("recharts").then((m) => m.PieChart), { ssr: false });
const Pie = dynamic(() => import("recharts").then((m) => m.Pie), { ssr: false });
const Cell = dynamic(() => import("recharts").then((m) => m.Cell), { ssr: false });
const ResponsiveContainer = dynamic(() => import("recharts").then((m) => m.ResponsiveContainer), { ssr: false });
const Tooltip = dynamic(() => import("recharts").then((m) => m.Tooltip), { ssr: false });

const TYPE_COLORS: Record<AssetType, string> = {
  STOCK: "#10b981",
  BOND: "#3b82f6",
  ETF: "#8b5cf6",
  MUTUAL_FUND: "#f59e0b",
  CRYPTO: "#f43f5e",
  COMMODITY: "#14b8a6",
  CASH: "#64748b",
};

interface PortfolioOverviewProps {
  userId: number;
}

export function PortfolioOverview({ userId }: PortfolioOverviewProps) {
  const [summary, setSummary] = useState<PortfolioSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadSummary = async (id: number) => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getPortfolioSummary(id);
      setSummary(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load portfolio");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSummary(userId);
  }, [userId]);

  const allocationByType = useMemo(() => {
    if (!summary?.holdings) return [];
    const map = new Map<AssetType, number>();
    summary.holdings.forEach((h) => {
      const type = h.assetType;
      const value = Number(h.currentValue ?? 0);
      map.set(type, (map.get(type) ?? 0) + value);
    });
    return Array.from(map.entries())
      .filter(([, value]) => value > 0)
      .map(([type, value]) => ({ name: type.replace(/_/g, " "), type, value }));
  }, [summary]);

  const isPositive = Number(summary?.totalUnrealizedPnL ?? 0) >= 0;

  if (loading) return <PortfolioSkeleton />;

  if (error) {
    return (
      <div className="space-y-4">
        <h2 className="text-2xl font-bold tracking-tight">Portfolio Overview</h2>
        <ErrorAlert message={error} onDismiss={() => setError(null)} />
      </div>
    );
  }

  if (!summary) {
    return (
      <div className="space-y-4">
        <h2 className="text-2xl font-bold tracking-tight">Portfolio Overview</h2>
        <Card>
          <CardContent className="py-12 text-center text-muted-foreground">
            <p>No portfolio data available. Add holdings or choose a different user.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Portfolio Overview</h2>
          <p className="text-muted-foreground">
            {summary.fullName} · <span className="capitalize">{summary.riskProfile.toLowerCase()}</span> risk
          </p>
        </div>
        <Badge variant="outline" className="w-fit">
          <Briefcase className="mr-1 h-3 w-3" />
          {summary.totalHoldings} holdings
        </Badge>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <MetricCard
          title="Total Current Value"
          value={formatCurrency(summary.totalCurrentValue)}
          icon={Wallet}
          trend={formatPercent(summary.totalUnrealizedPnLPercentage)}
          positive={Number(summary.totalUnrealizedPnLPercentage ?? 0) >= 0}
        />
        <MetricCard
          title="Total Unrealized P&L"
          value={formatCurrency(summary.totalUnrealizedPnL)}
          icon={isPositive ? TrendingUp : TrendingDown}
          trend={formatPercent(summary.totalUnrealizedPnLPercentage)}
          positive={isPositive}
          accent={isPositive ? "emerald" : "rose"}
        />
        <MetricCard
          title="Total Invested"
          value={formatCurrency(summary.totalInvestmentValue)}
          icon={AlertCircle}
          trend="Cost basis"
          positive
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Asset Allocation</CardTitle>
            <CardDescription>Current mix by asset type</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-72">
              {allocationByType.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={allocationByType}
                      dataKey="value"
                      nameKey="name"
                      innerRadius={60}
                      outerRadius={100}
                      paddingAngle={3}
                    >
                      {allocationByType.map((entry) => (
                        <Cell key={entry.type} fill={TYPE_COLORS[entry.type]} stroke="transparent" />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "hsl(222 47% 8%)",
                        border: "1px solid hsl(217 33% 17%)",
                        borderRadius: "0.5rem",
                      }}
                      itemStyle={{ color: "hsl(210 40% 98%)" }}
                      formatter={(value: number) => formatCurrency(value)}
                    />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
                  No allocation data available
                </div>
              )}
            </div>
            <div className="mt-4 flex flex-wrap gap-3">
              {allocationByType.map((entry) => (
                <div key={entry.type} className="flex items-center gap-2 text-xs">
                  <span
                    className="h-3 w-3 rounded-full"
                    style={{ backgroundColor: TYPE_COLORS[entry.type] }}
                  />
                  <span className="text-muted-foreground">{entry.name}</span>
                  <span className="font-medium">{formatCurrency(entry.value)}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Holdings Performance</CardTitle>
            <CardDescription>Live P&L across positions</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ticker</TableHead>
                  <TableHead>Qty</TableHead>
                  <TableHead>Avg Cost</TableHead>
                  <TableHead>Price</TableHead>
                  <TableHead className="text-right">P&L</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {summary.holdings.map((holding) => (
                  <HoldingRow key={holding.id ?? holding.symbol} holding={holding} />
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

      <AddHoldingCard userId={userId} onAdded={() => loadSummary(userId)} />
      </div>
    </div>
  );
}

function HoldingRow({ holding }: { holding: AssetHolding }) {
  const pnl = Number(holding.unrealizedPnL ?? 0);
  const pct = Number(holding.unrealizedPnLPercentage ?? 0);
  const positive = pnl >= 0;
  return (
    <TableRow>
      <TableCell>
        <div className="font-medium">{holding.symbol}</div>
        <div className="text-xs text-muted-foreground">{holding.assetName}</div>
      </TableCell>
      <TableCell>{Number(holding.quantity).toFixed(2)}</TableCell>
      <TableCell>{formatCurrency(holding.averageCostPrice)}</TableCell>
      <TableCell>{formatCurrency(holding.currentPrice)}</TableCell>
      <TableCell className="text-right">
        <div className={cn("font-medium", positive ? "text-emerald-400" : "text-rose-400")}>
          {positive ? "+" : ""}
          {formatCurrency(holding.unrealizedPnL)}
        </div>
        <div className={cn("text-xs", positive ? "text-emerald-400/80" : "text-rose-400/80")}>
          {formatPercent(pct)}
        </div>
      </TableCell>
    </TableRow>
  );
}

function MetricCard({
  title,
  value,
  icon: Icon,
  trend,
  positive,
  accent = "emerald",
}: {
  title: string;
  value: string;
  icon: React.ElementType;
  trend: string;
  positive: boolean;
  accent?: "emerald" | "rose";
}) {
  return (
    <Card>
      <CardContent className="pt-6">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <p className="text-sm font-medium text-muted-foreground">{title}</p>
            <p className="text-3xl font-bold tracking-tight">{value}</p>
          </div>
          <div
            className={cn(
              "flex h-10 w-10 items-center justify-center rounded-full",
              accent === "rose" ? "bg-rose-500/10 text-rose-400" : "bg-emerald-500/10 text-emerald-400"
            )}
          >
            <Icon className="h-5 w-5" />
          </div>
        </div>
        <div
          className={cn(
            "mt-4 text-sm font-medium",
            positive ? "text-emerald-400" : "text-rose-400"
          )}
        >
          {trend}
        </div>
      </CardContent>
    </Card>
  );
}

function PortfolioSkeleton() {
  return (
    <div className="space-y-6">
      <Skeleton className="h-8 w-64" />
      <div className="grid gap-4 md:grid-cols-3">
        <Skeleton className="h-36" />
        <Skeleton className="h-36" />
        <Skeleton className="h-36" />
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <Skeleton className="h-96" />
        <Skeleton className="h-96" />
      </div>
    </div>
  );
}
