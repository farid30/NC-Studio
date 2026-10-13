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
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun SpindleFeedControl(
    spindleOn: Boolean,
    spindleCcw: Boolean,
    targetRpm: Int,
    actualRpm: Float,
    feedOverridePercent: Int,
    actualFeedRate: Float,
    onToggleSpindle: () -> Unit,
    onSetSpindleDirection: (Boolean) -> Unit,
    onSetSpindleRpm: (Int) -> Unit,
    onSetFeedOverride: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spindle_feed_card"),
        colors = CardDefaults.cardColors(containerColor = NcDarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // SPINDLE SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (spindleOn) NcTertiaryGreen else NcTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPINDLE CONTROL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = NcPrimaryCyan
                    )
                }

                // Actual RPM Display
                Text(
                    text = "ACTUAL: ${actualRpm.toInt()} RPM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (spindleOn) NcTertiaryGreen else NcTextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spindle Power and Direction row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Spindle ON/OFF Button
                Button(
                    onClick = onToggleSpindle,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("spindle_power_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (spindleOn) NcTertiaryGreen else NcDarkSurfaceElevated,
                        contentColor = if (spindleOn) Color.Black else NcTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (spindleOn) "SPINDLE ON" else "SPINDLE OFF",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                // Spindle CW / CCW Direction Toggle
                OutlinedButton(
                    onClick = { onSetSpindleDirection(!spindleCcw) },
                    modifier = Modifier
                        .weight(0.8f)
                        .height(44.dp)
                        .testTag("spindle_direction_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NcSecondaryAmber
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (spindleCcw) Icons.Default.RotateLeft else Icons.Default.RotateRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (spindleCcw) "CCW (M4)" else "CW (M3)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Spindle RPM Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val rpmPresets = listOf(6000, 12000, 18000, 24000)
                rpmPresets.forEach { preset ->
                    val isSelected = targetRpm == preset
                    Button(
                        onClick = { onSetSpindleRpm(preset) },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("rpm_preset_$preset"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) NcPrimaryCyan else NcDarkBackground,
                            contentColor = if (isSelected) Color.Black else NcTextSecondary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "${preset / 1000}K",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Spindle Slider
            Slider(
                value = targetRpm.toFloat(),
                onValueChange = { onSetSpindleRpm(it.toInt()) },
                valueRange = 1000f..24000f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = NcTertiaryGreen,
                    activeTrackColor = NcTertiaryGreen,
                    inactiveTrackColor = NcDarkBackground
                ),
                modifier = Modifier.fillMaxWidth().testTag("spindle_rpm_slider")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // FEED RATE OVERRIDE SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FEED OVERRIDE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NcSecondaryAmber
                )
                Text(
                    text = "$feedOverridePercent% (Actual: ${actualFeedRate.toInt()} mm/min)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = NcTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onSetFeedOverride((feedOverridePercent - 10).coerceAtLeast(10)) },
                    modifier = Modifier.height(32.dp).testTag("feed_minus_10"),
                    colors = ButtonDefaults.buttonColors(containerColor = NcDarkBackground, contentColor = NcTextPrimary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("-10%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSetFeedOverride(100) },
                    modifier = Modifier.weight(1f).height(32.dp).testTag("feed_reset_100"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (feedOverridePercent == 100) NcDarkSurfaceElevated else NcSecondaryAmber,
                        contentColor = if (feedOverridePercent == 100) NcPrimaryCyan else Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("100% Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSetFeedOverride((feedOverridePercent + 10).coerceAtMost(200)) },
                    modifier = Modifier.height(32.dp).testTag("feed_plus_10"),
                    colors = ButtonDefaults.buttonColors(containerColor = NcDarkBackground, contentColor = NcTextPrimary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("+10%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
