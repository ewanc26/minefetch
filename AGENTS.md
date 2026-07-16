# AGENTS.md

Guidance for agents working on Minefetch, a Kotlin Paper plugin plus host-side Fastfetch producers.

## Current data flow

- `MinefetchPlugin` hard-codes the container path `/sysinfo/host.json`, registers `/minefetch` (`/sysinfo`), starts a best-effort async GitHub update check, and collects live JVM/Paper metrics.
- Every command calls `SysInfoReader.readAndRefresh()`: it creates `/sysinfo/.refresh`, then calls `Thread.sleep(250)` up to 20 times on the Paper command thread while waiting for the trigger to disappear. This can block the server thread for five seconds and must not be described as asynchronous.
- The shell/PowerShell watcher polls `.refresh` every 500 ms, removes it, writes Fastfetch JSON to `host.json.tmp`, and atomically renames it. One-shot update scripts use the same temporary-file pattern.
- `SysInfoReader` tolerates missing/malformed JSON by returning null and extracts known Fastfetch modules into `HostInfo`. The command renders host values plus server version, Java, players, JVM heap, one-minute TPS, plugin count, and cores.
- Gradle uses JDK 26 while targeting Java/JVM 25, shades Kotlin and relocated `org.json`, and treats Paper as compile-only. Install scripts build the `-all.jar` and overwrite `Minefetch.jar` in an operator-selected plugin directory.

## Invariants and safety

- Move refresh waiting and file I/O off the Paper main thread before increasing work or timeouts; marshal only final messages back to the sender safely.
- Preserve the `.refresh`/temporary-file atomic handshake across Bash and PowerShell. Handle stale triggers, watcher absence, interrupted writes, BOM/encoding differences, and concurrent commands.
- Treat host JSON as untrusted and bound its size. Avoid exposing host secrets, private addresses, usernames, mounts, or arbitrary Fastfetch modules to all players; `minefetch.use` defaults to true.
- Keep `plugin.yml`, Gradle/plugin versions, README/runbooks, fixed mount path, and script defaults synchronized.

## Validation

Run `./gradlew build` with JDK 26, `bash -n` plus `shellcheck` on shell scripts, and PowerShell parsing/static checks where available. There is no automated test suite, so test on a disposable Paper 26.1 server with and without a watcher: missing/null/malformed/partial/oversized JSON, stale triggers, two simultaneous commands, five-second timeout, atomic refresh, permissions, TPS/memory formatting, update-check failure, and clean reload. Never commit generated host data or build output.
