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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogMessageType
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurfaceElevated
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary
import com.example.ui.theme.NcTextSecondary
import com.example.viewmodel.CncUiState
import com.example.viewmodel.CncViewModel

@Composable
fun MdiConsoleScreen(
    uiState: CncUiState,
    viewModel: CncViewModel,
    modifier: Modifier = Modifier
) {
    val logListState = rememberLazyListState()
    var selectedFilter by remember { mutableStateOf<LogMessageType?>(null) }

    // Auto-scroll to bottom on new log
    LaunchedEffect(uiState.logs.size) {
        if (uiState.logs.isNotEmpty()) {
            logListState.animateScrollToItem(uiState.logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("mdi_console_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Macro Action Buttons (Scrollable chips)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "QUICK MACROS & COMMANDS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = NcPrimaryCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.quickMacros.forEach { macro ->
                    Button(
                        onClick = { viewModel.runMacro(macro) },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("macro_button_${macro.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NcDarkSurfaceVariant,
                            contentColor = NcPrimaryCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            text = macro.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Terminal Log Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("terminal_log_card"),
            colors = CardDefaults.cardColors(containerColor = NcDarkBackground),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Terminal Header Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NcDarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESP32 SERIAL TERMINAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = NcTextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.clearLogs() },
                            modifier = Modifier.size(28.dp).testTag("clear_logs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear logs",
                                tint = NcTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Log Entries
                val filteredLogs = remember(uiState.logs, selectedFilter) {
                    if (selectedFilter == null) uiState.logs
                    else uiState.logs.filter { it.type == selectedFilter }
                }

                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Serial terminal empty. Send G-code below.",
                            color = NcTextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = logListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = log.timestamp,
                                    color = NcTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(72.dp)
                                )
                                Text(
                                    text = "[${log.type.tag}] ",
                                    color = log.type.color,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(52.dp)
                                )
                                Text(
                                    text = log.message,
                                    color = log.type.color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // MDI Command Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.currentCommandInput,
                onValueChange = { viewModel.onCommandInputChange(it) },
                placeholder = {
                    Text("Enter G-Code (e.g. G0 X10 Y10, ?)", color = NcTextMuted, fontSize = 12.sp)
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("mdi_command_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.sendTerminalCommand() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NcPrimaryCyan,
                    unfocusedBorderColor = NcBorder,
                    focusedTextColor = NcTextPrimary,
                    unfocusedTextColor = NcTextPrimary,
                    cursorColor = NcPrimaryCyan,
                    focusedContainerColor = NcDarkSurfaceVariant,
                    unfocusedContainerColor = NcDarkSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = { viewModel.sendTerminalCommand() },
                modifier = Modifier
                    .height(52.dp)
                    .testTag("send_mdi_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NcPrimaryCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SEND", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
