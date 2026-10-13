package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.connection.Esp32ControllerManager
import com.example.connection.GrblProtocol
import com.example.data.SamplePrograms
import com.example.model.AxisCoordinates
import com.example.model.ConnectionStatus
import com.example.model.CoordinateDisplayMode
import com.example.model.Esp32ConnectionConfig
import com.example.model.GCodeProgram
import com.example.model.LogMessage
import com.example.model.LogMessageType
import com.example.model.MachineState
import com.example.model.MacroItem
import com.example.parser.GCodeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CncViewModel(application: Application) : AndroidViewModel(application) {

    private val controllerManager = Esp32ControllerManager(application, viewModelScope)

    private val _uiState = MutableStateFlow(CncUiState())
    val uiState: StateFlow<CncUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private var jobExecutionJob: Job? = null
    private var jobTimerJob: Job? = null

    init {
        // Load sample CNC programs
        val samples = SamplePrograms.all
        val initialProgram = samples.firstOrNull()

        val defaultMacros = listOf(
            MacroItem("m_home", "Home All (\$H)", "\$H", "Perform machine homing sequence", "home"),
            MacroItem("m_unlock", "Unlock (\$X)", "\$X", "Clear GRBL alarm lock", "lock_open"),
            MacroItem("m_zero_all", "Zero Work WCS", "G10 L20 P1 X0 Y0 Z0", "Zero all workpiece axes at current position", "adjust"),
            MacroItem("m_safe_z", "Safe Z (+15mm)", "G91 G0 Z15\nG90", "Retract tool to safe clearance height", "arrow_upward"),
            MacroItem("m_origin", "Goto Origin (X0 Y0)", "G90 G0 Z10\nG0 X0 Y0\nG0 Z0", "Move tool safely to workpiece zero", "my_location"),
            MacroItem("m_probe", "Auto Z-Probe", "G91 G38.2 Z-30 F50", "Run mobile tool calibrator touch probe", "sensors"),
            MacroItem("m_spindle_warm", "Spindle Warmup", "M3 S8000\nG4 P3\nM3 S14000\nG4 P3\nM5", "Gradual spindle speed warmup sequence", "speed"),
            MacroItem("m_status", "Query Status (?)", "?", "Request real-time GRBL machine report", "info"),
            MacroItem("m_settings", "GRBL Settings (\$\$)", "\$\$", "Print controller EEPROM parameters", "settings")
        )

        _uiState.update {
            it.copy(
                availablePrograms = samples,
                selectedProgram = initialProgram,
                quickMacros = defaultMacros,
                totalJobLines = initialProgram?.lines?.size ?: 0
            )
        }

        // Connect by default to Simulator so the app is immediately live and testable
        controllerManager.connect(_uiState.value.connectionConfig)

        // Observe connection status
        viewModelScope.launch {
            controllerManager.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
                if (status == ConnectionStatus.CONNECTED) {
                    addLog("Controller connected in ${_uiState.value.connectionConfig.type.title}", LogMessageType.SYSTEM)
                } else if (status == ConnectionStatus.DISCONNECTED) {
                    addLog("Controller disconnected", LogMessageType.SYSTEM)
                }
            }
        }

        // Observe incoming stream from ESP32
        viewModelScope.launch {
            controllerManager.incomingMessages.collect { msg ->
                handleIncomingMessage(msg)
            }
        }
    }

    private fun handleIncomingMessage(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return

        // 1. Status Report
        if (trimmed.startsWith("<") && trimmed.endsWith(">")) {
            val report = GrblProtocol.parseStatusReport(trimmed)
            if (report != null) {
                _uiState.update { current ->
                    current.copy(
                        machineState = report.state,
                        mPos = report.mPos,
                        wPos = report.wPos,
                        actualFeedRate = report.feedRate,
                        actualSpindleRpm = report.spindleRpm
                    )
                }
            }
            return
        }

        // 2. Alarm Message
        if (trimmed.uppercase().contains("ALARM")) {
            _uiState.update { it.copy(machineState = MachineState.ALARM) }
            addLog(trimmed, LogMessageType.ALARM)
            return
        }

        // 3. Error Message
        if (trimmed.uppercase().startsWith("ERROR:")) {
            addLog(trimmed, LogMessageType.RESPONSE_ERROR)
            return
        }

        // 4. Standard Responses / OK / Info
        if (trimmed == "ok") {
            // Keep logs clean by only logging significant responses or batch
            addLog("ok", LogMessageType.RESPONSE_OK)
        } else {
            addLog(trimmed, LogMessageType.SYSTEM)
        }
    }

    private fun addLog(text: String, type: LogMessageType) {
        val timestamp = timeFormat.format(Date())
        val entry = LogMessage(
            timestamp = timestamp,
            message = text,
            type = type
        )
        _uiState.update { current ->
            // Keep recent 150 logs to prevent memory bloat
            val updated = (current.logs + entry).takeLast(150)
            current.copy(logs = updated)
        }
    }

    // --- MANUAL JOGGING CONTROLS ---

    fun setJogMode(stepMode: Boolean) {
        _uiState.update { it.copy(isStepMode = stepMode) }
    }

    fun setStepDistance(distMm: Float) {
        _uiState.update { it.copy(stepDistanceMm = distMm) }
    }

    fun setJogFeedRate(feed: Float) {
        _uiState.update { it.copy(jogFeedRate = feed) }
    }

    fun jogAxis(axis: String, direction: Int) {
        val state = _uiState.value
        val step = state.stepDistanceMm * direction
        val cmd = GrblProtocol.buildJogCommand(axis, step, state.jogFeedRate)
        sendTerminalCommand(cmd)
    }

    fun cancelJog() {
        controllerManager.sendRealtime(GrblProtocol.CMD_JOG_CANCEL)
    }

    // --- COORDINATES & ZEROING ---

    fun toggleCoordinateDisplayMode() {
        _uiState.update {
            it.copy(
                displayMode = if (it.displayMode == CoordinateDisplayMode.WORKPIECE)
                    CoordinateDisplayMode.MACHINE else CoordinateDisplayMode.WORKPIECE
            )
        }
    }

    fun toggleAAxis() {
        _uiState.update { it.copy(aAxisEnabled = !it.aAxisEnabled) }
    }

    fun zeroAxis(axis: String, customValue: Float = 0f) {
        val cmd = GrblProtocol.buildZeroAxisCommand(axis, customValue)
        sendTerminalCommand(cmd)
        addLog("Zeroed Axis $axis to $customValue mm", LogMessageType.SYSTEM)
    }

    fun zeroAllAxes() {
        val cmd = GrblProtocol.buildZeroAllCommand()
        sendTerminalCommand(cmd)
        addLog("Workpiece Origin Zeroed (X0 Y0 Z0)", LogMessageType.SYSTEM)
    }

    fun gotoWorkOrigin() {
        val cmd = GrblProtocol.buildGotoOriginCommand()
        sendTerminalCommand(cmd)
        addLog("Moving to Work Origin (Safe Z)", LogMessageType.SYSTEM)
    }

    fun showZeroDialog(axis: String?) {
        _uiState.update { it.copy(showZeroAxisDialog = axis) }
    }

    // --- SPINDLE & OVERRIDES ---

    fun toggleSpindle() {
        val state = _uiState.value
        val next = !state.spindleOn
        val cmd = GrblProtocol.buildSpindleCommand(next, state.targetSpindleRpm, state.spindleCcw)
        sendTerminalCommand(cmd)
        _uiState.update { it.copy(spindleOn = next) }
    }

    fun setSpindleDirection(ccw: Boolean) {
        _uiState.update { it.copy(spindleCcw = ccw) }
        if (_uiState.value.spindleOn) {
            val cmd = GrblProtocol.buildSpindleCommand(true, _uiState.value.targetSpindleRpm, ccw)
            sendTerminalCommand(cmd)
        }
    }

    fun setTargetSpindleRpm(rpm: Int) {
        _uiState.update { it.copy(targetSpindleRpm = rpm) }
        if (_uiState.value.spindleOn) {
            val cmd = GrblProtocol.buildSpindleCommand(true, rpm, _uiState.value.spindleCcw)
            sendTerminalCommand(cmd)
        }
    }

    fun setFeedOverride(percent: Int) {
        _uiState.update { it.copy(feedOverridePercent = percent) }
        // GRBL real-time override characters
        if (percent == 100) {
            controllerManager.sendRealtime(GrblProtocol.CMD_FEED_100)
        } else if (percent > 100) {
            controllerManager.sendRealtime(GrblProtocol.CMD_FEED_PLUS_10)
        } else {
            controllerManager.sendRealtime(GrblProtocol.CMD_FEED_MINUS_10)
        }
    }

    fun emergencyStop() {
        // Soft reset Ctrl+X
        controllerManager.sendRealtime(GrblProtocol.CMD_SOFT_RESET)
        stopJobExecution()
        _uiState.update {
            it.copy(
                machineState = MachineState.IDLE,
                isJobRunning = false,
                isJobPaused = false,
                spindleOn = false
            )
        }
        addLog("EMERGENCY STOP (Reset 0x18 sent)", LogMessageType.ALARM)
    }

    fun unlockAlarm() {
        sendTerminalCommand(GrblProtocol.CMD_UNLOCK_ALARM)
        addLog("Alarm lock cleared (\$X)", LogMessageType.SYSTEM)
    }

    fun homeMachine() {
        sendTerminalCommand(GrblProtocol.CMD_HOME_ALL)
        addLog("Machine homing started (\$H)", LogMessageType.SYSTEM)
    }

    // --- G-CODE JOB EXECUTION ---

    fun selectProgram(program: GCodeProgram) {
        if (_uiState.value.isJobRunning) return
        _uiState.update {
            it.copy(
                selectedProgram = program,
                currentLineIndex = 0,
                totalJobLines = program.lines.size,
                jobProgress = 0f,
                jobElapsedSeconds = 0L
            )
        }
    }

    fun loadCustomGCode(name: String, content: String) {
        val parsed = GCodeParser.parse(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            description = "Imported / Custom G-code file",
            rawGcode = content
        )
        _uiState.update { current ->
            val updated = current.availablePrograms + parsed
            current.copy(
                availablePrograms = updated,
                selectedProgram = parsed,
                currentLineIndex = 0,
                totalJobLines = parsed.lines.size,
                jobProgress = 0f,
                jobElapsedSeconds = 0L
            )
        }
        addLog("Loaded program: ${parsed.name} (${parsed.lines.size} lines)", LogMessageType.SYSTEM)
    }

    fun startJob() {
        val program = _uiState.value.selectedProgram ?: return
        if (_uiState.value.isJobRunning && _uiState.value.isJobPaused) {
            // Resume from pause
            controllerManager.sendRealtime(GrblProtocol.CMD_CYCLE_START)
            _uiState.update { it.copy(isJobPaused = false, machineState = MachineState.RUN) }
            addLog("Job Resumed (~)", LogMessageType.SYSTEM)
            return
        }

        _uiState.update {
            it.copy(
                isJobRunning = true,
                isJobPaused = false,
                currentLineIndex = 0,
                jobProgress = 0f,
                jobElapsedSeconds = 0L,
                machineState = MachineState.RUN
            )
        }
        addLog("Cycle Start: Machining ${program.name}", LogMessageType.SYSTEM)

        startJobTimer()
        startJobStream(program)
    }

    fun pauseJob() {
        controllerManager.sendRealtime(GrblProtocol.CMD_FEED_HOLD)
        _uiState.update { it.copy(isJobPaused = true, machineState = MachineState.HOLD) }
        addLog("Feed Hold: Job Paused (!)", LogMessageType.SYSTEM)
    }

    fun stopJob() {
        stopJobExecution()
        controllerManager.sendRealtime(GrblProtocol.CMD_SOFT_RESET)
        _uiState.update {
            it.copy(
                isJobRunning = false,
                isJobPaused = false,
                machineState = MachineState.IDLE
            )
        }
        addLog("Job Aborted by Operator", LogMessageType.SYSTEM)
    }

    private fun startJobStream(program: GCodeProgram) {
        jobExecutionJob?.cancel()
        jobExecutionJob = viewModelScope.launch(Dispatchers.Default) {
            val lines = program.lines
            val total = lines.size.coerceAtLeast(1)

            for (i in lines.indices) {
                while (_uiState.value.isJobPaused && isActive) {
                    delay(100)
                }
                if (!isActive || !_uiState.value.isJobRunning) break

                val line = lines[i].trim()
                if (line.isNotEmpty() && !line.startsWith("(") && !line.startsWith(";")) {
                    controllerManager.sendCommand(line)
                    // Pacing delay for smooth streaming
                    delay(35)
                }

                val progress = (i + 1).toFloat() / total.toFloat()
                _uiState.update {
                    it.copy(
                        currentLineIndex = i,
                        jobProgress = progress
                    )
                }
            }

            _uiState.update {
                it.copy(
                    isJobRunning = false,
                    isJobPaused = false,
                    jobProgress = 1f,
                    machineState = MachineState.IDLE
                )
            }
            jobTimerJob?.cancel()
            addLog("Machining Complete: ${program.name}", LogMessageType.SYSTEM)
        }
    }

    private fun startJobTimer() {
        jobTimerJob?.cancel()
        jobTimerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isJobRunning) {
                delay(1000)
                if (!_uiState.value.isJobPaused) {
                    _uiState.update { it.copy(jobElapsedSeconds = it.jobElapsedSeconds + 1) }
                }
            }
        }
    }

    private fun stopJobExecution() {
        jobExecutionJob?.cancel()
        jobExecutionJob = null
        jobTimerJob?.cancel()
        jobTimerJob = null
    }

    // --- MDI TERMINAL & COMMANDS ---

    fun onCommandInputChange(text: String) {
        _uiState.update { it.copy(currentCommandInput = text) }
    }

    fun sendTerminalCommand(command: String? = null) {
        val toSend = command ?: _uiState.value.currentCommandInput
        val trimmed = toSend.trim()
        if (trimmed.isEmpty()) return

        addLog(trimmed, LogMessageType.COMMAND_SENT)
        controllerManager.sendCommand(trimmed)

        if (command == null) {
            _uiState.update { it.copy(currentCommandInput = "") }
        }
    }

    fun runMacro(macro: MacroItem) {
        addLog("Macro: ${macro.title}", LogMessageType.SYSTEM)
        macro.gcode.lines().filter { it.isNotBlank() }.forEach { line ->
            sendTerminalCommand(line)
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    // --- CONNECTION SETTINGS ---

    fun updateConnectionConfig(config: Esp32ConnectionConfig) {
        _uiState.update { it.copy(connectionConfig = config) }
    }

    fun connectToEsp32() {
        stopJobExecution()
        controllerManager.connect(_uiState.value.connectionConfig)
    }

    fun disconnectEsp32() {
        stopJobExecution()
        controllerManager.disconnect()
    }

    // --- VISUALIZER ZOOM/PAN ---

    fun updateVisualizerZoom(zoom: Float) {
        _uiState.update { it.copy(visualizerZoom = zoom.coerceIn(0.2f, 10f)) }
    }

    fun updateVisualizerPan(dx: Float, dy: Float) {
        _uiState.update { it.copy(visualizerPanX = it.visualizerPanX + dx, visualizerPanY = it.visualizerPanY + dy) }
    }

    fun resetVisualizerView() {
        _uiState.update { it.copy(visualizerZoom = 1.0f, visualizerPanX = 0f, visualizerPanY = 0f) }
    }

    fun toggleVisualizerGrid() {
        _uiState.update { it.copy(showGrid = !it.showGrid) }
    }

    fun toggleVisualizerRapids() {
        _uiState.update { it.copy(showRapids = !it.showRapids) }
    }

    override fun onCleared() {
        super.onCleared()
        controllerManager.disconnect()
    }
}
