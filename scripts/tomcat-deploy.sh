#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Created .env from .env.example. Please review credentials."
fi

set -a
source .env
set +a

docker compose -f docker/docker-compose.yml up -d --build postgres redis s3 app

echo "Deployment complete: http://localhost:8082/login"
