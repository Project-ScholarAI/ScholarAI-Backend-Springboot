#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ROOT_DIR="$(dirname "$0")/.."
DOCKER_APP="$ROOT_DIR/docker/docker-app.yml"
DOCKER_SERVICES="$ROOT_DIR/docker/docker-services.yml"

# Build image
build_docker() {
    "$ROOT_DIR/scripts/local.sh" build
    docker build --no-cache -t twiggle-app -f "$ROOT_DIR/docker/Dockerfile" "$ROOT_DIR" || {
        echo -e "${RED}Docker build failed.${NC}"; exit 1;
    }
    echo -e "${GREEN}Docker image built.${NC}"
}

# Start/Stop services
start_services() { docker compose -f "$DOCKER_SERVICES" up -d; }
stop_services() { docker compose -f "$DOCKER_SERVICES" down; }

start_app() { docker compose -f "$DOCKER_APP" up -d; }
stop_app() { docker compose -f "$DOCKER_APP" down; }

rebuild_all() {
    stop_app && stop_services
    build_docker
    start_services && start_app
}

rebuild_nocache() {
    docker compose -f "$DOCKER_SERVICES" build --no-cache &&
    docker compose -f "$DOCKER_APP" build --no-cache &&
    start_services &&
    start_app
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
