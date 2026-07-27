# FinAI - AI-Powered FinTech Market Research & Insights Analyst

A production-grade, distributed FinTech platform built using Java 21, Spring Boot 3.x, Spring Cloud, and Spring AI. The platform autonomously tracks user investment portfolios, analyzes live financial market data/news using Agentic AI patterns, and generates highly accurate, verified investment and rebalancing reports.

## 🏗️ Architecture

### Microservices Architecture

The platform follows a distributed microservices architecture with domain data isolation:

- **Discovery Server** (Port 8761) - Eureka-based service registry
- **API Gateway** (Port 8080) - Spring Cloud Gateway for dynamic routing
- **Portfolio Service** (Port 8081) - Manages user profiles, risk appetites, and asset holdings
- **Market Data Service** (Port 8082) - External financial API integration with Redis caching
- **Insights Service** (Port 8083) - Core agentic orchestration with RAG and Evaluator-Optimizer pattern
- **Notification Service** (Port 8084) - Event-driven report delivery via Email/Slack

### Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **AI Engine**: Spring AI (OpenAI GPT-4o / Claude 3.5 Sonnet)
- **Data Layer**:
  - PostgreSQL (Portfolio Service)
  - Redis (Market Data Cache)
  - PostgreSQL + PGVector (Insights Vector Store)
- **Messaging**: Apache Kafka (Asynchronous report distribution)
- **Build Tool**: Maven

## 🚀 Agentic AI Execution Model

### Retrieval-Augmented Generation (RAG)
Real-time financial news is converted into embeddings and stored dynamically using `PgVectorStore` for semantic search and context retrieval.

### Tool Calling
The AI Agents dynamically trigger backend microservice REST endpoints using Spring AI's `@Tool` implementation:
- Portfolio data retrieval
- Stock quotes and market data
- Historical data analysis

### Evaluator-Optimizer Loop
A multi-agent pattern where:
1. **Generator Agent** drafts the investment report
2. **Evaluator Agent** cross-verifies figures against ground-truth data
3. If hallucinations are found, the system loops back for regeneration
4. Continues until confidence threshold is met or max iterations reached

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker & Docker Compose**
- **OpenAI API Key** (for GPT-4o)
- **SMTP Credentials** (for email notifications - optional)
- **Slack Webhook URL** (for Slack notifications - optional)

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd FinAI
```

### 2. Start Infrastructure Services

Start PostgreSQL, Redis, and Kafka using Docker Compose:

```bash
docker-compose up -d
```

Verify services are running:
```bash
docker-compose ps
```

### 3. Configure Environment Variables

Create environment variables or update `application.yml` files:

```bash
# Required for Insights Service
export OPENAI_API_KEY=your-openai-api-key-here

# Optional for Notification Service
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password
export SLACK_WEBHOOK_URL=your-slack-webhook-url
export SLACK_ENABLED=true
export EMAIL_ENABLED=true
```

### 4. Build the Project

Build all microservices:

```bash
mvn clean install
```

### 5. Start Microservices

Start services in the following order:

```bash
# Terminal 1 - Discovery Server
cd discovery-server
mvn spring-boot:run

# Terminal 2 - API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 3 - Portfolio Service
cd portfolio-service
mvn spring-boot:run

# Terminal 4 - Market Data Service
cd market-data-service
mvn spring-boot:run

# Terminal 5 - Insights Service
cd insights-service
mvn spring-boot:run

# Terminal 6 - Notification Service
cd notification-service
mvn spring-boot:run
```

### 6. Verify Services

Check Eureka Dashboard: http://localhost:8761

All services should be registered and show UP status.

## 📡 API Endpoints

### API Gateway (Port 8080)

All requests are routed through the API Gateway:

```
http://localhost:8080/api/portfolio/*    -> Portfolio Service
http://localhost:8080/api/market/*       -> Market Data Service
http://localhost:8080/api/insights/*    -> Insights Service
http://localhost:8080/api/notifications/* -> Notification Service
```

### Portfolio Service

```bash
# Create User
POST /api/portfolio/users
{
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "riskProfile": "MODERATE"
}

# Get User
GET /api/portfolio/users/{id}

# Add Asset Holding
POST /api/portfolio/users/{userId}/holdings
{
  "symbol": "AAPL",
  "assetName": "Apple Inc.",
  "assetType": "STOCK",
  "quantity": 100,
  "averageCostPrice": 150.00,
  "currentPrice": 175.50
}

# Get User Holdings
GET /api/portfolio/users/{userId}/holdings
```

### Market Data Service

```bash
# Get Stock Quote
GET /api/market/quote/{symbol}

# Get Historical Data
GET /api/market/historical/{symbol}?interval=daily&outputSize=compact

# Get Market News
GET /api/market/news/{symbol}

# Evict Cache
DELETE /api/market/cache/{symbol}
```

### Insights Service

```bash
# Generate Insight Report
POST /api/insights/reports/generate?userId=1&username=john_doe

# Get User Reports
GET /api/insights/reports/user/{userId}

# Ingest News for RAG
POST /api/insights/rag/ingest/{symbol}
```

### Notification Service

```bash
# Get User Notifications
GET /api/notifications/user/{userId}

# Send Manual Notification
POST /api/notifications/send?to=user@example.com&subject=Test&content=Hello
```

## 🔧 Configuration

### Database Configuration

**Portfolio Service** (PostgreSQL on port 5432):
- Database: `portfolio_db`
- User: `fintech`
- Password: `fintech123`

**Insights Service** (PostgreSQL with PGVector on port 5433):
- Database: `insights_db`
- User: `fintech`
- Password: `fintech123`

### Redis Configuration

- Host: `localhost`
- Port: `6379`
- TTL: 5 minutes (configurable)

### Kafka Configuration

- Bootstrap Servers: `localhost:9092`
- Topic: `report-events`
- Consumer Group: `notification-service-group`

### Kafka UI

Access Kafka UI for monitoring: http://localhost:8085

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Manual Testing with cURL

```bash
# Create a user
curl -X POST http://localhost:8080/api/portfolio/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "riskProfile": "MODERATE"
  }'

# Get stock quote
curl http://localhost:8080/api/market/quote/AAPL

# Generate insight report
curl -X POST "http://localhost:8080/api/insights/reports/generate?userId=1&username=testuser"
```

## 📊 Monitoring

### Actuator Endpoints

Each service exposes Spring Boot Actuator endpoints:

- Health: `http://localhost:{port}/actuator/health`
- Info: `http://localhost:{port}/actuator/info`
- Metrics: `http://localhost:{port}/actuator/metrics`

### Eureka Dashboard

- URL: http://localhost:8761
- View registered services and their status

### Kafka UI

- URL: http://localhost:8085
- Monitor topics, consumers, and message flow

## 🔒 Security Considerations

For production deployment:

1. **Enable HTTPS** - Configure SSL/TLS for all services
2. **API Authentication** - Implement OAuth2/JWT
3. **Database Security** - Use strong passwords and SSL connections
4. **API Key Management** - Use vault services for API keys
5. **Network Security** - Implement network policies and firewalls
6. **Rate Limiting** - Configure rate limiting in API Gateway
7. **Input Validation** - All endpoints have validation enabled

## 🚀 Deployment

### Docker Deployment

Build Docker images for each service:

```bash
# Build all services
mvn clean package docker:build

# Or build individual services
cd discovery-server && mvn clean package docker:build
cd ../api-gateway && mvn clean package docker:build
# ... repeat for other services
```

### Kubernetes Deployment

Create Kubernetes manifests for each service (example structure):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: portfolio-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: portfolio-service
  template:
    metadata:
      labels:
        app: portfolio-service
    spec:
      containers:
      - name: portfolio-service
        image: fintech/portfolio-service:1.0.0
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/portfolio_db"
```

### Cloud Deployment

The platform can be deployed to:
- **AWS** - EKS, RDS, ElastiCache, MSK
- **Google Cloud** - GKE, Cloud SQL, Memorystore, Pub/Sub
- **Azure** - AKS, Azure Database, Redis Cache, Event Hubs

## 🐛 Troubleshooting

### Service Not Registering with Eureka

Check Eureka server is running:
```bash
curl http://localhost:8761/eureka/apps
```

Verify service configuration in `application.yml`:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Database Connection Issues

Verify PostgreSQL is running:
```bash
docker-compose ps postgres-portfolio
docker-compose logs postgres-portfolio
```

Test connection:
```bash
psql -h localhost -p 5432 -U fintech -d portfolio_db
```

### Redis Connection Issues

Verify Redis is running:
```bash
docker-compose ps redis
redis-cli ping
```

### Kafka Connection Issues

Verify Kafka is running:
```bash
docker-compose ps kafka
docker-compose logs kafka
```

Check Kafka UI: http://localhost:8085

### AI Service Issues

Verify OpenAI API key is set:
```bash
echo $OPENAI_API_KEY
```

Check Insights Service logs for AI-related errors.

## 📈 Performance Tuning

### JVM Options

Add to `JAVA_OPTS` for each service:
```bash
-Xms512m -Xmx1024m -XX:+UseG1GC
```

### Connection Pooling

Configure HikariCP in `application.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### Redis Caching

Adjust TTL based on data volatility:
```yaml
market-data:
  cache:
    ttl: 300  # 5 minutes
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 📞 Support

For issues and questions:
- Create an issue on GitHub
- Contact: support@fintech-platform.com

## 🎯 Roadmap

- [ ] Add real-time WebSocket support for live market data
- [ ] Implement portfolio rebalancing automation
- [ ] Add support for additional AI models (Claude, Gemini)
- [ ] Implement advanced risk analytics
- [ ] Add mobile app support
- [ ] Implement multi-tenancy
- [ ] Add audit logging and compliance features
- [ ] Implement advanced caching strategies
- [ ] Add GraphQL API support
- [ ] Implement service mesh with Istio/Linkerd

## 🙏 Acknowledgments

- Spring AI Team for the excellent AI integration framework
- Spring Cloud Team for the robust microservices support
- OpenAI for GPT-4o API
- The open-source community for various libraries and tools
