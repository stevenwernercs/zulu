#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
cd "$ROOT_DIR"

JAR="$(ls -1 target/*-all.jar 2>/dev/null | head -n1 || true)"
if [[ -z "${JAR}" ]]; then
  echo "Fat jar not found. Building..."
  mvn -DskipTests package
  JAR="$(ls -1 target/*-all.jar | head -n1)"
fi

echo "Running: ${JAR}"
exec java -jar "${JAR}"

