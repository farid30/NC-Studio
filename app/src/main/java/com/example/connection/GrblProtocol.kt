package com.example.connection

import com.example.model.AxisCoordinates
import com.example.model.MachineState
import java.util.Locale

data class GrblStatusReport(
    val state: MachineState,
    val mPos: AxisCoordinates,
    val wPos: AxisCoordinates,
    val feedRate: Float,
    val spindleRpm: Float,
    val rawReport: String
)

object GrblProtocol {

    const val CMD_STATUS_QUERY = "?"
    const val CMD_FEED_HOLD = "!"
    const val CMD_CYCLE_START = "~"
    const val CMD_SOFT_RESET = "\u0018" // Ctrl+X (0x18)
    const val CMD_JOG_CANCEL = "\u0085" // 0x85
    const val CMD_UNLOCK_ALARM = "\$X"
    const val CMD_HOME_ALL = "\$H"
    const val CMD_VIEW_SETTINGS = "\$\$"
    const val CMD_BUILD_INFO = "\$I"

    // Realtime overrides
    const val CMD_FEED_100 = "\u0090" // 0x90
    const val CMD_FEED_PLUS_10 = "\u0091" // 0x91
    const val CMD_FEED_MINUS_10 = "\u0092" // 0x92
    const val CMD_SPINDLE_PLUS_10 = "\u0099" // 0x99
    const val CMD_SPINDLE_MINUS_10 = "\u009A" // 0x9A
    const val CMD_SPINDLE_100 = "\u009B" // 0x9B

    fun buildJogCommand(axis: String, stepDistanceMm: Float, feedRateMmMin: Float): String {
        return String.format(Locale.US, "\$J=G91 G21 %s%.3f F%.0f", axis.uppercase(), stepDistanceMm, feedRateMmMin)
    }

    fun buildZeroAxisCommand(axis: String, value: Float = 0f): String {
        // G10 L20 P1 sets the current WCS (G54) coordinate
        return String.format(Locale.US, "G10 L20 P1 %s%.3f", axis.uppercase(), value)
    }

    fun buildZeroAllCommand(): String {
        return "G10 L20 P1 X0 Y0 Z0"
    }

    fun buildGotoOriginCommand(safeZ: Float = 10f): String {
        // Safe sequence: lift Z to safe height, move XY to 0,0, then move Z to origin
        return "G90 G0 Z${safeZ}\nG0 X0 Y0\nG0 Z0"
    }

    fun buildSpindleCommand(on: Boolean, rpm: Int, isCcw: Boolean = false): String {
        return if (on) {
            val dir = if (isCcw) "M4" else "M3"
            "$dir S$rpm"
        } else {
            "M5"
        }
    }

    fun buildProbeZCommand(maxDistanceMm: Float = 30f, feedRate: Float = 50f): String {
        return String.format(Locale.US, "G91 G38.2 Z-%.1f F%.0f", maxDistanceMm, feedRate)
    }

    /**
     * Parses standard GRBL status string:
     * e.g. <Idle|MPos:10.000,20.000,-5.000|WPos:10.000,20.000,-5.000|FS:1200,12000>
     * or <Run|MPos:5.000,5.000,0.000|WCO:0.000,0.000,0.000|FS:800,10000>
     */
    fun parseStatusReport(report: String): GrblStatusReport? {
        val trimmed = report.trim()
        if (!trimmed.startsWith("<") || !trimmed.endsWith(">")) return null
        val inner = trimmed.substring(1, trimmed.length - 1)
        val parts = inner.split("|")
        if (parts.isEmpty()) return null

        val state = MachineState.fromGrblString(parts[0])
        var mPos = AxisCoordinates()
        var wPos = AxisCoordinates()
        var wco = AxisCoordinates()
        var hasWPos = false
        var feedRate = 0f
        var spindleRpm = 0f

        for (i in 1 until parts.size) {
            val part = parts[i]
            when {
                part.startsWith("MPos:") -> {
                    val coords = part.substring(5).split(",")
                    val x = coords.getOrNull(0)?.toFloatOrNull() ?: 0f
                    val y = coords.getOrNull(1)?.toFloatOrNull() ?: 0f
                    val z = coords.getOrNull(2)?.toFloatOrNull() ?: 0f
                    val a = coords.getOrNull(3)?.toFloatOrNull() ?: 0f
                    mPos = AxisCoordinates(x, y, z, a)
                }
                part.startsWith("WPos:") -> {
                    val coords = part.substring(5).split(",")
                    val x = coords.getOrNull(0)?.toFloatOrNull() ?: 0f
                    val y = coords.getOrNull(1)?.toFloatOrNull() ?: 0f
                    val z = coords.getOrNull(2)?.toFloatOrNull() ?: 0f
                    val a = coords.getOrNull(3)?.toFloatOrNull() ?: 0f
                    wPos = AxisCoordinates(x, y, z, a)
                    hasWPos = true
                }
                part.startsWith("WCO:") -> {
                    val coords = part.substring(4).split(",")
                    val x = coords.getOrNull(0)?.toFloatOrNull() ?: 0f
                    val y = coords.getOrNull(1)?.toFloatOrNull() ?: 0f
                    val z = coords.getOrNull(2)?.toFloatOrNull() ?: 0f
                    val a = coords.getOrNull(3)?.toFloatOrNull() ?: 0f
                    wco = AxisCoordinates(x, y, z, a)
                }
                part.startsWith("FS:") -> {
                    val fs = part.substring(3).split(",")
                    feedRate = fs.getOrNull(0)?.toFloatOrNull() ?: 0f
                    spindleRpm = fs.getOrNull(1)?.toFloatOrNull() ?: 0f
                }
                part.startsWith("F:") -> {
                    feedRate = part.substring(2).toFloatOrNull() ?: 0f
                }
            }
        }

        if (!hasWPos) {
            // WPos = MPos - WCO
            wPos = AxisCoordinates(
                x = mPos.x - wco.x,
                y = mPos.y - wco.y,
                z = mPos.z - wco.z,
                a = mPos.a - wco.a
            )
        }

        return GrblStatusReport(
            state = state,
            mPos = mPos,
            wPos = wPos,
            feedRate = feedRate,
            spindleRpm = spindleRpm,
            rawReport = trimmed
        )
    }
}
