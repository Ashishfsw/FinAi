import { NextRequest, NextResponse } from "next/server";
import { AssetAllocation, AssetHolding, AssetType, AuthResponse, InsightReport, PortfolioSummary, RegisterRequest, StockQuote, User } from "@/lib/types";

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

let createdUsers: User[] = [];
let createdHoldings: AssetHolding[] = [];

const MOCK_HOLDINGS: AssetHolding[] = [
  {
    id: 1,
    symbol: "AAPL",
    assetName: "Apple Inc.",
    assetType: "STOCK" as AssetType,
    quantity: 50,
    averageCostPrice: 145.0,
    currentPrice: 175.5,
    currentValue: 8775.0,
    unrealizedPnL: 1525.0,
    unrealizedPnLPercentage: 10.5,
    purchasedAt: "2023-01-15T10:00:00Z",
    lastUpdated: "2026-07-25T10:00:00Z",
  },
  {
    id: 2,
    symbol: "TSLA",
    assetName: "Tesla, Inc.",
    assetType: "STOCK" as AssetType,
    quantity: 30,
    averageCostPrice: 210.0,
    currentPrice: 240.0,
    currentValue: 7200.0,
    unrealizedPnL: 900.0,
    unrealizedPnLPercentage: 4.29,
    purchasedAt: "2023-02-20T10:00:00Z",
    lastUpdated: "2026-07-25T10:00:00Z",
  },
  {
    id: 3,
    symbol: "BTC",
    assetName: "Bitcoin",
    assetType: "CRYPTO" as AssetType,
    quantity: 0.25,
    averageCostPrice: 28000.0,
    currentPrice: 32000.0,
    currentValue: 8000.0,
    unrealizedPnL: 1000.0,
    unrealizedPnLPercentage: 3.57,
    purchasedAt: "2023-03-10T10:00:00Z",
    lastUpdated: "2026-07-25T10:00:00Z",
  },
  {
    id: 4,
    symbol: "BND",
    assetName: "US Aggregate Bond ETF",
    assetType: "BOND" as AssetType,
    quantity: 100,
    averageCostPrice: 75.0,
    currentPrice: 76.5,
    currentValue: 7650.0,
    unrealizedPnL: 150.0,
    unrealizedPnLPercentage: 0.2,
    purchasedAt: "2023-04-05T10:00:00Z",
    lastUpdated: "2026-07-25T10:00:00Z",
  },
];

function buildPortfolioSummary(userId: number): PortfolioSummary {
  const holdings = createdHoldings.length ? createdHoldings : MOCK_HOLDINGS;
  const totalCurrentValue = holdings.reduce((sum, h) => sum + Number(h.currentValue ?? 0), 0);
  const totalInvestmentValue = holdings.reduce(
    (sum, h) => sum + Number(h.quantity ?? 0) * Number(h.averageCostPrice ?? 0),
    0
  );
  const totalUnrealizedPnL = totalCurrentValue - totalInvestmentValue;
  const totalUnrealizedPnLPercentage = totalInvestmentValue
    ? (totalUnrealizedPnL / totalInvestmentValue) * 100
    : 0;

  const user = createdUsers.find((u) => u.id === userId) || {
    id: userId,
    username: "demo_user",
    email: "demo@finai.com",
    fullName: "Demo Investor",
    riskProfile: "MODERATE",
    totalInvestmentValue: 0,
    targetAllocation: 100,
    createdAt: "2023-01-01T00:00:00Z",
    updatedAt: "2023-01-01T00:00:00Z",
  };

  const assetAllocations: Record<string, AssetAllocation> = {};
  holdings.forEach((h) => {
    const value = Number(h.currentValue ?? 0);
    assetAllocations[h.symbol] = {
      symbol: h.symbol,
      assetName: h.assetName,
      assetType: h.assetType,
      currentValue: value,
      allocationPercentage: totalCurrentValue ? (value / totalCurrentValue) * 100 : 0,
      targetAllocation: 20,
      allocationDifference: totalCurrentValue ? (value / totalCurrentValue) * 100 - 20 : 0,
    };
  });

  return {
    userId,
    username: user.username,
    email: user.email,
    fullName: user.fullName,
    riskProfile: user.riskProfile as "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE",
    totalInvestmentValue,
    totalCurrentValue,
    totalUnrealizedPnL,
    totalUnrealizedPnLPercentage,
    assetAllocations,
    totalHoldings: holdings.length,
    bestPerformingAsset: 10.5,
    worstPerformingAsset: 0.2,
    holdings,
  };
}

function buildQuote(symbol: string): StockQuote {
  return {
    symbol: symbol.toUpperCase(),
    name: `${symbol.toUpperCase()} Corp`,
    price: 175.5 + Math.random() * 10 - 5,
    change: 2.5 + Math.random() * 2 - 1,
    changePercent: 1.45 + Math.random() * 0.5 - 0.25,
    open: 170.0,
    high: 180.0,
    low: 169.0,
    previousClose: 172.0,
    volume: 1_500_000,
    timestamp: new Date().toISOString(),
    exchange: "NASDAQ",
  };
}

function buildReport(userId: number): InsightReport {
  return {
    id: 1,
    userId,
    username: "demo_user",
    reportContent:
      "# Agentic Wealth Report\n\n## Executive Summary\nThe portfolio is broadly diversified across equities, crypto, and fixed income. The current asset allocation slightly overweights growth assets, which is appropriate for a **MODERATE** risk profile.\n\n## Recommendations\n- **Rebalance**: Consider trimming equity exposure if it exceeds 60% of total value.\n- **Tax-loss harvesting**: Review underperforming positions for tax efficiency.\n- **Diversification**: Add international equity exposure to reduce home-country bias.\n\n## Risk Analysis\nVolatility remains within acceptable bands. Crypto allocation adds meaningful upside but also tail risk.\n\n## Market Outlook\nNear-term sentiment is constructive; maintain current strategic allocation with quarterly rebalancing.",
    summary:
      "Portfolio is well-positioned. Recommend quarterly rebalancing and adding international exposure.",
    recommendations:
      "1. Trim growth equities if > 60%.\n2. Review tax-loss harvesting opportunities.\n3. Add international equity exposure.",
    riskAnalysis:
      "Volatility within acceptable bands. Crypto allocation contributes tail risk; consider position sizing.",
    marketOutlook:
      "Constructive near-term sentiment; maintain strategic allocation with disciplined rebalancing.",
    confidenceScore: 0.87,
    iterationCount: 3,
    status: "COMPLETED",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

export async function GET(request: NextRequest, { params }: { params: { slug: string[] } }) {
  await sleep(400);
  const path = (params.slug || []).join("/");

  const portfolioMatch = path.match(/^portfolios\/(\d+)$/);
  if (portfolioMatch) {
    const userId = parseInt(portfolioMatch[1], 10);
    return NextResponse.json(buildPortfolioSummary(userId));
  }

  const userMatch = path.match(/^users\/(\d+)$/);
  if (userMatch) {
    const userId = parseInt(userMatch[1], 10);
    const user = createdUsers.find((u) => u.id === userId) || {
      id: userId,
      username: "demo_user",
      email: "demo@finai.com",
      fullName: "Demo Investor",
      riskProfile: "MODERATE",
      totalInvestmentValue: 0,
      targetAllocation: 100,
      createdAt: "2023-01-01T00:00:00Z",
      updatedAt: "2023-01-01T00:00:00Z",
    };
    return NextResponse.json(user);
  }

  const marketMatch = path.match(/^market\/price\/(.+)$/);
  if (marketMatch) {
    return NextResponse.json(buildQuote(decodeURIComponent(marketMatch[1])));
  }

  return NextResponse.json({ error: "Not found" }, { status: 404 });
}

export async function POST(request: NextRequest, { params }: { params: { slug: string[] } }) {
  await sleep(600);
  const path = (params.slug || []).join("/");

  const authMatch = path.match(/^api\/v1\/auth\/(login|register)$/);
  if (authMatch) {
    const payload = (await request.json()) as RegisterRequest;
    const isRegister = authMatch[1] === "register";
    let user: User;
    if (isRegister) {
      user = {
        id: createdUsers.length + 1,
        username: payload.username || "demo_user",
        email: payload.email || "demo@finai.com",
        fullName: payload.fullName || "Demo Investor",
        riskProfile: "MODERATE",
        totalInvestmentValue: 0,
        targetAllocation: 100,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      createdUsers.push(user);
    } else {
      user =
        createdUsers.find((u) => u.username === payload.username) || {
          id: 1,
          username: payload.username || "demo_user",
          email: "demo@finai.com",
          fullName: "Demo Investor",
          riskProfile: "MODERATE",
          totalInvestmentValue: 0,
          targetAllocation: 100,
          createdAt: "2023-01-01T00:00:00Z",
          updatedAt: "2023-01-01T00:00:00Z",
        };
    }
    const token = `mock_token_${user.id}_${Date.now()}`;
    return NextResponse.json({ token, userId: user.id, username: user.username } as AuthResponse);
  }

  if (path === "users") {
    const payload = (await request.json()) as Partial<User>;
    const newUser: User = {
      id: createdUsers.length + 1,
      username: payload.username || "demo_user",
      email: payload.email || "demo@finai.com",
      fullName: payload.fullName || "Demo Investor",
      riskProfile: payload.riskProfile || "MODERATE",
      totalInvestmentValue: payload.totalInvestmentValue ?? 0,
      targetAllocation: payload.targetAllocation ?? 100,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    createdUsers.push(newUser);
    return NextResponse.json(newUser, { status: 201 });
  }

  if (path === "portfolios/holdings") {
    const payload = (await request.json()) as Partial<AssetHolding>;
    const holding: AssetHolding = {
      id: createdHoldings.length + 101,
      symbol: payload.symbol || "UNKNOWN",
      assetName: payload.assetName || "Unknown Asset",
      assetType: payload.assetType || "STOCK",
      quantity: Number(payload.quantity ?? 0),
      averageCostPrice: Number(payload.averageCostPrice ?? 0),
      currentPrice: Number(payload.currentPrice ?? 0),
      currentValue:
        Number(payload.quantity ?? 0) * Number(payload.currentPrice ?? 0),
      unrealizedPnL:
        Number(payload.quantity ?? 0) *
        (Number(payload.currentPrice ?? 0) - Number(payload.averageCostPrice ?? 0)),
      unrealizedPnLPercentage:
        Number(payload.averageCostPrice ?? 0)
          ? ((Number(payload.currentPrice ?? 0) - Number(payload.averageCostPrice ?? 0)) /
              Number(payload.averageCostPrice ?? 0)) *
            100
          : 0,
      purchasedAt: new Date().toISOString(),
      lastUpdated: new Date().toISOString(),
    };
    createdHoldings.push(holding);
    return NextResponse.json(holding, { status: 201 });
  }

  const ingestMatch = path.match(/^insights\/ingest-news\/(.+)$/);
  if (ingestMatch) {
    return NextResponse.json(
      `Mock news ingestion completed for ${decodeURIComponent(ingestMatch[1]).toUpperCase()}.`,
      { status: 200 }
    );
  }

  const reportMatch = path.match(/^insights\/generate-report\/(\d+)$/);
  if (reportMatch) {
    const userId = parseInt(reportMatch[1], 10);
    return NextResponse.json(buildReport(userId), { status: 200 });
  }

  return NextResponse.json({ error: "Not found" }, { status: 404 });
}
