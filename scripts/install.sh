#!/bin/bash
# install.sh — Build and install the Minefetch plugin into the Minecraft server.
#
# Usage:  ./scripts/install.sh [server-plugins-dir]

set -euo pipefail

PLUGIN_DIR="${1:-/Volumes/Storage/Server/MC/data/plugins}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building Minefetch..."
cd "$PROJECT_DIR"
./gradlew shadowJar

JAR="$PROJECT_DIR/build/libs/Minefetch-1.0.0-all.jar"
if [ ! -f "$JAR" ]; then
    echo "Error: Build output not found at $JAR"
    exit 1
fi

echo "Installing to $PLUGIN_DIR..."
cp "$JAR" "$PLUGIN_DIR/Minefetch.jar"

echo "Done! Minefetch installed. Restart or /reload to enable."
