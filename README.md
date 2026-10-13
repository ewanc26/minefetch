# Minefetch

A Paper plugin that shows host system information in-game with `/minefetch` (alias `/sysinfo`).

- Permission: `minefetch.use` (granted to everyone by default).
- Built against Paper API 26.1.2 and declares `api-version` 1.21.

## Host information

The plugin does not query the host itself. `scripts/update-sysinfo.sh` (macOS and Linux) or `scripts/update-sysinfo.ps1` (Windows) captures fastfetch output into a shared directory. That directory is mounted into the Minecraft container at `/sysinfo/host.json`, which the plugin reads. Run the script periodically or schedule it. `scripts/sysinfo-watcher.sh` and `scripts/minefetch-launchd.plist` are also included.

## Building

Requires a JDK. The Gradle wrapper is included:

```
./gradlew build
```

`scripts/install.sh [plugins-dir]` builds the plugin and copies it into a server's plugins folder.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
