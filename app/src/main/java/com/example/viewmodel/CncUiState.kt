package com.example.viewmodel

import com.example.model.AxisCoordinates
import com.example.model.ConnectionStatus
import com.example.model.CoordinateDisplayMode
import com.example.model.Esp32ConnectionConfig
import com.example.model.GCodeProgram
import com.example.model.LogMessage
import com.example.model.MachineState
import com.example.model.MacroItem

data class CncUiState(
    // Coordinates
    val wPos: AxisCoordinates = AxisCoordinates(0f, 0f, 0f, 0f),
    val mPos: AxisCoordinates = AxisCoordinates(0f, 0f, 0f, 0f),
    val displayMode: CoordinateDisplayMode = CoordinateDisplayMode.WORKPIECE,
    val aAxisEnabled: Boolean = false,

    // Machine Status
    val machineState: MachineState = MachineState.IDLE,
    val actualFeedRate: Float = 0f,
    val actualSpindleRpm: Float = 0f,
    val targetSpindleRpm: Int = 12000,
    val spindleOn: Boolean = false,
    val spindleCcw: Boolean = false,
    val feedOverridePercent: Int = 100,
    val rapidOverridePercent: Int = 100,
    val coolantFlood: Boolean = false,
    val coolantMist: Boolean = false,

    // Manual Jog Controls
    val isStepMode: Boolean = true,
    val stepDistanceMm: Float = 1.0f,
    val jogFeedRate: Float = 1500f,

    // G-Code Job Execution
    val availablePrograms: List<GCodeProgram> = emptyList(),
    val selectedProgram: GCodeProgram? = null,
    val isJobRunning: Boolean = false,
    val isJobPaused: Boolean = false,
    val currentLineIndex: Int = 0,
    val totalJobLines: Int = 0,
    val jobProgress: Float = 0f,
    val jobElapsedSeconds: Long = 0L,

    // MDI & Terminal
    val logs: List<LogMessage> = emptyList(),
    val currentCommandInput: String = "",
    val quickMacros: List<MacroItem> = emptyList(),

    // Connection
    val connectionConfig: Esp32ConnectionConfig = Esp32ConnectionConfig(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,

    // Toolpath Visualizer view state
    val visualizerZoom: Float = 1.0f,
    val visualizerPanX: Float = 0f,
    val visualizerPanY: Float = 0f,
    val showGrid: Boolean = true,
    val showRapids: Boolean = true,
    val followTool: Boolean = true,

    // UI Dialogs
    val showZeroAxisDialog: String? = null // null or "X", "Y", "Z", "A"
) {
    val activeCoords: AxisCoordinates
        get() = if (displayMode == CoordinateDisplayMode.WORKPIECE) wPos else mPos
}
