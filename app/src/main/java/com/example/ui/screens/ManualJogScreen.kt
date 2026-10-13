package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.CoordinateReadoutCard
import com.example.ui.components.JogPad
import com.example.ui.components.SpindleFeedControl
import com.example.viewmodel.CncUiState
import com.example.viewmodel.CncViewModel

@Composable
fun ManualJogScreen(
    uiState: CncUiState,
    viewModel: CncViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .testTag("manual_jog_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Digital Readout (DRO)
        CoordinateReadoutCard(
            coordinates = uiState.activeCoords,
            displayMode = uiState.displayMode,
            aAxisEnabled = uiState.aAxisEnabled,
            onZeroAxis = { viewModel.zeroAxis(it) },
            onShowSetAxisDialog = { viewModel.showZeroDialog(it) },
            onZeroAll = { viewModel.zeroAllAxes() },
            onGotoOrigin = { viewModel.gotoWorkOrigin() },
            onProbeZ = { viewModel.runMacro(uiState.quickMacros.first { it.id == "m_probe" }) },
            onToggleAAxis = { viewModel.toggleAAxis() }
        )

        // Tactile Manual Jog Keypad
        JogPad(
            isStepMode = uiState.isStepMode,
            currentStepMm = uiState.stepDistanceMm,
            jogFeedRate = uiState.jogFeedRate,
            aAxisEnabled = uiState.aAxisEnabled,
            onSetJogMode = { viewModel.setJogMode(it) },
            onSetStepDistance = { viewModel.setStepDistance(it) },
            onSetJogFeed = { viewModel.setJogFeedRate(it) },
            onJogAxis = { axis, dir -> viewModel.jogAxis(axis, dir) },
            onCancelJog = { viewModel.cancelJog() }
        )

        // Spindle & Feed Overrides
        SpindleFeedControl(
            spindleOn = uiState.spindleOn,
            spindleCcw = uiState.spindleCcw,
            targetRpm = uiState.targetSpindleRpm,
            actualRpm = uiState.actualSpindleRpm,
            feedOverridePercent = uiState.feedOverridePercent,
            actualFeedRate = uiState.actualFeedRate,
            onToggleSpindle = { viewModel.toggleSpindle() },
            onSetSpindleDirection = { viewModel.setSpindleDirection(it) },
            onSetSpindleRpm = { viewModel.setTargetSpindleRpm(it) },
            onSetFeedOverride = { viewModel.setFeedOverride(it) }
        )
    }
}
