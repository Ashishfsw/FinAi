# FinAI Platform - End-to-End Testing Workflow

## Prerequisites

1. Start all infrastructure services:
```bash
cd /Users/walkover/Public/FinAI
docker-compose up -d postgres-portfolio postgres-insights redis zookeeper kafka kafka-ui
```

2. Start Discovery Server (Eureka):
```bash
cd discovery-server
mvn spring-boot:run
```

3. Start Portfolio Service:
```bash
cd portfolio-service
mvn spring-boot:run
```

4. Start Market Data Service:
```bash
cd market-data-service
mvn spring-boot:run
```

5. Start Insights Service:
```bash
cd insights-service
export OPENAI_API_KEY=your-openai-api-key
mvn spring-boot:run
```

---

## Complete E2E Testing Workflow

### Step 1: Verify Service Discovery

```bash
# Check Eureka Dashboard
curl http://localhost:8761/eureka/apps

# Expected output should show:
# - portfolio-service
# - market-data-service
# - insights-service
```

### Step 2: Create Sample User Portfolio

```bash
# Create a user
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "riskProfile": "MODERATE"
  }'

# Expected response: User object with ID (assume ID = 1)

# Add stock holdings to portfolio
curl -X POST http://localhost:8081/api/v1/portfolios/holdings \
  -H "Content-Type: application/json" \
  -d '{
    "user": {"id": 1},
    "symbol": "AAPL",
    "assetName": "Apple Inc.",
    "assetType": "STOCK",
    "quantity": 100,
    "averageCostPrice": 150.00,
    "currentPrice": 175.50
  }'

curl -X POST http://localhost:8081/api/v1/portfolios/holdings \
  -H "Content-Type: application/json" \
  -d '{
    "user": {"id": 1},
    "symbol": "GOOGL",
    "assetName": "Alphabet Inc.",
    "assetType": "STOCK",
    "quantity": 50,
    "averageCostPrice": 120.00,
    "currentPrice": 135.00
  }'

curl -X POST http://localhost:8081/api/v1/portfolios/holdings \
  -H "Content-Type: application/json" \
  -d '{
    "user": {"id": 1},
    "symbol": "TSLA",
    "assetName": "Tesla Inc.",
    "assetType": "STOCK",
    "quantity": 25,
    "averageCostPrice": 200.00,
    "currentPrice": 220.00
  }'

# Verify portfolio summary
curl http://localhost:8081/api/v1/portfolios/1
```

### Step 3: Test Market Data Service with Mock Fallback

```bash
# Test live price endpoint (will use mock fallback)
curl http://localhost:8082/api/v1/market/price/AAPL

# Test market news endpoint (will use mock fallback)
curl http://localhost:8082/api/v1/market/news/AAPL

# Test market news for other holdings
curl http://localhost:8082/api/v1/market/news/GOOGL
curl http://localhost:8082/api/v1/market/news/TSLA

# Verify Redis caching
docker exec -it fintech-redis redis-cli
KEYS quote:*
KEYS news:*
GET quote:AAPL
TTL quote:AAPL
```

### Step 4: RAG Ingestion for Portfolio Holdings

```bash
# Ingest news for AAPL
curl -X POST http://localhost:8083/api/v1/insights/ingest-news/AAPL

# Ingest news for GOOGL
curl -X POST http://localhost:8083/api/v1/insights/ingest-news/GOOGL

# Ingest news for TSLA
curl -X POST http://localhost:8083/api/v1/insights/ingest-news/TSLA

# Verify vector store stats
curl http://localhost:8083/api/v1/insights/vector-store/stats

# Test similarity search
curl "http://localhost:8083/api/v1/insights/search?query=Apple%20financial%20performance&topK=3"

# Verify PGVector database
docker exec -it fintech-postgres-insights psql -U fintech -d insights_db
SELECT COUNT(*) FROM vector_store;
SELECT id, left(content, 100) as content_preview FROM vector_store ORDER BY id DESC LIMIT 5;
```

### Step 5: Trigger Multi-Agent Report Generation

```bash
# Generate investment report for user ID 1
curl -X POST "http://localhost:8083/api/v1/insights/generate-report/1?username=john_doe"

# This will trigger:
# 1. Generator Agent fetches portfolio summary
# 2. Generator Agent fetches live stock quotes for AAPL, GOOGL, TSLA
# 3. Generator Agent retrieves news for each symbol
# 4. Generator Agent searches relevant context via RAG
# 5. Generator Agent creates comprehensive report
# 6. Evaluator Agent verifies data integrity
# 7. If invalid, feedback loop (max 3 iterations)
# 8. Final report saved with confidence score

# Monitor the process (check logs for iteration progress)
# Expected: Report generation takes 30-60 seconds
```

### Step 6: Verify Generated Report

```bash
# Get the generated report
curl http://localhost:8083/api/v1/insights/reports/user/1

# Get specific report by ID (use ID from previous response)
curl http://localhost:8083/api/v1/insights/reports/{report_id}

# Expected response structure:
{
  "id": 1,
  "userId": 1,
  "username": "john_doe",
  "status": "COMPLETED",
  "iterationCount": 1,
  "confidenceScore": 0.85,
  "summary": "...",
  "recommendations": "...",
  "riskAnalysis": "...",
  "marketOutlook": "...",
  "reportContent": "# Investment Analysis Report for John Doe\n\n...",
  "createdAt": "2026-07-25T12:00:00"
}
```

### Step 7: Verify Database Invariants

```bash
# Check Portfolio Database
docker exec -it fintech-postgres-portfolio psql -U fintech -d portfolio_db
\dt
SELECT * FROM users;
SELECT * FROM asset_holdings;

# Check Insights Database
docker exec -it fintech-postgres-insights psql -U fintech -d insights_db
\dt
SELECT * FROM financial_news;
SELECT * FROM insight_reports;
SELECT COUNT(*) FROM vector_store;

# Verify PGVector extension
\dx
# Should show: vector | 1 | public | vector data type and ivfflat and hnsw access methods
```

### Step 8: Verify Redis Cache Invariants

```bash
# Connect to Redis
docker exec -it fintech-redis redis-cli

# Check all cache keys
KEYS *

# Expected patterns:
# quote:AAPL, quote:GOOGL, quote:TSLA
# news:AAPL, news:GOOGL, news:TSLA
# historical:AAPL:daily:compact (if historical data was fetched)

# Check TTLs
TTL quote:AAPL
TTL news:AAPL

# Expected: ~300 seconds (5 minutes)

# Verify cache hit patterns
# Run the same price query again
curl http://localhost:8082/api/v1/market/price/AAPL
# Should hit cache (check logs for "Cache hit")
```

### Step 9: Cleanup (Optional)

```bash
# Clear vector store
curl -X DELETE http://localhost:8083/api/v1/insights/rag/clear

# Clear Redis cache
curl -X DELETE http://localhost:8082/api/v1/market-data/cache

# Stop all services
docker-compose down
```

---

## Expected Results Summary

### Service Discovery
- ✅ All three services registered in Eureka
- ✅ Services can discover each other via service names
- ✅ Load balancing works correctly

### Portfolio Service
- ✅ User creation successful
- ✅ Asset holdings added correctly
- ✅ Portfolio summary returns complete data
- ✅ Database schema initialized via schema.sql

### Market Data Service
- ✅ Mock fallback working for local development
- ✅ Redis caching with 5-minute TTL
- ✅ Price and news endpoints functional
- ✅ Cache hit/miss logic working

### Insights Service
- ✅ RAG ingestion successful
- ✅ PGVector database populated
- ✅ Similarity search working
- ✅ Multi-agent orchestration functional
- ✅ Generator-Evaluator loop executing
- ✅ Reports generated with confidence scores

### Database Invariants
- ✅ Portfolio DB (port 5432) with users and asset_holdings tables
- ✅ Insights DB (port 5433) with financial_news, insight_reports, vector_store tables
- ✅ PGVector extension installed and working
- ✅ No synchronization conflicts between databases

### Cache Invariants
- ✅ Redis cache keys properly prefixed (quote:, news:, historical:)
- ✅ TTL set to 300 seconds (5 minutes)
- ✅ Cache eviction working correctly
- ✅ No key conflicts between services

---

## Troubleshooting

### Service Discovery Issues
```bash
# Check Eureka server status
curl http://localhost:8761/eureka/apps

# Verify service registration
# Each service should appear in the registry
```

### Database Connection Issues
```bash
# Check PostgreSQL containers
docker-compose ps postgres-portfolio postgres-insights

# Verify databases exist
docker exec -it fintech-postgres-portfolio psql -U fintech -l
docker exec -it fintech-postgres-insights psql -U fintech -l
```

### Redis Connection Issues
```bash
# Check Redis container
docker-compose ps redis

# Test Redis connection
docker exec -it fintech-redis redis-cli PING
# Should return: PONG
```

### AI Tool Calling Issues
```bash
# Check service name mappings
# Ensure WebClient URLs use service names, not localhost
# Example: http://portfolio-service/api/v1/portfolios/{userId}
```

### PGVector Issues
```bash
# Verify extension is installed
docker exec -it fintech-postgres-insights psql -U fintech -d insights_db
\dx

# Check vector store table
\d vector_store
```

---

## Performance Benchmarks

Expected response times (local development):
- Portfolio API: 50-100ms
- Market Data API (cached): 10-20ms
- Market Data API (uncached): 500-1000ms (with mock fallback)
- RAG Ingestion: 2-5 seconds per symbol
- Similarity Search: 100-300ms
- Report Generation: 30-60 seconds (depends on iterations)
