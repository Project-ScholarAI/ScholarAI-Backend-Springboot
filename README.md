# ScholarAI Backend

Your AI research assistant, mentor, and copilot - a Spring Boot application for academic research management.

## 🚀 Quick Start

### Prerequisites

- **Docker & Docker Compose** (for containerized setup)
- **Java 21** (for local development)
- **Maven 3.9+** (for local development)

### Option 1: Docker Setup (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Backend
   ```

2. **Set up environment variables**
   ```bash
   # Copy the example environment file
   cp env.example .env
   
   # Edit .env with your configuration
   # Required: Set ENV=dev or ENV=prod
   ```

3. **Start the entire stack**
   ```bash
   # Build and start all services (databases, Redis, RabbitMQ, app)
   ./scripts/docker.sh start-svc
   ```

4. **Access the application**
   - **API**: http://localhost:8080
   - **RabbitMQ Management**: http://localhost:15672
   - **Core Database**: localhost:5433
   - **Paper Database**: localhost:5434
   - **Redis**: localhost:6379

### Option 2: Local Development

1. **Set up environment variables**
   ```bash
   cp env.example .env
   # Configure your .env file
   ```

2. **Build and run locally**
   ```bash
   # Format code and build
   ./scripts/local.sh build
   
   # Run the application
   ./scripts/local.sh run
   ```

## 📋 Available Commands

### Docker Commands (`./scripts/docker.sh`)

| Command | Description |
|---------|-------------|
| `build` | Build the Docker image |
| `start` | Start all services (databases + app) |
| `stop` | Stop all services |
| `start-svc` | Start only core services (databases) |
| `stop-svc` | Stop only core services |
| `start-app` | Start only the application |
| `stop-app` | Stop only the application |
| `rebuild` | Full rebuild (stop → build → start) |
| `rebuild-nocache` | Rebuild without Docker cache |
| `status` | Show container status |
| `help` | Show detailed help |

### Local Development Commands (`./scripts/local.sh`)

| Command | Description |
|---------|-------------|
| `format` | Check and apply code formatting |
| `build` | Build the application |
| `test` | Run all tests |
| `run` | Build and run locally |
| `help` | Show detailed help |

## 🔧 Environment Configuration

Create a `.env` file based on `env.example` with the following required variables:

```bash
# Environment (Required)
ENV=dev  # or prod

# Database Configuration
CORE_DB_USER=your_core_db_user
CORE_DB_PASSWORD=your_core_db_password
PAPER_DB_USER=your_paper_db_user
PAPER_DB_PASSWORD=your_paper_db_password

# RabbitMQ
RABBITMQ_USER=your_rabbitmq_user
RABBITMQ_PASSWORD=your_rabbitmq_password

# Redis
REDIS_PASSWORD=your_redis_password

# JWT Configuration
JWT_SECRET=your_jwt_secret
JWT_ACCESS_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=86400000

# Social Authentication (Optional)
SPRING_GOOGLE_CLIENT_ID=your_google_client_id
SPRING_GOOGLE_CLIENT_SECRET=your_google_client_secret
SPRING_GITHUB_CLIENT_ID=your_github_client_id
SPRING_GITHUB_CLIENT_SECRET=your_github_client_secret

# Email Service (Optional)
SENDGRID_API_KEY=your_sendgrid_api_key
SENDGRID_FROM_EMAIL=your_email@domain.com
SENDGRID_TEMPLATE_ID=your_template_id

# External Services
FASTAPI_BASE_URL=your_fastapi_url
```

## 🏗️ Architecture

The application consists of:

- **Spring Boot Application** (Port 8080)
- **PostgreSQL Core Database** (Port 5433)
- **PostgreSQL Paper Database** (Port 5434)
- **Redis Cache** (Port 6379)
- **RabbitMQ Message Broker** (Port 5672, Management UI: 15672)

## 🧪 Testing

```bash
# Run tests locally
./scripts/local.sh test

# Run tests in Docker
./scripts/docker.sh build
```

## 📚 API Documentation

Once the application is running, access the API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔍 Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8080, 5433, 5434, 6379, 5672, 15672 are available
2. **Environment variables**: Make sure `.env` file exists and `ENV` is set
3. **Docker permissions**: Ensure Docker daemon is running and you have proper permissions

### Useful Commands

```bash
# Check container status
./scripts/docker.sh status

# View logs
docker compose -f docker/core-service.yml -f docker/docker-app.yml logs

# Clean up everything
./scripts/docker.sh stop
docker system prune -f
```

## 📝 Development Workflow

1. **Start development environment**
   ```bash
   ./scripts/docker.sh start-svc  # Start databases
   ./scripts/local.sh run         # Run app locally
   ```

2. **Make changes and test**
   ```bash
   ./scripts/local.sh test        # Run tests
   ./scripts/local.sh format      # Format code
   ```

3. **Deploy changes**
   ```bash
   ./scripts/docker.sh rebuild    # Rebuild and restart
   ```
