#!/bin/bash

# Production startup script for ScholarAI Backend
# This script ensures all required environment variables are set and starts services

set -e  # Exit on any error

echo "🚀 Starting ScholarAI Backend in Production Mode..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "Please create a .env file based on ENVIRONMENT_VARIABLES.md"
    exit 1
fi

# Source environment variables
source .env

# Validate critical environment variables
required_vars=(
    "REDIS_PASSWORD"
    "CORE_DB_USER"
    "CORE_DB_PASSWORD" 
    "PAPER_DB_USER"
    "PAPER_DB_PASSWORD"
    "RABBITMQ_USER"
    "RABBITMQ_PASSWORD"
    "JWT_SECRET"
)

echo "🔍 Validating environment variables..."
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ Error: $var is not set!"
        echo "Please check your .env file and set all required variables."
        exit 1
    fi
done

# Validate Redis password strength
if [ ${#REDIS_PASSWORD} -lt 16 ]; then
    echo "⚠️  Warning: REDIS_PASSWORD should be at least 16 characters for security!"
fi

echo "✅ All required environment variables are set"

# Start core services
echo "🐳 Starting core services with Docker Compose..."
docker-compose -f docker/core-service.yml down
docker-compose -f docker/core-service.yml up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo "🏥 Checking service health..."

# Check Redis
if docker exec core-redis redis-cli -a "$REDIS_PASSWORD" ping > /dev/null 2>&1; then
    echo "✅ Redis is healthy and authentication is working"
else
    echo "❌ Redis health check failed"
    exit 1
fi

# Check PostgreSQL cores
if docker exec core-db pg_isready -U "$CORE_DB_USER" > /dev/null 2>&1; then
    echo "✅ Core Database is healthy"
else
    echo "❌ Core Database health check failed"
    exit 1
fi

# Check Paper DB
if docker exec paper-db pg_isready -U "$PAPER_DB_USER" > /dev/null 2>&1; then
    echo "✅ Paper Database is healthy"
else
    echo "❌ Paper Database health check failed"
    exit 1
fi

# Check RabbitMQ
if docker exec core-rabbitmq rabbitmq-diagnostics check_port_connectivity > /dev/null 2>&1; then
    echo "✅ RabbitMQ is healthy"
else
    echo "❌ RabbitMQ health check failed"
    exit 1
fi

echo ""
echo "🎉 All services are running successfully!"
echo ""
echo "📊 Service Status:"
echo "   Redis:    localhost:6379 (password protected)"
echo "   Core DB:  localhost:5433"
echo "   Paper DB: localhost:55436"
echo "   RabbitMQ: localhost:5672 (management: localhost:15672)"
echo ""
echo "🔧 Next steps:"
echo "   1. Start your Spring Boot application with profile 'prod'"
echo "   2. Application will connect to authenticated Redis automatically"
echo "   3. Check logs to ensure Redis connection is successful"
echo ""
echo "💡 To start the application:"
echo "   mvn spring-boot:run -Dspring-boot.run.profiles=prod"
echo "" 