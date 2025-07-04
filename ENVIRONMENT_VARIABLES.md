# Environment Variables Configuration

This document lists all required environment variables for production deployment.

## Redis Configuration (REQUIRED)

For production deployment with Redis authentication, you must set:

```bash
REDIS_HOST=core-redis          # or your Redis server hostname
REDIS_PORT=6379               # Redis port (default: 6379)
REDIS_PASSWORD=your_secure_redis_password_here  # REQUIRED for authentication
```

## Database Configuration

### Core Database
```bash
CORE_DB_USER=scholar_user
CORE_DB_PASSWORD=your_secure_password_here
CORE_DB_HOST=core-db          # Docker service name or hostname
CORE_DB_PORT=5432
CORE_DB_NAME=coreDB
```

### Paper Database
```bash
PAPER_DB_USER=paper_user
PAPER_DB_PASSWORD=your_secure_password_here
PAPER_DB_HOST=paper-db        # Docker service name or hostname
PAPER_DB_PORT=5432
PAPER_DB_NAME=paperDB
```

## RabbitMQ Configuration
```bash
RABBITMQ_HOST=core-rabbitmq   # Docker service name or hostname
RABBITMQ_PORT=5672
RABBITMQ_USER=scholar_user
RABBITMQ_PASSWORD=your_secure_rabbitmq_password_here
```

## JWT Configuration
```bash
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here_at_least_32_characters
JWT_ACCESS_EXPIRATION_MS=900000       # 15 minutes
JWT_REFRESH_EXPIRATION_MS=604800000   # 7 days
```

## Social Authentication
```bash
SPRING_GOOGLE_CLIENT_ID=your_google_client_id_here
SPRING_GOOGLE_CLIENT_SECRET=your_google_client_secret_here
SPRING_GITHUB_CLIENT_ID=your_github_client_id_here
SPRING_GITHUB_CLIENT_SECRET=your_github_client_secret_here
```

## CORS Configuration
```bash
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://scholarai.tech
```

## Quick Start for Production

1. Copy this template and create your `.env` file
2. Replace all placeholder values with secure, production-ready values
3. Ensure `REDIS_PASSWORD` is set to a strong password (minimum 16 characters)
4. Run your services with: `docker-compose -f docker/core-service.yml up -d`

## Security Notes

- Use strong, unique passwords for all services
- Keep your `.env` file secure and never commit it to version control
- Regularly rotate passwords and secrets
- Use environment-specific configurations for different deployment stages 