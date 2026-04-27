#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [[ -f ".env.deploy" ]]; then
  # shellcheck disable=SC1091
  set -a
  source ".env.deploy"
  set +a
fi

export HOST_POSTGRES_PORT="${HOST_POSTGRES_PORT:-55432}"
export HOST_MINIO_API_PORT="${HOST_MINIO_API_PORT:-9900}"
export HOST_MINIO_CONSOLE_PORT="${HOST_MINIO_CONSOLE_PORT:-9901}"

export POSTGRES_DB="${POSTGRES_DB:-springboot_db}"

# Keep docker-compose Postgres creds and Spring datasource creds aligned.
# Prefer explicit POSTGRES_* for compose; if only Spring vars are provided, mirror them.
if [[ -n "${POSTGRES_USER:-}" && -n "${SPRING_DATASOURCE_USERNAME:-}" && "${POSTGRES_USER}" != "${SPRING_DATASOURCE_USERNAME}" ]]; then
  echo "ERROR: POSTGRES_USER and SPRING_DATASOURCE_USERNAME must match for this deployment script."
  exit 1
fi
if [[ -n "${POSTGRES_PASSWORD:-}" && -n "${SPRING_DATASOURCE_PASSWORD:-}" && "${POSTGRES_PASSWORD}" != "${SPRING_DATASOURCE_PASSWORD}" ]]; then
  echo "ERROR: POSTGRES_PASSWORD and SPRING_DATASOURCE_PASSWORD must match for this deployment script."
  exit 1
fi

export POSTGRES_USER="${POSTGRES_USER:-${SPRING_DATASOURCE_USERNAME:-postgres}}"
export POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-${SPRING_DATASOURCE_PASSWORD:-postgres}}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${POSTGRES_USER}}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${POSTGRES_PASSWORD}}"

export MINIO_ROOT_USER="${MINIO_ROOT_USER:-minioadmin}"
export MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD:-minioadmin}"
export MINIO_BUCKET="${MINIO_BUCKET:-araw-media}"

export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:${HOST_POSTGRES_PORT}/${POSTGRES_DB}}"

export MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:${HOST_MINIO_API_PORT}}"
export MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-${MINIO_ROOT_USER}}"
export MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-${MINIO_ROOT_PASSWORD}}"
export MINIO_SECURE="${MINIO_SECURE:-false}"

if [[ -z "${GOOGLE_CLIENT_ID:-}" || -z "${GOOGLE_CLIENT_SECRET:-}" ]]; then
  echo "ERROR: GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET must be set."
  echo "Set them in your shell or in .env.deploy, then run this script again."
  exit 1
fi

resolve_java_home() {
  is_java_21_jdk() {
    local home="$1"
    [[ -x "${home}/bin/javac" ]] || return 1
    local ver
    ver="$("${home}/bin/javac" -version 2>&1 || true)"
    [[ "${ver}" =~ javac\ 21\. ]] || return 1
    return 0
  }

  # If JAVA_HOME is already set and supports Java 21 compilation, keep it.
  if [[ -n "${JAVA_HOME:-}" ]] && is_java_21_jdk "${JAVA_HOME}"; then
    echo "${JAVA_HOME}"
    return 0
  fi

  # macOS helper
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
      /usr/libexec/java_home -v 21
      return 0
    fi
  fi

  # Linux common locations (Ubuntu/Debian)
  for candidate in \
    /usr/lib/jvm/java-21-openjdk-amd64 \
    /usr/lib/jvm/java-21-openjdk \
    /usr/lib/jvm/java-21-amazon-corretto \
    /usr/lib/jvm/temurin-21-jdk-amd64; do
    if is_java_21_jdk "${candidate}"; then
      echo "${candidate}"
      return 0
    fi
  done

  # Last resort: try PATH javac
  if command -v javac >/dev/null 2>&1; then
    local ver
    ver="$(javac -version 2>&1 || true)"
    if [[ "${ver}" =~ javac\ 21\. ]]; then
      # Derive JAVA_HOME from javac path: .../bin/javac -> .../
      local javac_path
      javac_path="$(command -v javac)"
      echo "$(cd "$(dirname "${javac_path}")/.." && pwd)"
      return 0
    fi
  fi

  return 1
}

echo "Starting backing services on host ports:"
echo "  Postgres: ${HOST_POSTGRES_PORT}"
echo "  MinIO API: ${HOST_MINIO_API_PORT}"
echo "  MinIO Console: ${HOST_MINIO_CONSOLE_PORT}"
docker compose -f compose.yaml up -d postgres minio

echo "Waiting for Postgres..."
until docker exec postgres-dev pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; do
  sleep 1
done
if ! docker exec -e PGPASSWORD="${SPRING_DATASOURCE_PASSWORD}" postgres-dev \
  psql -h 127.0.0.1 -U "${SPRING_DATASOURCE_USERNAME}" -d "${POSTGRES_DB}" -c "select 1;" >/dev/null 2>&1; then
  echo "ERROR: Postgres is running, but the backend datasource credentials cannot log in."
  echo "If the Docker volume already existed, it keeps the credentials from first initialization."
  echo "Fix by rotating the database role password or, for a fresh dev database, recreate the volume:"
  echo "  docker compose -f compose.yaml down -v"
  echo "  ./deploy-backend.sh"
  exit 1
fi

echo "Waiting for MinIO..."
until curl -fsS "http://localhost:${HOST_MINIO_API_PORT}/minio/health/ready" >/dev/null; do
  sleep 1
done

JAVA_HOME_RESOLVED="$(resolve_java_home || true)"
if [[ -z "${JAVA_HOME_RESOLVED}" ]]; then
  echo "ERROR: Java 21 JDK not found (javac must support release 21)."
  echo "Fix options:"
  echo "  - Set JAVA_HOME to a Java 21 JDK (must include bin/javac)"
  echo "  - Ubuntu/Debian example: sudo apt-get install -y openjdk-21-jdk"
  echo "  - macOS example: brew install openjdk@21"
  exit 1
fi

export JAVA_HOME="${JAVA_HOME_RESOLVED}"
export PATH="${JAVA_HOME}/bin:${PATH}"

echo "Using JAVA_HOME=${JAVA_HOME}"
java -version

echo "Building backend..."
./mvnw -q -DskipTests package

echo "Starting backend (nohup)..."
mkdir -p .run
pkill -f "araw-0.0.1-SNAPSHOT.jar" >/dev/null 2>&1 || true
nohup "${JAVA_HOME}/bin/java" -jar target/araw-0.0.1-SNAPSHOT.jar > .run/backend.log 2>&1 &
echo $! > .run/backend.pid

echo "Backend started."
echo "  API: http://localhost:8081"
echo "  MinIO Console: http://localhost:${HOST_MINIO_CONSOLE_PORT}"
echo "  Logs: tail -f .run/backend.log"
