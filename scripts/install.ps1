# install.ps1 — Build and install the Minefetch plugin into the Minecraft server.
#
# Usage:  .\scripts\install.ps1 [[-PluginDir] <string>]

param(
    [string]$PluginDir = "C:\ProgramData\Minecraft\plugins"
)

$ProjectDir = Split-Path -Parent $PSScriptRoot

Write-Host "Building Minefetch..."
Set-Location $ProjectDir
& .\gradlew.bat shadowJar
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed"
    exit 1
}

$Jar = Get-ChildItem -Path (Join-Path $ProjectDir "build\libs") -Filter "Minefetch-*-all.jar" | Select-Object -First 1
if (-not $Jar) {
    Write-Error "Build output not found at build\libs\Minefetch-*-all.jar"
    exit 1
}

Write-Host "Installing to $PluginDir..."
New-Item -ItemType Directory -Force -Path $PluginDir | Out-Null
Copy-Item -Force -Path $Jar.FullName -Destination (Join-Path $PluginDir "Minefetch.jar")

Write-Host "Done! Minefetch installed. Restart or /reload to enable."
