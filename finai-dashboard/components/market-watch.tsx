"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { getStockQuote, ingestNews } from "@/lib/api";
import { StockQuote } from "@/lib/types";
import { cn, formatCurrency, formatPercent } from "@/lib/utils";
import { Activity, Database, Loader2, RefreshCw, Search, TrendingDown, TrendingUp } from "lucide-react";
import { ErrorAlert } from "./error-alert";

export function MarketWatch() {
  const [symbol, setSymbol] = useState("AAPL");
  const [quote, setQuote] = useState<StockQuote | null>(null);
  const [loadingPrice, setLoadingPrice] = useState(false);
  const [ingesting, setIngesting] = useState(false);
  const [ingestMessage, setIngestMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleLookup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!symbol.trim()) return;
    setLoadingPrice(true);
    setError(null);
    setQuote(null);
    try {
      const data = await getStockQuote(symbol.trim());
      setQuote(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Price lookup failed");
    } finally {
      setLoadingPrice(false);
    }
  };

  const handleIngest = async () => {
    if (!symbol.trim()) return;
    setIngesting(true);
    setIngestMessage(null);
    setError(null);
    try {
      const msg = await ingestNews(symbol.trim());
      setIngestMessage(msg);
    } catch (e) {
      setError(e instanceof Error ? e.message : "RAG ingestion failed");
    } finally {
      setIngesting(false);
    }
  };

  const price = quote?.price ?? quote?.avPrice;
  const change = quote?.change ?? quote?.avChange;
  const changePct = quote?.changePercent ?? quote?.avChangePercent;
  const positive = Number(change ?? 0) >= 0;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Live Market Streamer</h2>
        <p className="text-muted-foreground">Query live tickers and ingest financial news into the PGVector RAG store.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="h-5 w-5 text-emerald-400" />
            Ticker Lookup
          </CardTitle>
          <CardDescription>Enter a symbol to fetch the latest cached market price.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <form onSubmit={handleLookup} className="flex gap-2">
            <Input
              value={symbol}
              onChange={(e) => setSymbol(e.target.value.toUpperCase())}
              placeholder="e.g. AAPL"
              className="max-w-xs uppercase"
            />
            <Button type="submit" disabled={loadingPrice}>
              {loadingPrice ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Search className="mr-2 h-4 w-4" />}
              Lookup
            </Button>
          </form>

          {loadingPrice && (
            <div className="space-y-3">
              <Skeleton className="h-8 w-48" />
              <Skeleton className="h-4 w-32" />
            </div>
          )}

          {!loadingPrice && quote && (
            <div className="rounded-lg border bg-card/50 p-4">
              <div className="flex flex-wrap items-center gap-3">
                <div className="text-3xl font-bold">{formatCurrency(price)}</div>
                <Badge variant={positive ? "emerald" : "rose"} className="text-xs">
                  {positive ? <TrendingUp className="mr-1 h-3 w-3" /> : <TrendingDown className="mr-1 h-3 w-3" />}
                  {formatCurrency(change)} ({formatPercent(changePct)})
                </Badge>
              </div>
              <div className="mt-2 text-sm text-muted-foreground">
                {quote.name || quote.symbol} · {quote.exchange || "NYSE/NASDAQ"}
              </div>
              <div className="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
                <Metric label="Open" value={formatCurrency(quote.open ?? quote.avOpen)} />
                <Metric label="High" value={formatCurrency(quote.high ?? quote.avHigh)} />
                <Metric label="Low" value={formatCurrency(quote.low ?? quote.avLow)} />
                <Metric label="Prev Close" value={formatCurrency(quote.previousClose ?? quote.avPreviousClose)} />
              </div>
              <div className="mt-4">
                <Button
                  variant="secondary"
                  onClick={handleIngest}
                  disabled={ingesting}
                  className="gap-2"
                >
                  {ingesting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Database className="h-4 w-4" />}
                  Ingest Financial News into RAG
                </Button>
              </div>
            </div>
          )}

          {!loadingPrice && !quote && !error && (
            <div className="rounded-lg border border-dashed p-8 text-center text-sm text-muted-foreground">
              <RefreshCw className="mx-auto mb-2 h-6 w-6 opacity-50" />
              Enter a symbol and click Lookup to see live market data.
            </div>
          )}

          {ingestMessage && (
            <Alert variant="success">
              <Database className="h-4 w-4" />
              <AlertTitle>RAG ingestion initiated</AlertTitle>
              <AlertDescription>{ingestMessage}</AlertDescription>
            </Alert>
          )}

          {error && <ErrorAlert message={error} onDismiss={() => setError(null)} />}
        </CardContent>
      </Card>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md bg-muted/40 p-2">
      <div className="text-xs text-muted-foreground">{label}</div>
      <div className={cn("font-semibold", value === "$—" && "text-muted-foreground")}>{value}</div>
    </div>
  );
}
