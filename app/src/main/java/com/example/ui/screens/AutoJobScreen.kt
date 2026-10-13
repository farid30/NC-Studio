package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GCodeProgram
import com.example.ui.components.ToolpathCanvas
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurfaceElevated
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcRedEmergency
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTertiaryGreen
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary
import com.example.ui.theme.NcTextSecondary
import com.example.viewmodel.CncUiState
import com.example.viewmodel.CncViewModel
import java.util.Locale

@Composable
fun AutoJobScreen(
    uiState: CncUiState,
    viewModel: CncViewModel,
    modifier: Modifier = Modifier
) {
    var showImportDialog by remember { mutableStateOf(false) }
    val gcodeListState = rememberLazyListState()

    // Auto-scroll G-code line viewer to current line during execution
    LaunchedEffect(uiState.currentLineIndex) {
        if (uiState.isJobRunning && uiState.currentLineIndex > 2) {
            gcodeListState.animateScrollToItem((uiState.currentLineIndex - 2).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("auto_job_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Program Selection Horizontal Chip Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            uiState.availablePrograms.forEach { prog ->
                val isSelected = uiState.selectedProgram?.id == prog.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectProgram(prog) },
                    label = {
                        Text(
                            text = prog.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NcPrimaryCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = NcDarkSurfaceVariant,
                        labelColor = NcTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = NcBorder,
                        selectedBorderColor = NcPrimaryCyan
                    ),
                    modifier = Modifier.testTag("program_chip_${prog.id}")
                )
            }

            // Custom G-Code Import / Paste button
            OutlinedButton(
                onClick = { showImportDialog = true },
                modifier = Modifier
                    .height(32.dp)
                    .testTag("import_gcode_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NcSecondaryAmber),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New G-Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2D Toolpath Canvas Visualizer
        ToolpathCanvas(
            program = uiState.selectedProgram,
            currentLineIndex = uiState.currentLineIndex,
            toolCoordinates = uiState.wPos,
            zoomLevel = uiState.visualizerZoom,
            panX = uiState.visualizerPanX,
            panY = uiState.visualizerPanY,
            showGrid = uiState.showGrid,
            showRapids = uiState.showRapids,
            onZoomChange = { viewModel.updateVisualizerZoom(it) },
            onPanChange = { dx, dy -> viewModel.updateVisualizerPan(dx, dy) },
            onResetView = { viewModel.resetVisualizerView() },
            onToggleGrid = { viewModel.toggleVisualizerGrid() },
            onToggleRapids = { viewModel.toggleVisualizerRapids() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Job Machining Execution Control Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("job_execution_card"),
            colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Progress Bar and Percent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROGRESS: ${(uiState.jobProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (uiState.isJobRunning) NcPrimaryCyan else NcTextSecondary
                    )

                    val elapsedMin = uiState.jobElapsedSeconds / 60
                    val elapsedSec = uiState.jobElapsedSeconds % 60
                    val totalEst = uiState.selectedProgram?.estimatedSeconds ?: 0L
                    val estMin = totalEst / 60
                    val estSec = totalEst % 60

                    Text(
                        text = String.format(Locale.US, "%02d:%02d / %02d:%02d", elapsedMin, elapsedSec, estMin, estSec),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = NcSecondaryAmber
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { uiState.jobProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("job_progress_bar"),
                    color = NcPrimaryCyan,
                    trackColor = NcDarkBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Machining Controls (Cycle Start / Feed Hold / Stop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cycle Start / Resume Button
                    Button(
                        onClick = { viewModel.startJob() },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("cycle_start_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcTertiaryGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isJobPaused) "RESUME" else "CYCLE START",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    // Feed Hold / Pause Button
                    Button(
                        onClick = { viewModel.pauseJob() },
                        enabled = uiState.isJobRunning && !uiState.isJobPaused,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("feed_hold_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcSecondaryAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "HOLD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Stop / Abort Button
                    Button(
                        onClick = { viewModel.stopJob() },
                        enabled = uiState.isJobRunning || uiState.isJobPaused,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("abort_job_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcRedEmergency,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ABORT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // G-Code Program Viewer (Scrollable lines with line numbers)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .testTag("gcode_viewer_card"),
            colors = CardDefaults.cardColors(containerColor = NcDarkBackground),
            shape = RoundedCornerShape(8.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
        ) {
            val lines = uiState.selectedProgram?.lines ?: emptyList()
            if (lines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No program loaded", color = NcTextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    state = gcodeListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    itemsIndexed(lines) { index, line ->
                        val isExecuting = index == uiState.currentLineIndex && uiState.isJobRunning

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isExecuting) NcPrimaryCyan.copy(alpha = 0.25f) else Color.Transparent)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(Locale.US, "%04d: ", index + 1),
                                color = if (isExecuting) NcPrimaryCyan else NcTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isExecuting) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = line,
                                color = if (isExecuting) NcPrimaryCyan else if (line.startsWith("(") || line.startsWith(";")) NcTextMuted else NcTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isExecuting) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    // Custom G-Code Import Dialog
    if (showImportDialog) {
        ImportGCodeDialog(
            onDismiss = { showImportDialog = false },
            onImport = { name, code ->
                viewModel.loadCustomGCode(name, code)
                showImportDialog = false
            }
        )
    }
}

@Composable
private fun ImportGCodeDialog(
    onDismiss: () -> Unit,
    onImport: (name: String, content: String) -> Unit
) {
    var programName by remember { mutableStateOf("Custom Cut 01") }
    var gcodeContent by remember {
        mutableStateOf(
            """
            G21 ; mm
            G90 ; Absolute
            M3 S12000
            G0 Z5.000
            G0 X0 Y0
            G1 Z-1.000 F400
            G1 X30.000 Y0.000 F1000
            G1 X30.000 Y30.000
            G1 X0.000 Y30.000
            G1 X0.000 Y0.000
            G0 Z10.000
            M5
            M30
            """.trimIndent()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NcDarkSurfaceVariant,
        title = {
            Text("Load Custom G-Code", fontWeight = FontWeight.Bold, color = NcPrimaryCyan)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = programName,
                    onValueChange = { programName = it },
                    label = { Text("Program Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NcPrimaryCyan,
                        unfocusedBorderColor = NcBorder,
                        focusedTextColor = NcTextPrimary,
                        unfocusedTextColor = NcTextPrimary
                    )
                )

                OutlinedTextField(
                    value = gcodeContent,
                    onValueChange = { gcodeContent = it },
                    label = { Text("G-Code Text") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NcPrimaryCyan,
                        unfocusedBorderColor = NcBorder,
                        focusedTextColor = NcTextPrimary,
                        unfocusedTextColor = NcTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(programName, gcodeContent) },
                colors = ButtonDefaults.buttonColors(containerColor = NcPrimaryCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Load & Parse", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NcTextMuted)
            }
        }
    )
}
