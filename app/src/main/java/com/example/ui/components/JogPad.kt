package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NcAxisA
import com.example.ui.theme.NcAxisX
import com.example.ui.theme.NcAxisY
import com.example.ui.theme.NcAxisZ
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurfaceElevated
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcRedEmergency
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary
import com.example.ui.theme.NcTextSecondary

@Composable
fun JogPad(
    isStepMode: Boolean,
    currentStepMm: Float,
    jogFeedRate: Float,
    aAxisEnabled: Boolean,
    onSetJogMode: (Boolean) -> Unit,
    onSetStepDistance: (Float) -> Unit,
    onSetJogFeed: (Float) -> Unit,
    onJogAxis: (axis: String, direction: Int) -> Unit,
    onCancelJog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stepPresets = listOf(0.01f, 0.1f, 0.5f, 1.0f, 5.0f, 10.0f, 50.0f, 100.0f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jog_pad_card"),
        colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Step Mode vs Continuous Mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MANUAL JOG CONTROL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NcPrimaryCyan
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NcDarkBackground)
                        .padding(2.dp)
                ) {
                    val modeButtonModifier = Modifier.height(28.dp)
                    Button(
                        onClick = { onSetJogMode(true) },
                        modifier = modeButtonModifier.testTag("jog_mode_step_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStepMode) NcPrimaryCyan else Color.Transparent,
                            contentColor = if (isStepMode) Color.Black else NcTextSecondary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("STEP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSetJogMode(false) },
                        modifier = modeButtonModifier.testTag("jog_mode_continuous_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isStepMode) NcSecondaryAmber else Color.Transparent,
                            contentColor = if (!isStepMode) Color.Black else NcTextSecondary
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("CONT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Step Distance Selector Chips (shown when step mode is active)
            if (isStepMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    stepPresets.forEach { step ->
                        val isSelected = currentStepMm == step
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetStepDistance(step) },
                            label = {
                                Text(
                                    text = "${step}mm",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NcPrimaryCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = NcDarkBackground,
                                labelColor = NcTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = NcBorder,
                                selectedBorderColor = NcPrimaryCyan
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("step_chip_${step}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Jogging Grid Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // XY Directional Keypad (3x3 Matrix)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    // Row 1: Diagonal Left, Y+, Diagonal Right
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        JogButton(
                            label = "X-Y+",
                            color = NcTextMuted,
                            onClick = {
                                onJogAxis("X", -1)
                                onJogAxis("Y", 1)
                            },
                            modifier = Modifier.size(54.dp).testTag("jog_diag_nw")
                        ) {
                            Icon(Icons.Default.NorthWest, contentDescription = "X- Y+", modifier = Modifier.size(20.dp))
                        }

                        JogButton(
                            label = "Y+",
                            color = NcAxisY,
                            onClick = { onJogAxis("Y", 1) },
                            modifier = Modifier.size(54.dp).testTag("jog_y_plus")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Y+", modifier = Modifier.size(18.dp))
                                Text("Y+", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        JogButton(
                            label = "X+Y+",
                            color = NcTextMuted,
                            onClick = {
                                onJogAxis("X", 1)
                                onJogAxis("Y", 1)
                            },
                            modifier = Modifier.size(54.dp).testTag("jog_diag_ne")
                        ) {
                            Icon(Icons.Default.NorthEast, contentDescription = "X+ Y+", modifier = Modifier.size(20.dp))
                        }
                    }

                    // Row 2: X-, STOP/Cancel, X+
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        JogButton(
                            label = "X-",
                            color = NcAxisX,
                            onClick = { onJogAxis("X", -1) },
                            modifier = Modifier.size(54.dp).testTag("jog_x_minus")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "X-", modifier = Modifier.size(16.dp))
                                Text("X-", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        JogButton(
                            label = "STOP",
                            color = NcRedEmergency,
                            onClick = onCancelJog,
                            modifier = Modifier.size(54.dp).testTag("jog_stop_center")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Stop Jog", modifier = Modifier.size(22.dp))
                        }

                        JogButton(
                            label = "X+",
                            color = NcAxisX,
                            onClick = { onJogAxis("X", 1) },
                            modifier = Modifier.size(54.dp).testTag("jog_x_plus")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("X+", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowForward, contentDescription = "X+", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Row 3: Diagonal Left, Y-, Diagonal Right
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        JogButton(
                            label = "X-Y-",
                            color = NcTextMuted,
                            onClick = {
                                onJogAxis("X", -1)
                                onJogAxis("Y", -1)
                            },
                            modifier = Modifier.size(54.dp).testTag("jog_diag_sw")
                        ) {
                            Icon(Icons.Default.SouthWest, contentDescription = "X- Y-", modifier = Modifier.size(20.dp))
                        }

                        JogButton(
                            label = "Y-",
                            color = NcAxisY,
                            onClick = { onJogAxis("Y", -1) },
                            modifier = Modifier.size(54.dp).testTag("jog_y_minus")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Y-", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Y-", modifier = Modifier.size(18.dp))
                            }
                        }

                        JogButton(
                            label = "X+Y-",
                            color = NcTextMuted,
                            onClick = {
                                onJogAxis("X", 1)
                                onJogAxis("Y", -1)
                            },
                            modifier = Modifier.size(54.dp).testTag("jog_diag_se")
                        ) {
                            Icon(Icons.Default.SouthEast, contentDescription = "X+ Y-", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Z-Axis Vertical Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(0.7f)
                ) {
                    Text(
                        text = "Z AXIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = NcAxisZ
                    )

                    JogButton(
                        label = "Z+",
                        color = NcAxisZ,
                        onClick = { onJogAxis("Z", 1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("jog_z_plus")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Z+ Tool Up", modifier = Modifier.size(22.dp))
                            Text("Z+ UP", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    JogButton(
                        label = "Z-",
                        color = NcAxisZ,
                        onClick = { onJogAxis("Z", -1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("jog_z_minus")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Z- DOWN", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Z- Tool Down", modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // Optional 4th A-Axis row
            if (aAxisEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "4th AXIS (A):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NcAxisA
                    )
                    Button(
                        onClick = { onJogAxis("A", -1) },
                        modifier = Modifier.weight(1f).height(36.dp).testTag("jog_a_minus"),
                        colors = ButtonDefaults.buttonColors(containerColor = NcDarkSurfaceElevated, contentColor = NcAxisA),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("A- CCW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onJogAxis("A", 1) },
                        modifier = Modifier.weight(1f).height(36.dp).testTag("jog_a_plus"),
                        colors = ButtonDefaults.buttonColors(containerColor = NcDarkSurfaceElevated, contentColor = NcAxisA),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("A+ CW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Jog Feed Rate Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JOG SPEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = NcTextSecondary
                    )
                    Text(
                        text = "${jogFeedRate.toInt()} mm/min",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = NcPrimaryCyan
                    )
                }

                Slider(
                    value = jogFeedRate,
                    onValueChange = onSetJogFeed,
                    valueRange = 100f..4000f,
                    colors = SliderDefaults.colors(
                        thumbColor = NcPrimaryCyan,
                        activeTrackColor = NcPrimaryCyan,
                        inactiveTrackColor = NcDarkBackground
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("jog_feed_slider")
                )
            }
        }
    }
}

@Composable
private fun JogButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        color = NcDarkBackground,
        contentColor = color,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            content()
        }
    }
}
