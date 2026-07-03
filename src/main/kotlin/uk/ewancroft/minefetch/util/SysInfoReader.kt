package uk.ewancroft.minefetch.util

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class HostInfo(
    val osPretty: String,
    val kernel: String,
    val cpu: String,
    val memoryTotal: String,
    val memoryUsed: String,
    val memoryPercent: String,
    val diskTotal: String,
    val diskUsed: String,
    val diskPercent: String,
    val uptime: String,
    val packages: String,
)

class SysInfoReader(private val path: String) {

    fun readHostInfo(): HostInfo? {
        val file = File(path)
        if (!file.exists()) return null

        return try {
            val text = file.readText().trim()
            if (text.isEmpty() || text == "null") return null
            val arr = JSONArray(text)
            parseFastfetchJson(arr)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseFastfetchJson(arr: JSONArray): HostInfo {
        val map = mutableMapOf<String, Any>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            map[obj.getString("type")] = obj.get("result")
        }

        val os = parseOs(map)
        val kernel = parseKernel(map)
        val cpu = parseCpu(map)
        val mem = parseMemory(map)
        val disk = parseDisk(map)
        val uptime = parseUptime(map)
        val packages = parsePackages(map)

        return HostInfo(
            osPretty = os,
            kernel = kernel,
            cpu = cpu,
            memoryTotal = mem.second,
            memoryUsed = mem.first,
            memoryPercent = mem.third,
            diskTotal = disk.second,
            diskUsed = disk.first,
            diskPercent = disk.third,
            uptime = uptime,
            packages = packages,
        )
    }

    private fun parseOs(map: Map<String, Any>): String {
        val osObj = map["OS"] as? JSONObject ?: return "Unknown"
        val name = osObj.optString("prettyName", osObj.optString("name", "Unknown"))
        val arch = (map["Kernel"] as? JSONObject)?.optString("architecture", "")
        return if (arch.isNullOrEmpty()) name else "$name ($arch)"
    }

    private fun parseKernel(map: Map<String, Any>): String {
        val k = map["Kernel"] as? JSONObject ?: return "Unknown"
        val name = k.optString("name", "Unknown")
        val release = k.optString("release", "")
        return if (release.isEmpty()) name else "$name $release"
    }

    private fun parseCpu(map: Map<String, Any>): String {
        val c = map["CPU"] as? JSONObject ?: return "Unknown"
        val name = c.optString("cpu", "Unknown")
        val cores = c.optJSONObject("cores")?.optInt("physical", 0) ?: 0
        val freqHz = c.optJSONObject("frequency")?.optLong("max", 0) ?: 0L
        val freqGhz = if (freqHz > 0) "%.2f GHz".format(freqHz / 1000.0) else ""
        return buildString {
            append(name)
            if (cores > 0) append(" ($cores)")
            if (freqGhz.isNotEmpty()) append(" @ $freqGhz")
        }
    }

    private fun parseMemory(map: Map<String, Any>): Triple<String, String, String> {
        val m = map["Memory"] as? JSONObject ?: return Triple("", "", "")
        val total = m.optLong("total", 0)
        val used = m.optLong("used", 0)
        val percent = if (total > 0) "${used * 100 / total}%" else ""
        return Triple(formatBytes(used), formatBytes(total), percent)
    }

    private fun parseDisk(map: Map<String, Any>): Triple<String, String, String> {
        val disks = map["Disk"] as? JSONArray ?: return Triple("", "", "")
        val disk = findMainDisk(disks) ?: return Triple("", "", "")
        val bytes = disk.optJSONObject("bytes") ?: return Triple("", "", "")
        val total = bytes.optLong("total", 0)
        val used = bytes.optLong("used", 0)
        val percent = if (total > 0) "${used * 100 / total}%" else ""
        return Triple(formatBytes(used), formatBytes(total), percent)
    }

    private fun findMainDisk(disks: JSONArray): JSONObject? {
        for (i in 0 until disks.length()) {
            val d = disks.getJSONObject(i)
            val mp = d.optString("mountpoint", "")
            if (mp == "/") return d
        }
        return if (disks.length() > 0) disks.getJSONObject(0) else null
    }

    private fun parseUptime(map: Map<String, Any>): String {
        val u = map["Uptime"] as? JSONObject ?: return "Unknown"
        val seconds = u.optLong("uptime", 0)
        if (seconds <= 0) return "Unknown"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            append("${minutes}m")
        }.trim()
    }

    private fun parsePackages(map: Map<String, Any>): String {
        val p = map["Packages"] as? JSONObject ?: return ""
        val all = p.optInt("all", 0)
        return if (all > 0) "$all" else ""
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
