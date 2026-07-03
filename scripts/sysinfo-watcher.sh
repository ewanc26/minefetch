#!/bin/bash
# sysinfo-watcher.sh — Host-side file watcher for on-demand sysinfo refresh.
#
# Polls the sysinfo directory for a .refresh trigger file. When the Minefetch
# plugin creates one (triggered by a player's /minefetch command), this watcher
# deletes the trigger and regenerates host.json with fresh fastfetch output.
#
# Usage:
#   ./scripts/sysinfo-watcher.sh [sysinfo-dir]
#
# Platform defaults:
#   macOS:  /Volumes/Storage/Server/MC/sysinfo
#   Linux:  /var/opt/minecraft/sysinfo
#   Windows: .\scripts\sysinfo-watcher.ps1

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

case "$(uname -s)" in
    Darwin)  DEFAULT_SYSINFO_DIR="/Volumes/Storage/Server/MC/sysinfo" ;;
    Linux)   DEFAULT_SYSINFO_DIR="${MC_SYSINFO_DIR:-/var/opt/minecraft/sysinfo}" ;;
    *)       echo "Unsupported OS. For Windows, use: powershell .\scripts\sysinfo-watcher.ps1"
             exit 1 ;;
esac

SYSINFO_DIR="${1:-$DEFAULT_SYSINFO_DIR}"
SYSINFO_FILE="$SYSINFO_DIR/host.json"
TRIGGER_FILE="$SYSINFO_DIR/.refresh"
FASTFETCH_CONFIG="${MC_SYSINFO_CONFIG:-$SCRIPT_DIR/fastfetch-config.jsonc}"

if ! command -v fastfetch &> /dev/null; then
    echo "Error: fastfetch not found. Install it with: brew install fastfetch (macOS) or sudo apt install fastfetch (Linux)"
    exit 1
fi

echo "Watching $SYSINFO_DIR for .refresh triggers..."

while true; do
    if [ -f "$TRIGGER_FILE" ]; then
        rm -f "$TRIGGER_FILE"
        fastfetch --config "$FASTFETCH_CONFIG" --format json > "$SYSINFO_FILE.tmp"
        mv "$SYSINFO_FILE.tmp" "$SYSINFO_FILE"
    fi
    sleep 0.5
done
