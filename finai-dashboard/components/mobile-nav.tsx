"use client";

import { cn } from "@/lib/utils";
import { BarChart3, BrainCircuit, LayoutDashboard, Menu, TrendingUp, X } from "lucide-react";
import { useState } from "react";
import { View } from "./sidebar";

const nav = [
  { id: "overview" as View, label: "Dashboard", icon: LayoutDashboard },
  { id: "portfolio" as View, label: "Portfolio", icon: BarChart3 },
  { id: "market" as View, label: "Market", icon: TrendingUp },
  { id: "research" as View, label: "AI Research", icon: BrainCircuit },
];

interface MobileNavProps {
  active: View;
  onChange: (view: View) => void;
}

export function MobileNav({ active, onChange }: MobileNavProps) {
  const [open, setOpen] = useState(false);

  return (
    <div className="lg:hidden">
      <div className="flex h-16 items-center justify-between border-b bg-card/50 px-4 backdrop-blur">
        <div className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-emerald-400 to-teal-600 text-zinc-900">
            <TrendingUp className="h-5 w-5" />
          </div>
          <span className="text-lg font-bold tracking-tight">FinAI</span>
        </div>
        <button
          onClick={() => setOpen(!open)}
          className="inline-flex h-10 w-10 items-center justify-center rounded-md border border-input bg-background"
        >
          {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </div>
      {open && (
        <nav className="border-b bg-card/90 p-4 backdrop-blur">
          {nav.map((item) => {
            const selected = active === item.id;
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                onClick={() => {
                  onChange(item.id);
                  setOpen(false);
                }}
                className={cn(
                  "flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
                  selected
                    ? "bg-emerald-500/10 text-emerald-400"
                    : "text-muted-foreground hover:bg-accent hover:text-foreground"
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </button>
            );
          })}
        </nav>
      )}
    </div>
  );
}
