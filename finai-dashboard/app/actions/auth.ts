"use server";

import { cookies, headers } from "next/headers";
import { AuthResponse, LoginRequest, RegisterRequest } from "@/lib/types";

function resolveBackendUrl(): string {
  const configured = process.env.NEXT_PUBLIC_PORTFOLIO_API_URL?.replace(/\/$/, "");
  if (!configured) return "http://localhost:8081";
  if (configured.startsWith("http://") || configured.startsWith("https://")) {
    return configured;
  }
  // Relative path (e.g., /mock-api) - resolve against the current request host
  const host = headers().get("host") || "localhost:3000";
  const protocol = process.env.NODE_ENV === "production" ? "https" : "http";
  return `${protocol}://${host}${configured}`;
}

function getAuthBaseUrl(): string {
  return `${resolveBackendUrl()}/api/v1/auth`;
}

async function handleAuthResponse(res: Response): Promise<AuthResponse> {
  if (!res.ok) {
    let message = res.statusText || "Authentication failed";
    try {
      const text = await res.text();
      if (text) message = text;
    } catch {
      // ignore
    }
    throw new Error(`${res.status}: ${message}`);
  }
  return res.json();
}

export async function loginAction(payload: LoginRequest): Promise<AuthResponse> {
  const res = await fetch(`${getAuthBaseUrl()}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const data = await handleAuthResponse(res);
  const cookieStore = cookies();
  cookieStore.set("finai_token", data.token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "strict",
    maxAge: 60 * 60 * 24,
    path: "/",
  });
  return data;
}

export async function registerAction(payload: RegisterRequest): Promise<AuthResponse> {
  const res = await fetch(`${getAuthBaseUrl()}/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const data = await handleAuthResponse(res);
  const cookieStore = cookies();
  cookieStore.set("finai_token", data.token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "strict",
    maxAge: 60 * 60 * 24,
    path: "/",
  });
  return data;
}

export async function logoutAction(): Promise<void> {
  const cookieStore = cookies();
  cookieStore.delete("finai_token");
}
