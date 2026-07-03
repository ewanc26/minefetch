#!/bin/bash
# update-sysinfo.sh — Capture host fastfetch output for the Minefetch plugin.
#
# Run this periodically (or set up a scheduled task) to refresh the system info
# that the /minefetch in-game command displays.
#
# Usage:  ./scripts/update-sysinfo.sh [sysinfo-dir] [config-path]
#
# Platform defaults:
#   macOS:  /Volumes/Storage/Server/MC/sysinfo
#   Linux:  /var/opt/minecraft/sysinfo
#   Windows: .\scripts\update-sysinfo.ps1
#
# The output is written to a shared directory that gets mounted into the
# Minecraft container at /sysinfo/host.json.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

case "$(uname -s)" in
    Darwin)  DEFAULT_SYSINFO_DIR="/Volumes/Storage/Server/MC/sysinfo" ;;
    Linux)   DEFAULT_SYSINFO_DIR="${MC_SYSINFO_DIR:-/var/opt/minecraft/sysinfo}" ;;
    *)       echo "Unsupported OS. For Windows, use: powershell .\scripts\update-sysinfo.ps1"
             exit 1 ;;
esac

SYSINFO_DIR="${1:-$DEFAULT_SYSINFO_DIR}"
SYSINFO_FILE="$SYSINFO_DIR/host.json"
FASTFETCH_CONFIG="${2:-$SCRIPT_DIR/fastfetch-config.jsonc}"

mkdir -p "$SYSINFO_DIR"

if ! command -v fastfetch &> /dev/null; then
    echo "Error: fastfetch not found. Install it with: brew install fastfetch (macOS) or sudo apt install fastfetch (Linux)"
    exit 1
fi

fastfetch --config "$FASTFETCH_CONFIG" --format json > "$SYSINFO_FILE.tmp"
mv "$SYSINFO_FILE.tmp" "$SYSINFO_FILE"

echo "Written to $SYSINFO_FILE"
