# sysinfo-watcher.ps1 — Host-side file watcher for on-demand sysinfo refresh.
#
# Polls the sysinfo directory for a .refresh trigger file. When the Minefetch
# plugin creates one (triggered by a player's /minefetch command), this watcher
# deletes the trigger and regenerates host.json with fresh fastfetch output.
#
# Usage:  .\scripts\sysinfo-watcher.ps1 [[-SysinfoDir] <string>]

param(
    [string]$SysinfoDir = "C:\ProgramData\Minecraft\sysinfo"
)

$ConfigPath = Join-Path $PSScriptRoot "fastfetch-config.jsonc"
$SysinfoFile = Join-Path $SysinfoDir "host.json"
$TriggerFile = Join-Path $SysinfoDir ".refresh"

if (-not (Get-Command fastfetch -ErrorAction SilentlyContinue)) {
    Write-Error "fastfetch not found. Install it with: scoop install fastfetch  or  winget install fastfetch"
    exit 1
}

Write-Host "Watching $SysinfoDir for .refresh triggers..."

while ($true) {
    if (Test-Path $TriggerFile) {
        Remove-Item -Force $TriggerFile
        $tmpfile = Join-Path $SysinfoDir "host.json.tmp"
        & fastfetch --config "$ConfigPath" --format json | Out-File -FilePath $tmpfile -Encoding utf8
        Move-Item -Force -Path $tmpfile -Destination $SysinfoFile
    }
    Start-Sleep -Milliseconds 500
}
