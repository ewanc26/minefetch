# update-sysinfo.ps1 — Capture host fastfetch output for the Minefetch plugin.
#
# Usage:  .\scripts\update-sysinfo.ps1 [[-SysinfoDir] <string>]
#
# The output is written to a shared directory that gets mounted into the
# Minecraft container at /sysinfo/host.json.

param(
    [string]$SysinfoDir = "C:\ProgramData\Minecraft\sysinfo"
)

$ConfigPath = Join-Path $PSScriptRoot "fastfetch-config.jsonc"
$SysinfoFile = Join-Path $SysinfoDir "host.json"

if (-not (Get-Command fastfetch -ErrorAction SilentlyContinue)) {
    Write-Error "fastfetch not found. Install it with: scoop install fastfetch  or  winget install fastfetch"
    exit 1
}

New-Item -ItemType Directory -Force -Path $SysinfoDir | Out-Null
$tmpfile = Join-Path $SysinfoDir "host.json.tmp"

& fastfetch --config "$ConfigPath" --format json | Out-File -FilePath $tmpfile -Encoding utf8
Move-Item -Force -Path $tmpfile -Destination $SysinfoFile

Write-Host "Written to $SysinfoFile"
