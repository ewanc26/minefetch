package uk.ewancroft.minefetch.command

import uk.ewancroft.minefetch.MinefetchPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class MinefetchCommand(private val plugin: MinefetchPlugin) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val hostInfo = plugin.getFreshHostInfo()
        val serverInfo = plugin.buildServerInfo()

        val accent = TextColor.color(0x00AAFF)
        val labelColor = TextColor.color(0xAAAAAA)
        val valueColor = NamedTextColor.WHITE
        val headerColor = TextColor.color(0xFFAA00)
        val dimColor = TextColor.color(0x555555)

        val lines = mutableListOf<Component>()

        lines.add(Component.text("")
            .append(Component.text("═══ ", dimColor))
            .append(Component.text("Minefetch", headerColor))
            .append(Component.text(" ═══", dimColor)))

        lines.add(Component.empty())

        if (hostInfo != null) {
            lines.add(Component.text("Host", accent))
            lines.add(Component.text("  ", labelColor)
                .append(Component.text("OS:       ", labelColor))
                .append(Component.text(hostInfo.osPretty, valueColor)))
            lines.add(Component.text("  ", labelColor)
                .append(Component.text("Kernel:   ", labelColor))
                .append(Component.text(hostInfo.kernel, valueColor)))
            lines.add(Component.text("  ", labelColor)
                .append(Component.text("CPU:      ", labelColor))
                .append(Component.text(hostInfo.cpu, valueColor)))
            lines.add(Component.text("  ", labelColor)
                .append(Component.text("RAM:      ", labelColor))
                .append(Component.text("${hostInfo.memoryUsed} / ${hostInfo.memoryTotal} (${hostInfo.memoryPercent})", valueColor)))
            if (hostInfo.diskTotal.isNotEmpty()) {
                lines.add(Component.text("  ", labelColor)
                    .append(Component.text("Disk:     ", labelColor))
                    .append(Component.text("${hostInfo.diskUsed} / ${hostInfo.diskTotal} (${hostInfo.diskPercent})", valueColor)))
            }
            lines.add(Component.text("  ", labelColor)
                .append(Component.text("Uptime:   ", labelColor))
                .append(Component.text(hostInfo.uptime, valueColor)))
            if (hostInfo.packages.isNotEmpty()) {
                lines.add(Component.text("  ", labelColor)
                    .append(Component.text("Packages: ", labelColor))
                    .append(Component.text(hostInfo.packages, valueColor)))
            }
        } else {
            lines.add(Component.text("  Host: ", labelColor)
                .append(Component.text("No host info available (run update-sysinfo.sh)", dimColor)))
        }

        lines.add(Component.empty())
        lines.add(Component.text("Server", accent))

        val serverLabel = serverInfo.serverVersion.let { v ->
            if (v.contains("null")) "Paper ${serverInfo.bukkitVersion}"
            else v.replace("git-Paper-", "Paper ")
        }
        lines.add(Component.text("  ", labelColor)
            .append(Component.text("Version:  ", labelColor))
            .append(Component.text(serverLabel, valueColor)))
        lines.add(Component.text("  ", labelColor)
            .append(Component.text("Java:     ", labelColor))
            .append(Component.text("${serverInfo.javaVersion} (${serverInfo.javaVendor})", valueColor)))
        lines.add(Component.text("  ", labelColor)
            .append(Component.text("Players:  ", labelColor))
            .append(Component.text("${serverInfo.onlinePlayers}/${serverInfo.maxPlayers}", valueColor)))

        val maxMem = formatBytes(serverInfo.maxMemory)
        val usedMem = formatBytes(serverInfo.usedMemory)
        val memPercent = if (serverInfo.maxMemory > 0) {
            "${(serverInfo.usedMemory * 100 / serverInfo.maxMemory)}%"
        } else "?"
        lines.add(Component.text("  ", labelColor)
            .append(Component.text("Memory:   ", labelColor))
            .append(Component.text("$usedMem / $maxMem ($memPercent)", valueColor)))

        val tps = getTPS(serverInfo.tps)
        val tpsColor = when {
            tps < 15.0 -> NamedTextColor.RED
            tps < 19.0 -> NamedTextColor.YELLOW
            else -> NamedTextColor.GREEN
        }
        lines.add(Component.text("  ", labelColor)
            .append(Component.text("TPS:      ", labelColor))
            .append(Component.text("%.1f".format(tps), tpsColor)))
        lines.add(Component.text("  ", labelColor)
            .append(Component.text("Plugins:  ", labelColor))
            .append(Component.text("${serverInfo.plugins} loaded", valueColor)))
        lines.add(Component.text("  ", labelColor)
            .append(Component.text("Cores:    ", labelColor))
            .append(Component.text("${serverInfo.availableProcessors}", valueColor)))

        lines.add(Component.empty())
        lines.add(Component.text("═══════════════", dimColor))

        sender.sendMessage(Component.join(JoinConfiguration.newlines(), lines))
        return true
    }

    private fun getTPS(tps: DoubleArray): Double {
        return if (tps.isEmpty()) 20.0
        else tps.firstOrNull()?.coerceIn(0.0, 20.0) ?: 20.0
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024L -> "%d KiB".format(bytes / 1024L)
            bytes < 1024L * 1024L * 1024L -> "%.1f MiB".format(bytes / (1024.0 * 1024.0))
            else -> "%.2f GiB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
