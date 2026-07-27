import { AssetHolding, AuthResponse, InsightReport, LoginRequest, PortfolioSummary, RegisterRequest, StockQuote, User } from "@/lib/types";

const BASES = {
  portfolio: process.env.NEXT_PUBLIC_PORTFOLIO_API_URL || "/api",
  market: process.env.NEXT_PUBLIC_MARKET_API_URL || "/api",
  insights: process.env.NEXT_PUBLIC_INSIGHTS_API_URL || "/api",
};

const TOKEN_KEY = "finai_token";
const USERNAME_KEY = "finai_username";
const USER_ID_KEY = "finai_user_id";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token: string, username: string, userId: number) {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USERNAME_KEY, username);
    localStorage.setItem(USER_ID_KEY, String(userId));
  } catch {
    // ignore storage errors
  }
}

export function clearToken() {
  if (typeof window === "undefined") return;
  try {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(USER_ID_KEY);
  } catch {
    // ignore storage errors
  }
}

export function getStoredUsername(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(USERNAME_KEY);
}

export function getStoredUserId(): number | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(USER_ID_KEY);
  return raw ? Number(raw) : null;
}

export async function apiFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (options.headers) {
    const normalized = options.headers as Record<string, string>;
    Object.assign(headers, normalized);
  }
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(url, { ...options, headers });
  return handleResponse<T>(res);
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let message = res.statusText || "Request failed";
    try {
      const text = await res.text();
      if (text) message = text;
    } catch {
      // ignore
    }
    if (res.status === 401) {
      clearToken();
    }
    throw new Error(`${res.status}: ${message}`);
  }
  if (res.status === 204) return undefined as unknown as T;
  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return res.json();
  }
  return res.text() as unknown as T;
}

export async function login(payload: LoginRequest): Promise<AuthResponse> {
  const res = await fetch(`${BASES.portfolio}/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const data = await handleResponse<AuthResponse>(res);
  setToken(data.token, data.username, data.userId);
  return data;
}

export async function register(payload: RegisterRequest): Promise<AuthResponse> {
  const res = await fetch(`${BASES.portfolio}/v1/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const data = await handleResponse<AuthResponse>(res);
  setToken(data.token, data.username, data.userId);
  return data;
}

export async function createUser(payload: Partial<User>): Promise<User> {
  return apiFetch<User>(`${BASES.portfolio}/users`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function getUser(id: number): Promise<User> {
  return apiFetch<User>(`${BASES.portfolio}/users/${id}`);
}

export async function getPortfolioSummary(userId: number): Promise<PortfolioSummary> {
  return apiFetch<PortfolioSummary>(`${BASES.portfolio}/portfolios/${userId}`);
}

export async function addOrUpdateHolding(holding: Partial<AssetHolding>): Promise<AssetHolding> {
  return apiFetch<AssetHolding>(`${BASES.portfolio}/portfolios/holdings`, {
    method: "POST",
    body: JSON.stringify(holding),
  });
}

export async function getStockQuote(symbol: string): Promise<StockQuote> {
  return apiFetch<StockQuote>(`${BASES.market}/market/price/${encodeURIComponent(symbol.toUpperCase())}`);
}

export async function ingestNews(symbol: string): Promise<string> {
  return apiFetch<string>(`${BASES.insights}/insights/ingest-news/${encodeURIComponent(symbol.toUpperCase())}`, {
    method: "POST",
  });
}

export async function generateReport(userId: number, username?: string): Promise<InsightReport> {
  const params = new URLSearchParams();
  if (username) params.set("username", username);
  return apiFetch<InsightReport>(`${BASES.insights}/insights/generate-report/${userId}?${params.toString()}`, {
    method: "POST",
  });
}
