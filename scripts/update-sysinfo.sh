#!/bin/bash
# update-sysinfo.sh — Capture host fastfetch output for the Minefetch plugin.
#
# Run this periodically (or set up a LaunchAgent) to refresh the system info
# that the /minefetch in-game command displays.
#
# Usage:  ./scripts/update-sysinfo.sh
#
# The output is written to a shared directory that gets mounted into the
# Minecraft container at /sysinfo/host.json.

set -euo pipefail

SYSINFO_DIR="${1:-/Volumes/Storage/Server/MC/sysinfo}"
SYSINFO_FILE="$SYSINFO_DIR/host.json"

mkdir -p "$SYSINFO_DIR"

if ! command -v fastfetch &> /dev/null; then
    echo "Error: fastfetch not found. Install it with: brew install fastfetch"
    exit 1
fi

fastfetch --format json > "$SYSINFO_FILE.tmp"
mv "$SYSINFO_FILE.tmp" "$SYSINFO_FILE"

echo "Written to $SYSINFO_FILE"
