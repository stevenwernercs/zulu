#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
cd "$ROOT_DIR"

# Best-effort DISPLAY setup for WSL if not already set
if grep -qi microsoft /proc/version 2>/dev/null; then
  if [[ -z "${DISPLAY:-}" ]]; then
    ip=$(grep -m1 nameserver /etc/resolv.conf | awk '{print $2}')
    export DISPLAY="${ip}:0"
    export LIBGL_ALWAYS_INDIRECT=1
    echo "WSL detected: DISPLAY set to ${DISPLAY} (LIBGL_ALWAYS_INDIRECT=1)"
  fi
fi

JAR="$(ls -1 target/*-all.jar 2>/dev/null | head -n1 || true)"
if [[ -z "${JAR}" ]]; then
  echo "Fat jar not found. Building..."
  mvn -DskipTests package
  JAR="$(ls -1 target/*-all.jar | head -n1)"
fi

echo "Running: ${JAR}"
NATIVES_DIR=target/natives/linux
if [ ! -d "$NATIVES_DIR" ]; then
  echo "Extracting LWJGL natives..."
  mvn -DskipTests package >/dev/null
fi

exec java -Dorg.lwjgl.librarypath="$NATIVES_DIR" -jar "${JAR}"
