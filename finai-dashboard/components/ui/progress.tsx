import * as React from "react";
import { cn } from "@/lib/utils";

interface ProgressProps extends React.HTMLAttributes<HTMLDivElement> {
  value: number;
  max?: number;
  variant?: "default" | "emerald" | "amber" | "rose";
}

const Progress = React.forwardRef<HTMLDivElement, ProgressProps>(
  ({ className, value, max = 100, variant = "default", ...props }, ref) => {
    const pct = Math.min(100, Math.max(0, (value / max) * 100));
    const barColor =
      variant === "emerald"
        ? "bg-emerald-500"
        : variant === "amber"
        ? "bg-amber-500"
        : variant === "rose"
        ? "bg-rose-500"
        : "bg-primary";
    return (
      <div
        ref={ref}
        className={cn("relative h-2 w-full overflow-hidden rounded-full bg-secondary", className)}
        {...props}
      >
        <div
          className={cn("h-full transition-all duration-500 ease-out", barColor)}
          style={{ width: `${pct}%` }}
        />
      </div>
    );
  }
);
Progress.displayName = "Progress";

export { Progress };
