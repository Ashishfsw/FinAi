"use client";

import * as React from "react";
import { cn } from "@/lib/utils";

type TabsContextValue = {
  value: string;
  onValueChange: (value: string) => void;
};

const TabsContext = React.createContext<TabsContextValue | null>(null);

function useTabs() {
  const context = React.useContext(TabsContext);
  if (!context) throw new Error("Tabs components must be used within <Tabs>");
  return context;
}

interface TabsProps {
  defaultValue: string;
  value?: string;
  onValueChange?: (value: string) => void;
  children: React.ReactNode;
  className?: string;
}

function Tabs({ defaultValue, value, onValueChange, children, className }: TabsProps) {
  const [internal, setInternal] = React.useState(defaultValue);
  const current = value ?? internal;
  const handleChange = React.useCallback(
    (v: string) => {
      setInternal(v);
      onValueChange?.(v);
    },
    [onValueChange]
  );
  return (
    <TabsContext.Provider value={{ value: current, onValueChange: handleChange }}>
      <div className={cn("w-full", className)}>{children}</div>
    </TabsContext.Provider>
  );
}

function TabsList({ className, children }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={cn("inline-flex h-10 items-center justify-center rounded-md bg-muted p-1 text-muted-foreground", className)}>
      {children}
    </div>
  );
}

interface TabsTriggerProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  value: string;
}

function TabsTrigger({ value, className, children, ...props }: TabsTriggerProps) {
  const { value: selected, onValueChange } = useTabs();
  const active = selected === value;
  return (
    <button
      role="tab"
      aria-selected={active}
      onClick={() => onValueChange(value)}
      className={cn(
        "inline-flex items-center justify-center whitespace-nowrap rounded-sm px-3 py-1.5 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
        active ? "bg-background text-foreground shadow-sm" : "hover:bg-background/50 hover:text-foreground",
        className
      )}
      {...props}
    >
      {children}
    </button>
  );
}

interface TabsContentProps extends React.HTMLAttributes<HTMLDivElement> {
  value: string;
}

function TabsContent({ value, className, children, ...props }: TabsContentProps) {
  const { value: selected } = useTabs();
  if (selected !== value) return null;
  return (
    <div role="tabpanel" className={cn("mt-2 ring-offset-background focus-visible:outline-none", className)} {...props}>
      {children}
    </div>
  );
}

export { Tabs, TabsList, TabsTrigger, TabsContent };
