"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { createUser } from "@/lib/api";
import { RiskProfile, User } from "@/lib/types";
import { Loader2, UserPlus } from "lucide-react";

interface CreateUserCardProps {
  onUserCreated?: (user: User) => void;
}

export function CreateUserCard({ onUserCreated }: CreateUserCardProps) {
  const [form, setForm] = useState({
    username: "",
    email: "",
    fullName: "",
    password: "",
    riskProfile: "MODERATE" as RiskProfile,
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);
    try {
      const user = await createUser(form);
      setMessage(`User created: ${user.username} (ID: ${user.id})`);
      onUserCreated?.(user);
      setForm({ username: "", email: "", fullName: "", password: "", riskProfile: "MODERATE" });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to create user");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <UserPlus className="h-5 w-5 text-emerald-400" />
          Create User Profile
        </CardTitle>
        <CardDescription>Onboard a new investor into FinAI.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-3">
          <Input
            placeholder="Username"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            required
          />
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
          <Input
            placeholder="Password"
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
          <Select
            value={form.riskProfile}
            onChange={(e) => setForm({ ...form, riskProfile: e.target.value as "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE" })}
          >
            <option value="CONSERVATIVE">Conservative</option>
            <option value="MODERATE">Moderate</option>
            <option value="AGGRESSIVE">Aggressive</option>
          </Select>
          <Button type="submit" disabled={loading} className="w-full">
            {loading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <UserPlus className="mr-2 h-4 w-4" />}
            Create User
          </Button>
          {message && (
            <Alert variant="success">
              <AlertTitle>Success</AlertTitle>
              <AlertDescription>{message}</AlertDescription>
            </Alert>
          )}
          {error && (
            <Alert variant="destructive">
              <AlertTitle>Error</AlertTitle>
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
        </form>
      </CardContent>
    </Card>
  );
}
