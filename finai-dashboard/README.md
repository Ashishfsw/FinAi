# FinAI Dashboard

A high-fidelity, dark-mode-first financial dashboard frontend for the FinAI agentic microservices platform.

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Styling**: Tailwind CSS
- **Components**: Custom shadcn/ui-style primitives
- **Icons**: Lucide React
- **Charts**: Recharts
- **Markdown**: ReactMarkdown (custom-styled, no extra typography plugin required)

## Project Structure

```
finai-dashboard/
├── app/
│   ├── globals.css        # Tailwind entry + CSS variables
│   ├── layout.tsx         # Root layout with dark theme
│   └── page.tsx           # Dashboard shell
├── components/
│   ├── ui/                # Reusable shadcn-style primitives
│   ├── dashboard-overview.tsx
│   ├── portfolio-overview.tsx
│   ├── market-watch.tsx
│   ├── ai-research-desk.tsx
│   ├── telemetry-pipeline.tsx
│   ├── markdown-reader.tsx
│   ├── sidebar.tsx
│   ├── mobile-nav.tsx
│   └── error-alert.tsx
├── lib/
│   ├── api.ts             # REST client wired to FinAI endpoints
│   ├── types.ts           # TypeScript DTOs
│   └── utils.ts           # cn / format helpers
├── next.config.js         # Proxy rewrites to backend services
├── package.json
├── tailwind.config.ts
└── tsconfig.json
```

## Getting Started

```bash
cd /Users/walkover/Public/FinAI/finai-dashboard
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

## Backend Wiring

The dashboard expects the FinAI microservices to be running. To avoid CORS issues, Next.js rewrites proxy requests from the dev server to the backend:

| Frontend path                              | Proxied to backend                                              |
| ------------------------------------------ | --------------------------------------------------------------- |
| `/api/users`                               | `PORTFOLIO_API_URL/api/v1/users`                                |
| `/api/portfolios/:userId`                  | `PORTFOLIO_API_URL/api/v1/portfolios/:userId`                   |
| `/api/portfolios/holdings`                 | `PORTFOLIO_API_URL/api/v1/portfolios/holdings`                  |
| `/api/market/price/:symbol`              | `MARKET_API_URL/api/v1/market/price/:symbol`                    |
| `/api/insights/ingest-news/:symbol`        | `INSIGHTS_API_URL/api/v1/insights/ingest-news/:symbol`          |
| `/api/insights/generate-report/:userId`    | `INSIGHTS_API_URL/api/v1/insights/generate-report/:userId`      |

Default backend URLs point to the individual service ports:

- Portfolio Service: `http://localhost:8081`
- Market Data Service: `http://localhost:8082/market-data`
- Insights Service: `http://localhost:8083`

Override them with environment variables:

```bash
NEXT_PUBLIC_PORTFOLIO_API_URL=http://localhost:8081
NEXT_PUBLIC_MARKET_API_URL=http://localhost:8082/market-data
NEXT_PUBLIC_INSIGHTS_API_URL=http://localhost:8083
```

If you prefer to use the API Gateway (port 8080), set all three variables to `http://localhost:8080` and ensure the gateway routes match your controller paths.

## Features

- **Dashboard Overview**: At-a-glance portfolio value, P&L, holdings count, quick navigation, and a Create User form.
- **Portfolio Analytics**: Total value, asset allocation pie chart, holdings performance table, and an Add/Update Holding form.
- **Market Watch**: Live ticker price lookup with cached quotes and one-click RAG news ingestion.
- **AI Research Desk**: Visual Evaluator-Optimizer telemetry pipeline, CTA to generate the agentic wealth report, and a rendered Markdown report with confidence score and recommendations.
- **Resilience**: Skeleton loaders, friendly error alerts, and connection-failure handling when services are warming up.
