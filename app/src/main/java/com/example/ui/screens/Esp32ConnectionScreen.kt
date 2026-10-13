package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import com.example.ui.components.ApkDownloadDialog
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.ConnectionType
import com.example.model.Esp32ConnectionConfig
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

@Composable
fun Esp32ConnectionScreen(
    uiState: CncUiState,
    viewModel: CncViewModel,
    modifier: Modifier = Modifier
) {
    var hostIp by remember(uiState.connectionConfig.hostIp) { mutableStateOf(uiState.connectionConfig.hostIp) }
    var portText by remember(uiState.connectionConfig.port) { mutableStateOf(uiState.connectionConfig.port.toString()) }
    var selectedType by remember(uiState.connectionConfig.type) { mutableStateOf(uiState.connectionConfig.type) }
    var showApkDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .testTag("esp32_connection_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Connection Status Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("connection_status_card"),
            colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESP32 CONTROLLER LINK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = NcPrimaryCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedType.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NcTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    val isConnected = uiState.connectionStatus == ConnectionStatus.CONNECTED
                    val badgeColor = when (uiState.connectionStatus) {
                        ConnectionStatus.CONNECTED -> NcTertiaryGreen
                        ConnectionStatus.CONNECTING -> NcSecondaryAmber
                        ConnectionStatus.DISCONNECTED -> NcTextMuted
                        ConnectionStatus.ERROR -> NcRedEmergency
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = uiState.connectionStatus.name,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Connect / Disconnect Action Button
                if (uiState.connectionStatus == ConnectionStatus.CONNECTED) {
                    Button(
                        onClick = { viewModel.disconnectEsp32() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("disconnect_esp32_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcRedEmergency,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DISCONNECT FROM ESP32", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            val portInt = portText.toIntOrNull() ?: selectedType.defaultPort
                            val newConfig = Esp32ConnectionConfig(
                                type = selectedType,
                                hostIp = hostIp,
                                port = portInt
                            )
                            viewModel.updateConnectionConfig(newConfig)
                            viewModel.connectToEsp32()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("connect_esp32_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcPrimaryCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CONNECT TO ESP32", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Connection Protocol Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("protocol_selector_card"),
            colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "SELECT COMMUNICATION INTERFACE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NcSecondaryAmber
                )
                Spacer(modifier = Modifier.height(8.dp))

                ConnectionType.values().forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        onClick = {
                            selectedType = type
                            if (type.defaultPort > 0) {
                                portText = type.defaultPort.toString()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSelected) NcPrimaryCyan else NcBorder, RoundedCornerShape(8.dp)),
                        color = if (isSelected) NcDarkSurfaceElevated else NcDarkBackground
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (type) {
                                ConnectionType.SIMULATOR -> Icons.Default.Computer
                                ConnectionType.WIFI_TELNET -> Icons.Default.Router
                                ConnectionType.WEBSOCKET -> Icons.Default.Wifi
                                ConnectionType.HTTP_REST -> Icons.Default.Link
                                ConnectionType.BLUETOOTH -> Icons.Default.Bluetooth
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) NcPrimaryCyan else NcTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) NcPrimaryCyan else NcTextPrimary
                                )
                                Text(
                                    text = type.description,
                                    fontSize = 11.sp,
                                    color = NcTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Network Configuration (IP & Port)
        if (selectedType != ConnectionType.SIMULATOR) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("network_config_card"),
                colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "NETWORK SETTINGS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NcSecondaryAmber
                    )

                    // Quick Preset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                hostIp = "192.168.4.1"
                                portText = "23"
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("ESP32 AP", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                hostIp = "192.168.1.100"
                                portText = "23"
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("LAN Telnet", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                hostIp = "192.168.4.1"
                                portText = "80"
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("FluidNC WS", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = hostIp,
                        onValueChange = { hostIp = it },
                        label = { Text("ESP32 IP Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("host_ip_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NcPrimaryCyan,
                            unfocusedBorderColor = NcBorder,
                            focusedTextColor = NcTextPrimary,
                            unfocusedTextColor = NcTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it },
                        label = { Text("Port Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("port_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NcPrimaryCyan,
                            unfocusedBorderColor = NcBorder,
                            focusedTextColor = NcTextPrimary,
                            unfocusedTextColor = NcTextPrimary
                        )
                    )
                }
            }
        }

        // GRBL Settings Inspector & Machine Hardware Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("grbl_info_card"),
            colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "GRBL / FLUIDNC DIAGNOSTICS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NcPrimaryCyan
                )

                Text(
                    text = "Compatible with GRBL 1.1h, GRBL-ESP32, FluidNC, and ESP32 CNC shields. Supports standard motion interpolation, homing sequences (\$H), alarm resets (\$X), and real-time status polling (?).",
                    fontSize = 12.sp,
                    color = NcTextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.sendTerminalCommand("$$") },
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NcDarkBackground, contentColor = NcPrimaryCyan),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Query \$\$ Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.sendTerminalCommand("\$I") },
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NcDarkBackground, contentColor = NcSecondaryAmber),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Query \$I Build Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Standalone APK Package Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("apk_export_card"),
            colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STANDALONE APK PACKAGE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NcTertiaryGreen
                        )
                        Text(
                            text = "Save or export APK directly to your phone/tablet",
                            fontSize = 11.sp,
                            color = NcTextSecondary
                        )
                    }
                }

                Button(
                    onClick = { showApkDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_apk_dialog_from_setup"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NcTertiaryGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DOWNLOAD / EXPORT APK FILE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    if (showApkDialog) {
        ApkDownloadDialog(
            onDismiss = { showApkDialog = false }
        )
    }
}
