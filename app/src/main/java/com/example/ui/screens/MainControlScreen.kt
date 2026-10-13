package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import com.example.ui.components.ApkDownloadDialog
import com.example.ui.components.MachineHeaderBar
import com.example.ui.components.ZeroingDialog
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurface
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextSecondary
import com.example.viewmodel.CncUiState
import com.example.viewmodel.CncViewModel

enum class CncTab(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    JOG("DRO & Jog", Icons.Filled.Gamepad, Icons.Outlined.Gamepad),
    AUTO_JOB("Auto Job", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle),
    MDI("MDI Console", Icons.Filled.Code, Icons.Outlined.Code),
    SETTINGS("ESP32 Setup", Icons.Filled.Router, Icons.Outlined.Router)
}

@Composable
fun MainControlScreen(
    uiState: CncUiState,
    viewModel: CncViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showApkDownloadDialog by rememberSaveable { mutableStateOf(false) }

    // If on a secondary tab, pressing back returns to the main DRO & Jog tab
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NcDarkBackground,
        topBar = {
            MachineHeaderBar(
                machineState = uiState.machineState,
                connectionStatus = uiState.connectionStatus,
                connectionTitle = when (uiState.connectionConfig.type) {
                    com.example.model.ConnectionType.SIMULATOR -> "Virtual Simulator"
                    else -> "${uiState.connectionConfig.hostIp}:${uiState.connectionConfig.port}"
                },
                displayMode = uiState.displayMode,
                onToggleDisplayMode = { viewModel.toggleCoordinateDisplayMode() },
                onEmergencyStop = { viewModel.emergencyStop() },
                onUnlockAlarm = { viewModel.unlockAlarm() },
                onHomeMachine = { viewModel.homeMachine() },
                onDownloadApk = { showApkDownloadDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NcDarkSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("cnc_bottom_navigation")
            ) {
                CncTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                            selectedTextColor = NcPrimaryCyan,
                            indicatorColor = NcPrimaryCyan,
                            unselectedIconColor = NcTextMuted,
                            unselectedTextColor = NcTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> ManualJogScreen(uiState = uiState, viewModel = viewModel)
                1 -> AutoJobScreen(uiState = uiState, viewModel = viewModel)
                2 -> MdiConsoleScreen(uiState = uiState, viewModel = viewModel)
                3 -> Esp32ConnectionScreen(uiState = uiState, viewModel = viewModel)
            }
        }
    }

    // Zero Axis Custom Value Dialog
    uiState.showZeroAxisDialog?.let { axis ->
        ZeroingDialog(
            axis = axis,
            onDismiss = { viewModel.showZeroDialog(null) },
            onConfirm = { customValue ->
                viewModel.zeroAxis(axis, customValue)
                viewModel.showZeroDialog(null)
            }
        )
    }

    // Standalone APK Package Download & Export Dialog
    if (showApkDownloadDialog) {
        ApkDownloadDialog(
            onDismiss = { showApkDownloadDialog = false }
        )
    }
}
