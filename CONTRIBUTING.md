# Contributing to Minefetch

A Paper plugin that shows host system information in-game with `/minefetch` (alias `/sysinfo`). Thanks for wanting to help!

## Getting started

```bash
./gradlew build
```

`scripts/install.sh [plugins-dir]` builds the plugin and copies it into a server's plugins folder.

## Notes for this project

- **The plugin doesn't query the host itself.** `scripts/update-sysinfo.sh` (or `.ps1`) captures fastfetch output into a shared directory that gets mounted into the Minecraft container at `/sysinfo/host.json`. The plugin just reads that file — keep that separation; adding direct host access would break the container boundary it's designed around.
- Built against Paper API 26.3, api-version 1.21; permission `minefetch.use` is granted to everyone by default — keep the command harmless without it.
- Windows/macOS/Linux variants of the sysinfo script should stay in step: a change to the JSON shape must update all of them.
- Test with a real `host.json` fixture; the plugin's whole world is one file read and a formatted command response.

## Before opening a PR

- `./gradlew build` passes and you tested `/minefetch` on a Paper server with a mounted `host.json`.
- Conventional commits; focused PRs; describe the test.
- AI-assisted PRs welcome; stand behind every line and run the build yourself.

## Licence

See [LICENSE](LICENSE). Contributions accepted under the same terms.