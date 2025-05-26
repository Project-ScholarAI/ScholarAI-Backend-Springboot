#!/bin/bash

###############################################################################
# Pretty colours
###############################################################################
RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

###############################################################################
# Paths & constants
###############################################################################
ROOT_DIR="$(dirname "$0")/.."
DOCKER_APP="$ROOT_DIR/docker/docker-app.yml"
DOCKER_SERVICES="$ROOT_DIR/docker/core-service.yml"
COMPOSE_STACK="-f \"$DOCKER_SERVICES\" -f \"$DOCKER_APP\""
IMAGE_NAME="scholar-ai-app"

###############################################################################
# Build the Spring Boot image
###############################################################################
build_docker() {
  echo -e "${BLUE}▶ Building Docker image '${IMAGE_NAME}'...${NC}"

  # Optional pre-hook
  if [ -x "$ROOT_DIR/scripts/local.sh" ]; then
    "$ROOT_DIR/scripts/local.sh" build
  fi

  # Source the .env file from the project root to load ENV variable
  if [ -f "$ROOT_DIR/.env" ]; then
    echo -e "${CYAN}ℹ Sourcing environment variables from $ROOT_DIR/.env${NC}"
    set -a # Automatically export all variables defined by source
    # shellcheck source=/dev/null # Path is dynamic
    source "$ROOT_DIR/.env"
    set +a
  else
    echo -e "${YELLOW}⚠ $ROOT_DIR/.env file not found. ENV build argument might not be set.${NC}"
  fi

  if [ -z "$ENV" ]; then
    echo -e "${RED}✖ Error: ENV variable is not set.${NC}"
    echo -e "${YELLOW}ℹ Please define ENV in your $ROOT_DIR/.env file (e.g., ENV=dev).${NC}"
    exit 1
  fi

  echo -e "${CYAN}ℹ Building with ENV=${ENV}...${NC}"
  docker build --no-cache --build-arg ENV="$ENV" -t "$IMAGE_NAME" -f "$ROOT_DIR/docker/Dockerfile" "$ROOT_DIR" || {
    echo -e "${RED}✖ Docker build failed.${NC}"
    exit 1
  }

  echo -e "${GREEN}✔ Docker image '${IMAGE_NAME}' built successfully.${NC}"
}

###############################################################################
# Start / stop helpers (single compose up for both files)
###############################################################################
start_stack() {
  echo -e "${CYAN}▶ Starting core services and application...${NC}"
  # shellcheck disable=SC2086
  eval docker compose $COMPOSE_STACK up -d
  echo -e "${GREEN}✔ Stack is up. API → http://localhost:8080 | RabbitMQ UI → http://localhost:15672${NC}"
}

stop_stack() {
  echo -e "${CYAN}▶ Stopping core services and application...${NC}"
  # shellcheck disable=SC2086
  eval docker compose $COMPOSE_STACK down
  echo -e "${GREEN}✔ Stack stopped.${NC}"
}

# Start/Stop services
start_services() {
    echo -e "${CYAN}Starting core services...${NC}"
    docker compose -f "$DOCKER_SERVICES" up -d
    echo -e "${GREEN}Core services started.${NC}"
    echo -e "${GREEN}RabbitMQ management UI should be available at http://localhost:15672 ${NC}"
    echo -e "${GREEN}Redis should be running on port 6379.${NC}"
}

stop_services() {
    echo -e "${CYAN}Stopping core services...${NC}"
    docker compose -f "$DOCKER_SERVICES" down
    echo -e "${GREEN}Core services stopped.${NC}"
}

start_app() {
    echo -e "${CYAN}Starting application...${NC}"
    docker compose -f "$DOCKER_APP" up -d
    echo -e "${GREEN}Application started. You can typically access it at http://localhost:8000 (please verify the port).${NC}"
}

stop_app() {
    echo -e "${CYAN}Stopping application...${NC}"
    docker compose -f "$DOCKER_APP" down
    echo -e "${GREEN}Application stopped.${NC}"
}

###############################################################################
# Rebuild logic
###############################################################################
rebuild_all() {
  echo -e "${YELLOW}⚡ Full rebuild (clean + build + up)...${NC}"
  stop_stack
  build_docker
  start_stack
  echo -e "${GREEN}✔ Full rebuild completed.${NC}"
}

rebuild_nocache() {
  echo -e "${YELLOW}⚡ Rebuild WITHOUT cache for every service...${NC}"
  # shellcheck disable=SC2086
  eval docker compose $COMPOSE_STACK build --no-cache || {
    echo -e "${RED}✖ Docker compose build --no-cache failed.${NC}"
    exit 1
  }
  start_stack
  echo -e "${GREEN}✔ Rebuild (no-cache) completed.${NC}"
}

###############################################################################
# Status
###############################################################################
status() {
  echo -e "${CYAN}▶ Current container status:${NC}"
  # shellcheck disable=SC2086
  eval docker compose $COMPOSE_STACK ps
}

###############################################################################
# CLI entrypoint
###############################################################################
case "$1" in
  "build")            build_docker ;;
  "start")            start_services && start_app ;;
  "stop")             stop_app && stop_services ;;
  "start-app")        start_app ;;
  "stop-app")         stop_app ;;
  "start-svc")        start_services ;;
  "stop-svc")         stop_services ;;
  "rebuild")          rebuild_all ;;
  "rebuild-nocache")  rebuild_nocache ;;
  "status")           status ;;
  *)
    echo -e "${RED}Usage:${NC} $0 ${YELLOW}{build|start|stop|start-app|stop-app|start-svc|stop-svc|rebuild|rebuild-nocache|status}${NC}"
    exit 1
    ;;
esac
