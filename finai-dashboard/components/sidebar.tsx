"use client";

import { cn } from "@/lib/utils";
import { BarChart3, BrainCircuit, LayoutDashboard, TrendingUp } from "lucide-react";

export type View = "overview" | "portfolio" | "market" | "research";

const nav = [
  { id: "overview" as View, label: "Dashboard Overview", icon: LayoutDashboard },
  { id: "portfolio" as View, label: "Portfolio Analytics", icon: BarChart3 },
  { id: "market" as View, label: "Market Watch", icon: TrendingUp },
  { id: "research" as View, label: "AI Research Desk", icon: BrainCircuit },
];

interface SidebarProps {
  active: View;
  onChange: (view: View) => void;
}

export function Sidebar({ active, onChange }: SidebarProps) {
  return (
    <aside className="hidden w-64 shrink-0 flex-col border-r bg-card/50 backdrop-blur lg:flex">
      <div className="flex h-16 items-center gap-2 border-b px-6">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-emerald-400 to-teal-600 text-zinc-900">
          <TrendingUp className="h-5 w-5" />
        </div>
        <span className="text-lg font-bold tracking-tight">FinAI</span>
      </div>
      <nav className="flex-1 space-y-1 p-4">
        {nav.map((item) => {
          const selected = active === item.id;
          const Icon = item.icon;
          return (
            <button
              key={item.id}
              onClick={() => onChange(item.id)}
              className={cn(
                "flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
                selected
                  ? "bg-emerald-500/10 text-emerald-400 ring-1 ring-emerald-500/20"
                  : "text-muted-foreground hover:bg-accent hover:text-foreground"
              )}
            >
              <Icon className="h-4 w-4" />
              {item.label}
            </button>
          );
        })}
      </nav>
      <div className="border-t p-4">
        <div className="rounded-lg bg-muted/50 p-3 text-xs text-muted-foreground">
          <p className="font-medium text-foreground">System Status</p>
          <p className="mt-1">Microservices discovery via Eureka</p>
        </div>
      </div>
    </aside>
  );
}
