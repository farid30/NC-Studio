package com.example.connection

import com.example.model.AxisCoordinates
import com.example.model.MachineState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

class Esp32Simulator(
    private val scope: CoroutineScope
) {
    private var state = MachineState.IDLE
    private var mX = 0f
    private var mY = 0f
    private var mZ = 0f
    private var mA = 0f

    private var wcoX = 0f
    private var wcoY = 0f
    private var wcoZ = 0f
    private var wcoA = 0f

    private var feedRate = 1200f
    private var spindleRpm = 0f
    private var spindleOn = false
    private var isCcw = false

    private var targetX = 0f
    private var targetY = 0f
    private var targetZ = 0f
    private var targetA = 0f

    private var motionJob: Job? = null
    private var tickerJob: Job? = null

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    fun start() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            emitLine("Grbl 1.1h ['$' for help] [ESP32-GRBL / FluidNC v3.7.8]")
            emitLine("[MSG:Mode: FluidNC 4-Axis Router ESP32-WROOM-32D]")
            emitLine("ok")

            while (isActive) {
                delay(250)
                emitLine(buildStatusString())
            }
        }
    }

    fun stop() {
        tickerJob?.cancel()
        motionJob?.cancel()
        tickerJob = null
        motionJob = null
    }

    private suspend fun emitLine(line: String) {
        _incomingMessages.emit(line)
    }

    fun send(command: String) {
        scope.launch(Dispatchers.Default) {
            val cmd = command.trim()
            when {
                cmd == "?" -> {
                    emitLine(buildStatusString())
                }
                cmd == "!" -> {
                    if (state == MachineState.RUN || state == MachineState.JOG) {
                        state = MachineState.HOLD
                        emitLine(buildStatusString())
                    }
                }
                cmd == "~" -> {
                    if (state == MachineState.HOLD) {
                        state = MachineState.RUN
                        emitLine(buildStatusString())
                    }
                }
                cmd == "\u0018" -> { // Soft Reset (0x18)
                    motionJob?.cancel()
                    state = MachineState.IDLE
                    spindleOn = false
                    spindleRpm = 0f
                    emitLine("\nGrbl 1.1h ['$' for help]")
                    emitLine("[MSG:Soft Reset - Ready]")
                    emitLine("ok")
                }
                cmd == "\u0085" -> { // Jog Cancel (0x85)
                    motionJob?.cancel()
                    if (state == MachineState.JOG) {
                        state = MachineState.IDLE
                    }
                    emitLine("ok")
                }
                cmd == "\$X" -> {
                    state = MachineState.IDLE
                    emitLine("[MSG:Caution: Unlocked]")
                    emitLine("ok")
                }
                cmd == "\$H" -> {
                    simulateHoming()
                }
                cmd == "\$\$" -> {
                    emitGrblSettings()
                }
                cmd == "\$I" -> {
                    emitLine("[VER:1.1h.20240901:ESP32-NC-STUDIO]")
                    emitLine("[OPT:V,M,P,S,W,B]")
                    emitLine("ok")
                }
                cmd.startsWith("\$J=") -> {
                    executeJog(cmd)
                }
                cmd.startsWith("G10 L20 P1") -> {
                    executeZeroOrSet(cmd)
                }
                cmd.startsWith("G92") -> {
                    executeG92(cmd)
                }
                else -> {
                    executeGCode(cmd)
                }
            }
        }
    }

    private suspend fun executeJog(cmd: String) {
        state = MachineState.JOG
        val upper = cmd.uppercase()
        var dx = 0f
        var dy = 0f
        var dz = 0f

        val xMatch = Regex("X([+-]?[0-9]*\\.?[0-9]+)").find(upper)
        val yMatch = Regex("Y([+-]?[0-9]*\\.?[0-9]+)").find(upper)
        val zMatch = Regex("Z([+-]?[0-9]*\\.?[0-9]+)").find(upper)

        xMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { dx = it }
        yMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { dy = it }
        zMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { dz = it }

        targetX = mX + dx
        targetY = mY + dy
        targetZ = mZ + dz

        simulateMotionTo(targetX, targetY, targetZ, 80)
        state = MachineState.IDLE
        emitLine("ok")
    }

    private suspend fun executeZeroOrSet(cmd: String) {
        val upper = cmd.uppercase()
        val xMatch = Regex("X([+-]?[0-9]*\\.?[0-9]+)").find(upper)
        val yMatch = Regex("Y([+-]?[0-9]*\\.?[0-9]+)").find(upper)
        val zMatch = Regex("Z([+-]?[0-9]*\\.?[0-9]+)").find(upper)

        xMatch?.groupValues?.get(1)?.toFloatOrNull()?.let {
            wcoX = mX - it
        }
        yMatch?.groupValues?.get(1)?.toFloatOrNull()?.let {
            wcoY = mY - it
        }
        zMatch?.groupValues?.get(1)?.toFloatOrNull()?.let {
            wcoZ = mZ - it
        }
        emitLine("ok")
    }

    private suspend fun executeG92(cmd: String) {
        val upper = cmd.uppercase()
        val xMatch = Regex("X([+-]?[0-9]*\\.?[0-9]+)").find(upper)
        val yMatch = Regex("Y([+-]?[0-9]*\\.?[0-9]+)").find(upper)
        val zMatch = Regex("Z([+-]?[0-9]*\\.?[0-9]+)").find(upper)

        xMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { wcoX = mX - it }
        yMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { wcoY = mY - it }
        zMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { wcoZ = mZ - it }
        emitLine("ok")
    }

    private suspend fun simulateHoming() {
        state = MachineState.HOME
        emitLine("[MSG:Homing cycle initiated...]")
        // First home Z
        simulateMotionTo(mX, mY, 0f, 150)
        // Then home XY
        simulateMotionTo(0f, 0f, 0f, 250)
        mX = 0f
        mY = 0f
        mZ = 0f
        state = MachineState.IDLE
        emitLine("ok")
    }

    private suspend fun executeGCode(cmd: String) {
        val upper = cmd.uppercase()

        // Spindle commands
        if (upper.contains("M3") || upper.contains("M03")) {
            spindleOn = true
            isCcw = false
        }
        if (upper.contains("M4") || upper.contains("M04")) {
            spindleOn = true
            isCcw = true
        }
        if (upper.contains("M5") || upper.contains("M05")) {
            spindleOn = false
            spindleRpm = 0f
        }
        val sMatch = Regex("S([0-9]+)").find(upper)
        sMatch?.groupValues?.get(1)?.toFloatOrNull()?.let {
            spindleRpm = it
        }
        val fMatch = Regex("F([0-9]+)").find(upper)
        fMatch?.groupValues?.get(1)?.toFloatOrNull()?.let {
            feedRate = it
        }

        // Motion commands
        if (upper.contains("G0") || upper.contains("G1") || upper.contains("G00") || upper.contains("G01")) {
            state = MachineState.RUN
            val xVal = Regex("X([+-]?[0-9]*\\.?[0-9]+)").find(upper)?.groupValues?.get(1)?.toFloatOrNull()
            val yVal = Regex("Y([+-]?[0-9]*\\.?[0-9]+)").find(upper)?.groupValues?.get(1)?.toFloatOrNull()
            val zVal = Regex("Z([+-]?[0-9]*\\.?[0-9]+)").find(upper)?.groupValues?.get(1)?.toFloatOrNull()

            val nX = if (xVal != null) xVal + wcoX else mX
            val nY = if (yVal != null) yVal + wcoY else mY
            val nZ = if (zVal != null) zVal + wcoZ else mZ

            simulateMotionTo(nX, nY, nZ, 40)
            state = MachineState.IDLE
        }

        emitLine("ok")
    }

    private suspend fun simulateMotionTo(destX: Float, destY: Float, destZ: Float, steps: Int) {
        val startX = mX
        val startY = mY
        val startZ = mZ

        val stepCount = steps.coerceIn(5, 50)
        for (i in 1..stepCount) {
            val fraction = i.toFloat() / stepCount.toFloat()
            mX = startX + (destX - startX) * fraction
            mY = startY + (destY - startY) * fraction
            mZ = startZ + (destZ - startZ) * fraction
            delay(12)
        }
        mX = destX
        mY = destY
        mZ = destZ
    }

    private suspend fun emitGrblSettings() {
        val settings = listOf(
            "$0=10 (Step pulse time, microseconds)",
            "$1=25 (Step idle delay, milliseconds)",
            "$2=0 (Step pulse invert, mask)",
            "$3=0 (Step direction invert, mask)",
            "$10=1 (Status report options, mask: MPos/WPos)",
            "$11=0.010 (Junction deviation, mm)",
            "$12=0.002 (Arc tolerance, mm)",
            "$20=0 (Soft limits enable, boolean)",
            "$21=0 (Hard limits enable, boolean)",
            "$22=1 (Homing cycle enable, boolean)",
            "$30=24000 (Maximum spindle speed, RPM)",
            "$31=0 (Minimum spindle speed, RPM)",
            "$32=0 (Laser-mode enable, boolean)",
            "$100=800.000 (X-axis travel resolution, step/mm)",
            "$101=800.000 (Y-axis travel resolution, step/mm)",
            "$102=1600.000 (Z-axis travel resolution, step/mm)",
            "$110=4000.000 (X-axis maximum rate, mm/min)",
            "$111=4000.000 (Y-axis maximum rate, mm/min)",
            "$112=1500.000 (Z-axis maximum rate, mm/min)",
            "$120=250.000 (X-axis acceleration, mm/sec^2)",
            "$121=250.000 (Y-axis acceleration, mm/sec^2)",
            "$122=100.000 (Z-axis acceleration, mm/sec^2)",
            "$130=400.000 (X-axis maximum travel, mm)",
            "$131=600.000 (Y-axis maximum travel, mm)",
            "$132=100.000 (Z-axis maximum travel, mm)"
        )
        for (s in settings) {
            emitLine(s)
        }
        emitLine("ok")
    }

    private fun buildStatusString(): String {
        val wX = mX - wcoX
        val wY = mY - wcoY
        val wZ = mZ - wcoZ
        val curRpm = if (spindleOn) spindleRpm.toInt() else 0

        return String.format(
            Locale.US,
            "<%s|MPos:%.3f,%.3f,%.3f,%.3f|WPos:%.3f,%.3f,%.3f,%.3f|FS:%.0f,%d|Pn:P>",
            state.name,
            mX, mY, mZ, mA,
            wX, wY, wZ, mA,
            feedRate, curRpm
        )
    }
}
