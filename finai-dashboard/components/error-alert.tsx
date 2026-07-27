"use client";

import { AlertCircle, X } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";

interface ErrorAlertProps {
  title?: string;
  message: string;
  onDismiss?: () => void;
}

export function ErrorAlert({ title = "Connection issue", message, onDismiss }: ErrorAlertProps) {
  return (
    <Alert variant="destructive" className="relative">
      <AlertCircle className="h-4 w-4" />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription>{message}</AlertDescription>
      {onDismiss && (
        <Button
          variant="ghost"
          size="icon"
          className="absolute right-2 top-2 h-7 w-7 text-destructive-foreground/70 hover:text-destructive-foreground"
          onClick={onDismiss}
        >
          <X className="h-4 w-4" />
        </Button>
      )}
    </Alert>
  );
}
