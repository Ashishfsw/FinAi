export type RiskProfile = "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE";
export type AssetType = "STOCK" | "BOND" | "ETF" | "MUTUAL_FUND" | "CRYPTO" | "COMMODITY" | "CASH";
export type ReportStatus = "GENERATING" | "EVALUATING" | "COMPLETED" | "FAILED";

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  riskProfile: RiskProfile;
  totalInvestmentValue: number;
  targetAllocation: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AssetHolding {
  id?: number;
  symbol: string;
  assetName: string;
  assetType: AssetType;
  quantity: number | string;
  averageCostPrice: number | string;
  currentPrice?: number | string;
  currentValue?: number | string;
  unrealizedPnL?: number | string;
  unrealizedPnLPercentage?: number | string;
  purchasedAt?: string;
  lastUpdated?: string;
  user?: { id: number } | User;
}

export interface AssetAllocation {
  symbol: string;
  assetName: string;
  assetType: AssetType;
  currentValue: number | string;
  allocationPercentage: number | string;
  targetAllocation: number | string;
  allocationDifference: number | string;
}

export interface PortfolioSummary {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  riskProfile: RiskProfile;
  totalInvestmentValue: number | string;
  totalCurrentValue: number | string;
  totalUnrealizedPnL: number | string;
  totalUnrealizedPnLPercentage: number | string;
  assetAllocations: Record<string, AssetAllocation>;
  holdings: AssetHolding[];
  totalHoldings: number;
  bestPerformingAsset: number | string;
  worstPerformingAsset: number | string;
}

export interface StockQuote {
  symbol: string;
  name?: string;
  price?: number | string;
  change?: number | string;
  changePercent?: number | string;
  open?: number | string;
  high?: number | string;
  low?: number | string;
  previousClose?: number | string;
  volume?: number;
  timestamp?: string;
  exchange?: string;
  avPrice?: string;
  avChange?: string;
  avChangePercent?: string;
}

export interface InsightReport {
  id?: number;
  userId?: number;
  username?: string;
  reportContent?: string;
  summary?: string;
  recommendations?: string;
  riskAnalysis?: string;
  marketOutlook?: string;
  confidenceScore?: number | string;
  iterationCount?: number;
  status?: ReportStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface ApiError {
  message: string;
  status?: number;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  fullName: string;
  password: string;
}
