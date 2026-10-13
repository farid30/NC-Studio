package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AxisCoordinates
import com.example.model.CoordinateDisplayMode
import com.example.ui.theme.NcAxisA
import com.example.ui.theme.NcAxisX
import com.example.ui.theme.NcAxisY
import com.example.ui.theme.NcAxisZ
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurfaceContainer
import com.example.ui.theme.NcDarkSurfaceElevated
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary
import com.example.ui.theme.NcTextSecondary

@Composable
fun CoordinateReadoutCard(
    coordinates: AxisCoordinates,
    displayMode: CoordinateDisplayMode,
    aAxisEnabled: Boolean,
    onZeroAxis: (String) -> Unit,
    onShowSetAxisDialog: (String) -> Unit,
    onZeroAll: () -> Unit,
    onGotoOrigin: () -> Unit,
    onProbeZ: () -> Unit,
    onToggleAAxis: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dro_readout_card"),
        colors = CardDefaults.cardColors(
            containerColor = NcDarkSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NcBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Mode Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayMode.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NcPrimaryCyan
                )
                Text(
                    text = "UNIT: MM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = NcTextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X Axis Readout
            AxisRow(
                axisName = "X",
                axisColor = NcAxisX,
                valueString = coordinates.formatX(),
                onZero = { onZeroAxis("X") },
                onSet = { onShowSetAxisDialog("X") }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Y Axis Readout
            AxisRow(
                axisName = "Y",
                axisColor = NcAxisY,
                valueString = coordinates.formatY(),
                onZero = { onZeroAxis("Y") },
                onSet = { onShowSetAxisDialog("Y") }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Z Axis Readout
            AxisRow(
                axisName = "Z",
                axisColor = NcAxisZ,
                valueString = coordinates.formatZ(),
                onZero = { onZeroAxis("Z") },
                onSet = { onShowSetAxisDialog("Z") }
            )

            // Optional 4th A-Axis Readout
            if (aAxisEnabled) {
                Spacer(modifier = Modifier.height(6.dp))
                AxisRow(
                    axisName = "A",
                    axisColor = NcAxisA,
                    valueString = coordinates.formatA(),
                    onZero = { onZeroAxis("A") },
                    onSet = { onShowSetAxisDialog("A") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Global Origin & Zeroing Action Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onZeroAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("zero_all_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NcDarkSurfaceElevated,
                        contentColor = NcPrimaryCyan
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Zero All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onGotoOrigin,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("goto_origin_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NcDarkSurfaceElevated,
                        contentColor = NcSecondaryAmber
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Origin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onProbeZ,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("probe_z_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NcDarkSurfaceElevated,
                        contentColor = NcAxisZ
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Z-Probe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onToggleAAxis,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("toggle_a_axis_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (aAxisEnabled) NcAxisA else NcTextMuted
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (aAxisEnabled) "4-Axis" else "3-Axis",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AxisRow(
    axisName: String,
    axisColor: Color,
    valueString: String,
    onZero: () -> Unit,
    onSet: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NcDarkBackground)
            .border(1.dp, NcBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Axis badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(axisColor.copy(alpha = 0.2f))
                .border(1.dp, axisColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = axisName,
                color = axisColor,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Large glowing monospaced coordinate readout
        Text(
            text = valueString,
            color = axisColor,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Per-axis zero & set actions
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
                onClick = onZero,
                modifier = Modifier
                    .height(32.dp)
                    .testTag("zero_axis_${axisName.lowercase()}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NcDarkSurfaceElevated,
                    contentColor = axisColor
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "0.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = onSet,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NcDarkSurfaceElevated)
                    .testTag("set_axis_${axisName.lowercase()}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Set $axisName value",
                    tint = NcTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
