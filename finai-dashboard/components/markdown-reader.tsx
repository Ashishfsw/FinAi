"use client";

import ReactMarkdown from "react-markdown";
import { cn } from "@/lib/utils";

interface MarkdownReaderProps {
  content: string;
  className?: string;
}

const components = {
  h1: ({ className, ...props }: React.HTMLAttributes<HTMLHeadingElement>) => (
    <h1 className={cn("mb-4 mt-6 text-2xl font-bold tracking-tight text-foreground", className)} {...props} />
  ),
  h2: ({ className, ...props }: React.HTMLAttributes<HTMLHeadingElement>) => (
    <h2 className={cn("mb-3 mt-5 text-xl font-semibold tracking-tight text-emerald-400", className)} {...props} />
  ),
  h3: ({ className, ...props }: React.HTMLAttributes<HTMLHeadingElement>) => (
    <h3 className={cn("mb-2 mt-4 text-lg font-semibold text-foreground/90", className)} {...props} />
  ),
  p: ({ className, ...props }: React.HTMLAttributes<HTMLParagraphElement>) => (
    <p className={cn("mb-4 leading-7 text-foreground/80", className)} {...props} />
  ),
  ul: ({ className, ...props }: React.HTMLAttributes<HTMLUListElement>) => (
    <ul className={cn("mb-4 list-disc space-y-1 pl-6 text-foreground/80", className)} {...props} />
  ),
  ol: ({ className, ...props }: React.HTMLAttributes<HTMLOListElement>) => (
    <ol className={cn("mb-4 list-decimal space-y-1 pl-6 text-foreground/80", className)} {...props} />
  ),
  li: ({ className, ...props }: React.HTMLAttributes<HTMLLIElement>) => (
    <li className={cn("marker:text-emerald-400", className)} {...props} />
  ),
  strong: ({ className, ...props }: React.HTMLAttributes<HTMLElement>) => (
    <strong className={cn("font-semibold text-emerald-300", className)} {...props} />
  ),
  a: ({ className, ...props }: React.AnchorHTMLAttributes<HTMLAnchorElement>) => (
    <a className={cn("text-emerald-400 underline hover:text-emerald-300", className)} {...props} />
  ),
  code: ({ className, ...props }: React.HTMLAttributes<HTMLElement>) => (
    <code
      className={cn(
        "rounded bg-muted px-1.5 py-0.5 font-mono text-sm text-amber-300",
        className
      )}
      {...props}
    />
  ),
  pre: ({ className, ...props }: React.HTMLAttributes<HTMLPreElement>) => (
    <pre
      className={cn(
        "mb-4 overflow-x-auto rounded-lg border border-border bg-muted/50 p-4 text-sm",
        className
      )}
      {...props}
    />
  ),
  blockquote: ({ className, ...props }: React.HTMLAttributes<HTMLQuoteElement>) => (
    <blockquote
      className={cn(
        "mb-4 border-l-4 border-emerald-500/30 pl-4 italic text-foreground/70",
        className
      )}
      {...props}
    />
  ),
};

export function MarkdownReader({ content, className }: MarkdownReaderProps) {
  return (
    <div className={cn("text-sm", className)}>
      <ReactMarkdown components={components}>{content}</ReactMarkdown>
    </div>
  );
}
