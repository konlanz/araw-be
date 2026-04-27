#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [[ -f ".env.deploy" ]]; then
  # shellcheck disable=SC1091
  source ".env.deploy"
fi

export HOST_POSTGRES_PORT="${HOST_POSTGRES_PORT:-55432}"
export HOST_MINIO_API_PORT="${HOST_MINIO_API_PORT:-9900}"
export HOST_MINIO_CONSOLE_PORT="${HOST_MINIO_CONSOLE_PORT:-9901}"

export POSTGRES_DB="${POSTGRES_DB:-springboot_db}"
export POSTGRES_USER="${POSTGRES_USER:-postgres}"
export POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-postgres}"

export MINIO_ROOT_USER="${MINIO_ROOT_USER:-minioadmin}"
export MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD:-minioadmin}"
export MINIO_BUCKET="${MINIO_BUCKET:-araw-media}"

export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:${HOST_POSTGRES_PORT}/${POSTGRES_DB}}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${POSTGRES_USER}}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${POSTGRES_PASSWORD}}"

export MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:${HOST_MINIO_API_PORT}}"
export MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-${MINIO_ROOT_USER}}"
export MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-${MINIO_ROOT_PASSWORD}}"
export MINIO_SECURE="${MINIO_SECURE:-false}"

if [[ -z "${GOOGLE_CLIENT_ID:-}" || -z "${GOOGLE_CLIENT_SECRET:-}" ]]; then
  echo "ERROR: GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET must be set."
  echo "Set them in your shell or in .env.deploy, then run this script again."
  exit 1
fi

echo "Starting backing services on host ports:"
echo "  Postgres: ${HOST_POSTGRES_PORT}"
echo "  MinIO API: ${HOST_MINIO_API_PORT}"
echo "  MinIO Console: ${HOST_MINIO_CONSOLE_PORT}"
docker compose -f compose.yaml up -d postgres minio

echo "Waiting for Postgres..."
until docker exec postgres-dev pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; do
  sleep 1
done

echo "Waiting for MinIO..."
until curl -fsS "http://localhost:${HOST_MINIO_API_PORT}/minio/health/ready" >/dev/null; do
  sleep 1
done

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"
  export PATH="${JAVA_HOME}/bin:${PATH}"
fi

echo "Building backend..."
./mvnw -q -DskipTests package

echo "Starting backend (nohup)..."
mkdir -p .run
pkill -f "araw-0.0.1-SNAPSHOT.jar" >/dev/null 2>&1 || true
nohup java -jar target/araw-0.0.1-SNAPSHOT.jar > .run/backend.log 2>&1 &
echo $! > .run/backend.pid

echo "Backend started."
echo "  API: http://localhost:8081"
echo "  MinIO Console: http://localhost:${HOST_MINIO_CONSOLE_PORT}"
echo "  Logs: tail -f .run/backend.log"
