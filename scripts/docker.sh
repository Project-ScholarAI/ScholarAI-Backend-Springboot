#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ROOT_DIR="$(dirname "$0")/.."
DOCKER_APP="$ROOT_DIR/docker/docker-app.yml"
DOCKER_SERVICES="$ROOT_DIR/docker/core-service.yml"

# Build image
build_docker() {
    "$ROOT_DIR/scripts/local.sh" build
    docker build --no-cache -t twiggle-app -f "$ROOT_DIR/docker/Dockerfile" "$ROOT_DIR" || {
        echo -e "${RED}Docker build failed.${NC}"; exit 1;
    }
    echo -e "${GREEN}Docker image built.${NC}"
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

rebuild_all() {
    echo -e "${YELLOW}Starting full rebuild process...${NC}"
    stop_app && stop_services
    build_docker
    start_services && start_app
    echo -e "${GREEN}Full rebuild process completed.${NC}"
}

rebuild_nocache() {
    echo -e "${YELLOW}Starting rebuild process with no cache...${NC}"
    echo -e "${CYAN}Rebuilding core services (no-cache)...${NC}"
    docker compose -f "$DOCKER_SERVICES" build --no-cache || { echo -e "${RED}Core services rebuild (no-cache) failed.${NC}"; exit 1; }
    echo -e "${GREEN}Core services rebuilt successfully (no-cache).${NC}"

    echo -e "${CYAN}Rebuilding application (no-cache)...${NC}"
    docker compose -f "$DOCKER_APP" build --no-cache || { echo -e "${RED}Application rebuild (no-cache) failed.${NC}"; exit 1; }
    echo -e "${GREEN}Application rebuilt successfully (no-cache).${NC}"

    start_services && \
    start_app
    echo -e "${GREEN}Rebuild process with no cache completed.${NC}"
}

case "$1" in
    "build") build_docker ;;
    "start") start_services && start_app ;;
    "stop") stop_app && stop_services ;;
    "start-app") start_app ;;
    "stop-app") stop_app ;;
    "start-svc") start_services ;;
    "stop-svc") stop_services ;;
    "rebuild") rebuild_all ;;
    "rebuild-nocache") rebuild_nocache ;;
    *)
        echo -e "${RED}Usage: $0 {build|start|stop|start-app|stop-app|start-svc|stop-svc|rebuild|rebuild-nocache}${NC}"
        exit 1
        ;;
esac
