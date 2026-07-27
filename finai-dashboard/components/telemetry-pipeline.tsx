"use client";

import { CheckCircle2, Circle, Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

export type PipelineStep =
  | "FETCHING_PORTFOLIO"
  | "RETRIEVING_QUOTES"
  | "QUERYING_RAG"
  | "GENERATOR_DRAFTING"
  | "EVALUATOR_AUDITING"
  | "COMPLETED"
  | "FAILED";

const steps: { id: PipelineStep; label: string }[] = [
  { id: "FETCHING_PORTFOLIO", label: "FETCHING PORTFOLIO" },
  { id: "RETRIEVING_QUOTES", label: "RETRIEVING LIVE QUOTES" },
  { id: "QUERYING_RAG", label: "QUERYING PGVECTOR RAG" },
  { id: "GENERATOR_DRAFTING", label: "GENERATOR ANALYST DRAFTING" },
  { id: "EVALUATOR_AUDITING", label: "COMPLIANCE EVALUATOR AUDITING" },
];

interface TelemetryPipelineProps {
  activeStep: PipelineStep;
}

export function TelemetryPipeline({ activeStep }: TelemetryPipelineProps) {
  const activeIndex = steps.findIndex((s) => s.id === activeStep);

  return (
    <div className="relative">
      <div className="absolute left-4 top-0 bottom-0 w-px bg-gradient-to-b from-emerald-500/40 via-amber-500/40 to-rose-500/40" />
      <div className="space-y-6">
        {steps.map((step, index) => {
          const isCompleted = activeStep === "COMPLETED" || index < activeIndex;
          const isActive = step.id === activeStep && activeStep !== "COMPLETED";
          const isFailed = activeStep === "FAILED" && index === activeIndex;

          return (
            <div key={step.id} className="relative flex items-start gap-4 pl-1">
              <div
                className={cn(
                  "z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full border-2 bg-background transition-colors",
                  isCompleted
                    ? "border-emerald-500 text-emerald-400"
                    : isActive
                    ? "border-amber-500 text-amber-400"
                    : isFailed
                    ? "border-rose-500 text-rose-400"
                    : "border-muted-foreground/30 text-muted-foreground"
                )}
              >
                {isCompleted ? (
                  <CheckCircle2 className="h-5 w-5" />
                ) : isActive || isFailed ? (
                  <Loader2 className="h-5 w-5 animate-spin" />
                ) : (
                  <Circle className="h-4 w-4" />
                )}
              </div>
              <div className="pt-1">
                <div
                  className={cn(
                    "text-sm font-semibold tracking-wide transition-colors",
                    isCompleted
                      ? "text-emerald-400"
                      : isActive
                      ? "text-amber-400"
                      : isFailed
                      ? "text-rose-400"
                      : "text-muted-foreground"
                  )}
                >
                  {step.label}
                </div>
                <div className="text-xs text-muted-foreground">
                  {isCompleted
                    ? "Completed"
                    : isActive
                    ? "In progress..."
                    : isFailed
                    ? "Failed"
                    : "Pending"}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
