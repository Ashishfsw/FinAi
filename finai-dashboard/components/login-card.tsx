"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { useAuth } from "@/lib/auth-context";
import { Loader2, Lock, Shield, User } from "lucide-react";

export function LoginCard() {
  const { login, register } = useAuth();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [form, setForm] = useState({
    username: "",
    password: "",
    email: "",
    fullName: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isLogin = mode === "login";

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      if (isLogin) {
        await login({ username: form.username, password: form.password });
      } else {
        await register({
          username: form.username,
          password: form.password,
          email: form.email,
          fullName: form.fullName,
        });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Authentication failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md border border-border/50 bg-card/80 shadow-2xl backdrop-blur">
        <CardHeader className="space-y-1">
          <div className="flex items-center justify-center pb-2">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-emerald-400 to-teal-600 text-zinc-900 shadow-lg">
              <Shield className="h-6 w-6" />
            </div>
          </div>
          <CardTitle className="text-center text-2xl font-bold tracking-tight">
            {isLogin ? "Welcome back" : "Create your account"}
          </CardTitle>
          <CardDescription className="text-center">
            {isLogin
              ? "Sign in to access the FinAI agentic wealth dashboard."
              : "Register a new investor profile to get started."}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="relative">
              <User className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Username"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                className="pl-9"
                required
              />
            </div>
            <div className="relative">
              <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Password"
                type="password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                className="pl-9"
                required
              />
            </div>
            {!isLogin && (
              <>
                <Input
                  placeholder="Email"
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  required
                />
                <Input
                  placeholder="Full name"
                  value={form.fullName}
                  onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                  required
                />
              </>
            )}
            <Button type="submit" disabled={loading} className="w-full">
              {loading ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : isLogin ? (
                <Lock className="mr-2 h-4 w-4" />
              ) : (
                <User className="mr-2 h-4 w-4" />
              )}
              {isLogin ? "Sign in" : "Create account"}
            </Button>
          </form>

          {error && (
            <Alert variant="destructive" className="mt-4">
              <AlertTitle>Authentication failed</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="mt-4 text-center text-sm text-muted-foreground">
            {isLogin ? "Don't have an account?" : "Already have an account?"}{" "}
            <button
              type="button"
              onClick={() => {
                setMode(isLogin ? "register" : "login");
                setError(null);
              }}
              className="font-medium text-emerald-400 hover:text-emerald-300"
            >
              {isLogin ? "Register" : "Sign in"}
            </button>
          </div>

          <p className="mt-4 text-center text-xs text-muted-foreground">
            Default demo credentials: <span className="font-medium">alpha_trader</span> / <span className="font-medium">alpha123</span>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
