#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PID_FILE=".run/backend.pid"

if [[ ! -f "$PID_FILE" ]]; then
  echo "No PID file found at $PID_FILE. Backend may already be stopped."
  exit 0
fi

PID="$(cat "$PID_FILE")"

if [[ -z "$PID" ]]; then
  echo "PID file is empty. Removing stale PID file."
  rm -f "$PID_FILE"
  exit 0
fi

if kill -0 "$PID" >/dev/null 2>&1; then
  echo "Stopping backend process $PID..."
  kill "$PID"
  for _ in {1..20}; do
    if ! kill -0 "$PID" >/dev/null 2>&1; then
      break
    fi
    sleep 0.5
  done
fi

if kill -0 "$PID" >/dev/null 2>&1; then
  echo "Process did not stop gracefully, forcing kill..."
  kill -9 "$PID" || true
fi

rm -f "$PID_FILE"
echo "Backend stopped."
