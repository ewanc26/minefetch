#!/bin/bash
# install.sh — Build and install the Minefetch plugin into the Minecraft server.
#
# Usage:  ./scripts/install.sh [server-plugins-dir]
#
# Platform defaults:
#   macOS:  /Volumes/Storage/Server/MC/data/plugins
#   Linux:  /opt/minecraft/server/plugins
#   Windows: (use install.ps1 instead)

set -euo pipefail

case "$(uname -s)" in
    Darwin)  DEFAULT_PLUGIN_DIR="/Volumes/Storage/Server/MC/data/plugins" ;;
    Linux)   DEFAULT_PLUGIN_DIR="${MC_PLUGIN_DIR:-/opt/minecraft/server/plugins}" ;;
    *)       echo "Unsupported OS. For Windows, use: powershell .\scripts\install.ps1"
             exit 1 ;;
esac

PLUGIN_DIR="${1:-$DEFAULT_PLUGIN_DIR}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building Minefetch..."
cd "$PROJECT_DIR"
./gradlew shadowJar

JAR=$(ls "$PROJECT_DIR/build/libs/Minefetch-*-all.jar" 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
    echo "Error: Build output not found at build/libs/Minefetch-*-all.jar"
    exit 1
fi

echo "Installing to $PLUGIN_DIR..."
mkdir -p "$PLUGIN_DIR"
cp "$JAR" "$PLUGIN_DIR/Minefetch.jar"

echo "Done! Minefetch installed. Restart or /reload to enable."
