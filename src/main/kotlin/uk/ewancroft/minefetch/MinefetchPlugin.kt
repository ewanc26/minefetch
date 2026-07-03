package uk.ewancroft.minefetch

import uk.ewancroft.minefetch.command.MinefetchCommand
import uk.ewancroft.minefetch.util.HostInfo
import uk.ewancroft.minefetch.util.SysInfoReader
import uk.ewancroft.minefetch.util.UpdateChecker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.lang.management.ManagementFactory

class MinefetchPlugin : JavaPlugin() {

    private var sysInfoReader: SysInfoReader? = null

    override fun onEnable() {
        sysInfoReader = SysInfoReader("/sysinfo/host.json")
        val cmd = getCommand("minefetch") ?: return
        cmd.setExecutor(MinefetchCommand(this))
        UpdateChecker(this, "ewanc26", "Minefetch").checkAsync()
        logger.info("Minefetch enabled.")
    }

    fun getHostInfo(): HostInfo? = sysInfoReader?.readHostInfo()

    fun getFreshHostInfo(): HostInfo? = sysInfoReader?.readAndRefresh()

    fun buildServerInfo(): ServerInfo {
        val runtime = Runtime.getRuntime()
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        val usedMem = runtime.totalMemory() - runtime.freeMemory()
        return ServerInfo(
            serverVersion = server.version,
            bukkitVersion = server.bukkitVersion,
            javaVersion = System.getProperty("java.version") ?: "unknown",
            javaVendor = System.getProperty("java.vendor") ?: "unknown",
            maxMemory = runtime.maxMemory(),
            totalMemory = runtime.totalMemory(),
            freeMemory = runtime.freeMemory(),
            usedMemory = usedMem,
            availableProcessors = runtime.availableProcessors(),
            osName = osBean?.name ?: System.getProperty("os.name") ?: "unknown",
            osArch = osBean?.arch ?: System.getProperty("os.arch") ?: "unknown",
            onlinePlayers = server.onlinePlayers.size,
            maxPlayers = server.maxPlayers,
            tps = Bukkit.getTPS(),
            plugins = server.pluginManager.plugins.size,
        )
    }

    companion object {
        private val ACCENT = TextColor.color(0x00AAFF)
        private val LABEL = TextColor.color(0xAAAAAA)
        private val VALUE = NamedTextColor.WHITE
        private val HEADER = TextColor.color(0xFFAA00)
        private val DIM = TextColor.color(0x555555)
    }

    data class ServerInfo(
        val serverVersion: String,
        val bukkitVersion: String,
        val javaVersion: String,
        val javaVendor: String,
        val maxMemory: Long,
        val totalMemory: Long,
        val freeMemory: Long,
        val usedMemory: Long,
        val availableProcessors: Int,
        val osName: String,
        val osArch: String,
        val onlinePlayers: Int,
        val maxPlayers: Int,
        val tps: DoubleArray,
        val plugins: Int,
    )
}
