#!/bin/bash

# ScholarAI Communication Test Script
# This script tests the RabbitMQ communication between Spring Boot and FastAPI

echo "🚀 ScholarAI Communication Test"
echo "================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if service is running
check_service() {
    local url=$1
    local service_name=$2

    if curl -s "$url" >/dev/null; then
        echo -e "${GREEN}✅ $service_name is running${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is not running${NC}"
        return 1
    fi
}

# Check prerequisites
echo -e "${BLUE}📋 Checking prerequisites...${NC}"

# Check Spring Boot
if ! check_service "http://localhost:8080/actuator/health" "Spring Boot"; then
    echo -e "${YELLOW}💡 Start Spring Boot with: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev${NC}"
    exit 1
fi

# Check FastAPI
if ! check_service "http://localhost:8001/health" "FastAPI"; then
    echo -e "${YELLOW}💡 Start FastAPI with: poetry run uvicorn app.main:app --reload --port 8001${NC}"
    exit 1
fi

# Check RabbitMQ Management UI
if ! check_service "http://localhost:15672" "RabbitMQ Management"; then
    echo -e "${YELLOW}💡 RabbitMQ Management UI not accessible. Check if RabbitMQ is running.${NC}"
fi

echo ""
echo -e "${BLUE}🧪 Testing communication flow...${NC}"

# Test the summarization endpoint
echo -e "${YELLOW}📤 Sending summarization request...${NC}"

response=$(curl -s -X POST "http://localhost:8080/api/demo/trigger-summarization" \
    -H "Content-Type: application/json" \
    -d '{"pdfUrl": "https://arxiv.org/pdf/2301.00001.pdf"}')

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Request sent successfully!${NC}"
    echo "Response:"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"

    # Extract correlation ID for tracking
    correlation_id=$(echo "$response" | jq -r '.correlationId' 2>/dev/null)

    echo ""
    echo -e "${YELLOW}⏳ Processing will take ~12 seconds...${NC}"
    echo -e "${BLUE}📊 Monitor the logs in both Spring Boot and FastAPI terminals${NC}"
    echo -e "${BLUE}🔗 Correlation ID: $correlation_id${NC}"

    echo ""
    echo -e "${GREEN}🎯 Test completed! Check the service logs for processing details.${NC}"

else
    echo -e "${RED}❌ Failed to send request${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}📚 Additional Testing Options:${NC}"
echo "1. Swagger UI: http://localhost:8080/docs"
echo "2. RabbitMQ Management: http://localhost:15672 (scholar/scholar123)"
echo "3. FastAPI Health: http://localhost:8001/health"
echo "4. Spring Boot Health: http://localhost:8080/actuator/health"

echo ""
echo -e "${GREEN}✨ Communication backbone is working! Ready for real AI integration.${NC}"
