/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/v1/auth/:path*",
        destination: `${process.env.NEXT_PUBLIC_PORTFOLIO_API_URL || "http://localhost:8081"}/api/v1/auth/:path*`,
      },
      {
        source: "/api/users/:path*",
        destination: `${process.env.NEXT_PUBLIC_PORTFOLIO_API_URL || "http://localhost:8081"}/api/v1/users/:path*`,
      },
      {
        source: "/api/portfolios/holdings",
        destination: `${process.env.NEXT_PUBLIC_PORTFOLIO_API_URL || "http://localhost:8081"}/api/v1/portfolios/holdings`,
      },
      {
        source: "/api/portfolios/:userId",
        destination: `${process.env.NEXT_PUBLIC_PORTFOLIO_API_URL || "http://localhost:8081"}/api/v1/portfolios/:userId`,
      },
      {
        source: "/api/market/price/:symbol",
        destination: `${process.env.NEXT_PUBLIC_MARKET_API_URL || "http://localhost:8082/market-data"}/api/v1/market/price/:symbol`,
      },
      {
        source: "/api/insights/ingest-news/:symbol",
        destination: `${process.env.NEXT_PUBLIC_INSIGHTS_API_URL || "http://localhost:8083"}/api/v1/insights/ingest-news/:symbol`,
      },
      {
        source: "/api/insights/generate-report/:userId",
        destination: `${process.env.NEXT_PUBLIC_INSIGHTS_API_URL || "http://localhost:8083"}/api/v1/insights/generate-report/:userId`,
      },
    ];
  },
  async headers() {
    return [
      {
        source: "/api/:path*",
        headers: [
          { key: "Access-Control-Allow-Origin", value: "*" },
          { key: "Access-Control-Allow-Headers", value: "Origin, X-Requested-With, Content-Type, Accept, Authorization" },
        ],
      },
    ];
  },
};

module.exports = nextConfig;
