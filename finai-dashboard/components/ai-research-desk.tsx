"use client";

import { useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Skeleton } from "@/components/ui/skeleton";
import { generateReport } from "@/lib/api";
import { InsightReport } from "@/lib/types";
import { cn, formatPercent } from "@/lib/utils";
import { BrainCircuit, FileText, Loader2, Sparkles, User } from "lucide-react";
import { ErrorAlert } from "./error-alert";
import { MarkdownReader } from "./markdown-reader";
import { PipelineStep, TelemetryPipeline } from "./telemetry-pipeline";

const STEP_ORDER: PipelineStep[] = [
  "FETCHING_PORTFOLIO",
  "RETRIEVING_QUOTES",
  "QUERYING_RAG",
  "GENERATOR_DRAFTING",
  "EVALUATOR_AUDITING",
];

interface AIResearchDeskProps {
  userId: number;
  username?: string;
}

export function AIResearchDesk({ userId, username }: AIResearchDeskProps) {
  const [activeUserId, setActiveUserId] = useState<string>(String(userId));
  const [activeUsername, setActiveUsername] = useState<string>(username || "");
  const [activeStep, setActiveStep] = useState<PipelineStep | null>(null);
  const [loading, setLoading] = useState(false);
  const [report, setReport] = useState<InsightReport | null>(null);
  const [error, setError] = useState<string | null>(null);
  const progressRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (progressRef.current) clearInterval(progressRef.current);
    };
  }, []);

  const advanceStep = (index: number) => {
    if (index >= STEP_ORDER.length) {
      setActiveStep("EVALUATOR_AUDITING");
      return;
    }
    setActiveStep(STEP_ORDER[index]);
    progressRef.current = setTimeout(() => advanceStep(index + 1), 1200);
  };

  const handleGenerate = async () => {
    const id = parseInt(activeUserId, 10);
    if (Number.isNaN(id) || id <= 0) {
      setError("Please enter a valid user ID");
      return;
    }
    setLoading(true);
    setError(null);
    setReport(null);
    setActiveStep("FETCHING_PORTFOLIO");
    progressRef.current = setTimeout(() => advanceStep(1), 1200);

    try {
      const data = await generateReport(id, activeUsername || undefined);
      if (progressRef.current) clearTimeout(progressRef.current);
      setReport(data);
      setActiveStep("COMPLETED");
    } catch (e) {
      if (progressRef.current) clearTimeout(progressRef.current);
      setError(e instanceof Error ? e.message : "Report generation failed");
      setActiveStep("FAILED");
    } finally {
      setLoading(false);
    }
  };

  const confidence = Number(report?.confidenceScore ?? 0);
  const recommendations = report?.recommendations || "";

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">AI Research Desk</h2>
        <p className="text-muted-foreground">
          Evaluator-Optimizer agentic pipeline for validated wealth reports.
        </p>
      </div>

      <Card className="overflow-hidden border-emerald-500/10 bg-gradient-to-br from-card to-card/80">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BrainCircuit className="h-5 w-5 text-emerald-400" />
            Agentic Report Engine
          </CardTitle>
          <CardDescription>
            Runs portfolio retrieval → live quotes → RAG retrieval → generator drafting → compliance
            evaluation.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex flex-col gap-4 sm:flex-row">
            <div className="flex-1 space-y-1">
              <label className="text-xs font-medium text-muted-foreground">User ID</label>
              <div className="relative">
                <User className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  value={activeUserId}
                  onChange={(e) => setActiveUserId(e.target.value)}
                  className="pl-9"
                  placeholder="e.g. 1"
                  type="number"
                />
              </div>
            </div>
            <div className="flex-1 space-y-1">
              <label className="text-xs font-medium text-muted-foreground">Username (optional)</label>
              <Input
                value={activeUsername}
                onChange={(e) => setActiveUsername(e.target.value)}
                placeholder="e.g. john_doe"
              />
            </div>
            <div className="flex items-end">
              <Button
                onClick={handleGenerate}
                disabled={loading}
                size="lg"
                className="gap-2 bg-gradient-to-r from-emerald-500 to-teal-600 text-white"
              >
                {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Sparkles className="h-4 w-4" />}
                Generate Agentic Wealth Report
              </Button>
            </div>
          </div>

          {loading && (
            <div className="space-y-4 rounded-lg border bg-muted/30 p-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Pipeline running</span>
                <Badge variant="amber" className="animate-pulse">
                  {activeStep?.replace(/_/g, " ")}
                </Badge>
              </div>
              <Progress value={(STEP_ORDER.indexOf(activeStep || "FETCHING_PORTFOLIO") + 1) * 20} />
            </div>
          )}

          {activeStep && (
            <div className="rounded-lg border bg-card/50 p-4">
              <TelemetryPipeline activeStep={activeStep} />
            </div>
          )}

          {error && <ErrorAlert message={error} onDismiss={() => setError(null)} />}
        </CardContent>
      </Card>

      {loading && !report && !error && (
        <Card>
          <CardHeader>
            <Skeleton className="h-6 w-48" />
            <Skeleton className="h-4 w-72" />
          </CardHeader>
          <CardContent className="space-y-3">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-5/6" />
            <Skeleton className="h-4 w-4/6" />
            <Skeleton className="h-32 w-full" />
          </CardContent>
        </Card>
      )}

      {report && (
        <Card className="overflow-hidden border-emerald-500/20">
          <CardHeader className="border-b bg-card/50">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <CardTitle className="flex items-center gap-2 text-xl">
                  <FileText className="h-5 w-5 text-emerald-400" />
                  Validated Wealth Report
                </CardTitle>
                <CardDescription>
                  Generated at {new Date(report.createdAt || Date.now()).toLocaleString()} ·{" "}
                  {report.iterationCount ?? 1} iteration{(report.iterationCount ?? 1) > 1 ? "s" : ""}
                </CardDescription>
              </div>
              <div className="text-right">
                <div className="text-xs uppercase tracking-wider text-muted-foreground">Confidence Score</div>
                <div
                  className={cn(
                    "text-3xl font-bold",
                    confidence >= 0.8 ? "text-emerald-400" : confidence >= 0.6 ? "text-amber-400" : "text-rose-400"
                  )}
                >
                  {formatPercent(confidence * 100)}
                </div>
                <Progress value={confidence * 100} max={100} variant={confidence >= 0.8 ? "emerald" : "amber"} className="mt-2 w-40" />
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-6 p-6">
            {report.summary && (
              <section>
                <h3 className="mb-2 text-sm font-semibold uppercase tracking-wider text-emerald-400">Executive Summary</h3>
                <div className="rounded-lg bg-muted/30 p-4 text-sm leading-relaxed text-foreground/90">
                  {report.summary}
                </div>
              </section>
            )}

            {recommendations && (
              <section>
                <h3 className="mb-2 text-sm font-semibold uppercase tracking-wider text-emerald-400">Structured Recommendations</h3>
                <div className="rounded-lg border border-emerald-500/10 bg-emerald-500/5 p-4">
                  <MarkdownReader content={recommendations} />
                </div>
              </section>
            )}

            {(report.riskAnalysis || report.marketOutlook) && (
              <section className="grid gap-4 md:grid-cols-2">
                {report.riskAnalysis && (
                  <div className="rounded-lg border border-rose-500/10 bg-rose-500/5 p-4">
                    <h3 className="mb-2 text-sm font-semibold uppercase tracking-wider text-rose-400">Risk Analysis</h3>
                    <p className="text-sm text-foreground/90">{report.riskAnalysis}</p>
                  </div>
                )}
                {report.marketOutlook && (
                  <div className="rounded-lg border border-amber-500/10 bg-amber-500/5 p-4">
                    <h3 className="mb-2 text-sm font-semibold uppercase tracking-wider text-amber-400">Market Outlook</h3>
                    <p className="text-sm text-foreground/90">{report.marketOutlook}</p>
                  </div>
                )}
              </section>
            )}

            {report.reportContent && (
              <section>
                <h3 className="mb-3 text-sm font-semibold uppercase tracking-wider text-emerald-400">Full Report</h3>
                <div className="rounded-lg border bg-card/50 p-5">
                  <MarkdownReader content={report.reportContent} />
                </div>
              </section>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
