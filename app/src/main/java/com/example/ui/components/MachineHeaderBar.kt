package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.ConnectionStatus
import com.example.model.CoordinateDisplayMode
import com.example.model.MachineState
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkSurfaceElevated
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcRedEmergency
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTertiaryGreen
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary
import com.example.ui.theme.NcTextSecondary

@Composable
fun MachineHeaderBar(
    machineState: MachineState,
    connectionStatus: ConnectionStatus,
    connectionTitle: String,
    displayMode: CoordinateDisplayMode,
    onToggleDisplayMode: () -> Unit,
    onEmergencyStop: () -> Unit,
    onUnlockAlarm: () -> Unit,
    onHomeMachine: () -> Unit,
    onDownloadApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("machine_header_bar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Connection and Machine Status badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Connection Status indicator dot
                    val statusColor = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> NcTertiaryGreen
                        ConnectionStatus.CONNECTING -> NcSecondaryAmber
                        ConnectionStatus.DISCONNECTED -> NcTextMuted
                        ConnectionStatus.ERROR -> NcRedEmergency
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )

                    Column {
                        Text(
                            text = "NC STUDIO ESP32",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = NcPrimaryCyan
                        )
                        Text(
                            text = connectionTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = NcTextSecondary,
                            maxLines = 1
                        )
                    }

                    // Machine State pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(machineState.badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, machineState.badgeColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = machineState.label,
                            color = machineState.badgeColor,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Header Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Coordinate mode badge toggle (WCS vs MCS)
                    OutlinedButton(
                        onClick = onToggleDisplayMode,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("toggle_coord_mode_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NcPrimaryCyan
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = displayMode.shortName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Emergency Stop button (High priority)
                    Button(
                        onClick = onEmergencyStop,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("emergency_stop_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcRedEmergency,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "E-STOP",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    // Overflow Menu Button (⋮)
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("header_overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = NcTextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(NcDarkSurfaceElevated)
                                .border(1.dp, NcBorder, RoundedCornerShape(4.dp))
                                .testTag("header_dropdown_menu")
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Download APK File",
                                            fontWeight = FontWeight.Bold,
                                            color = NcPrimaryCyan
                                        )
                                        Text(
                                            text = "Save / Export standalone package",
                                            fontSize = 10.sp,
                                            color = NcTextSecondary
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = NcPrimaryCyan
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDownloadApk()
                                },
                                modifier = Modifier.testTag("menu_item_download_apk")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Home Machine (\$H)",
                                        color = NcTextPrimary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = NcSecondaryAmber
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onHomeMachine()
                                },
                                modifier = Modifier.testTag("menu_item_home_machine")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Unlock Alarm (\$X)",
                                        color = NcTextPrimary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = NcTertiaryGreen
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onUnlockAlarm()
                                },
                                modifier = Modifier.testTag("menu_item_unlock_alarm")
                            )
                        }
                    }
                }
            }

            // If machine in ALARM state, show persistent quick-unlock alert banner
            if (machineState == MachineState.ALARM) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(NcRedEmergency.copy(alpha = 0.2f))
                        .border(1.dp, NcRedEmergency, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alarm Warning",
                            tint = NcRedEmergency,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Machine Locked (Alarm State)",
                            style = MaterialTheme.typography.bodySmall,
                            color = NcTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = onUnlockAlarm,
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("unlock_alarm_banner_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcSecondaryAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Unlock (\$X)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
