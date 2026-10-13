package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AxisCoordinates
import com.example.model.GCodeProgram
import com.example.ui.theme.NcAxisX
import com.example.ui.theme.NcAxisY
import com.example.ui.theme.NcAxisZ
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurfaceElevated
import com.example.ui.theme.NcGridLines
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcRedEmergency
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTertiaryGreen
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary
import com.example.ui.theme.NcTextSecondary
import com.example.ui.theme.NcToolpathFeed
import com.example.ui.theme.NcToolpathRapid
import java.util.Locale

@Composable
fun ToolpathCanvas(
    program: GCodeProgram?,
    currentLineIndex: Int,
    toolCoordinates: AxisCoordinates,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    showGrid: Boolean,
    showRapids: Boolean,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    onResetView: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleRapids: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NcDarkBackground)
            .border(1.dp, NcBorder, RoundedCornerShape(12.dp))
            .testTag("toolpath_visualizer_container")
    ) {
        // Interactive Canvas with Pan & Zoom gesture detection
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onZoomChange(zoomLevel * zoom)
                        onPanChange(pan.x, pan.y)
                    }
                }
                .testTag("toolpath_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f + panX
            val centerY = canvasH / 2f + panY

            // Calculate auto-fit scale
            val bounds = program?.bounds
            val maxSpan = maxOf(bounds?.width ?: 60f, bounds?.height ?: 60f, 40f)
            val baseScale = (minOf(canvasW, canvasH) * 0.7f / maxSpan) * zoomLevel

            // Coordinate transform helper: (cncX, cncY) -> Canvas Offset
            // In CNC, positive Y is UP. In Canvas, positive Y is DOWN.
            val offsetBoundsX = bounds?.minX ?: 0f
            val offsetBoundsY = bounds?.minY ?: 0f
            val boundsMidX = (bounds?.maxX ?: 40f + offsetBoundsX) / 2f
            val boundsMidY = (bounds?.maxY ?: 40f + offsetBoundsY) / 2f

            fun toCanvas(cncX: Float, cncY: Float): Offset {
                val screenX = centerX + (cncX - boundsMidX) * baseScale
                val screenY = centerY - (cncY - boundsMidY) * baseScale
                return Offset(screenX, screenY)
            }

            // 1. Draw Grid Lines
            if (showGrid) {
                drawCncGrid(
                    centerX = centerX,
                    centerY = centerY,
                    canvasW = canvasW,
                    canvasH = canvasH,
                    scale = baseScale
                )
            }

            // 2. Draw CNC Origin (0,0) Reticle and Axis indicators
            val originScreen = toCanvas(0f, 0f)
            // X-axis red arrow
            drawLine(
                color = NcAxisX,
                start = originScreen,
                end = Offset(originScreen.x + 35f, originScreen.y),
                strokeWidth = 3f
            )
            // Y-axis green arrow
            drawLine(
                color = NcAxisY,
                start = originScreen,
                end = Offset(originScreen.x, originScreen.y - 35f),
                strokeWidth = 3f
            )
            drawCircle(
                color = NcSecondaryAmber,
                radius = 4f,
                center = originScreen
            )

            // 3. Draw G-code Toolpath Segments
            if (program != null) {
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                program.segments.forEach { segment ->
                    val isPastOrActive = segment.lineIndex <= currentLineIndex
                    val isCurrent = segment.lineIndex == currentLineIndex

                    val start = toCanvas(segment.startX, segment.startY)
                    val end = toCanvas(segment.endX, segment.endY)

                    when {
                        segment.isRapid -> {
                            if (showRapids) {
                                drawLine(
                                    color = if (isCurrent) Color.White else NcToolpathRapid.copy(alpha = 0.6f),
                                    start = start,
                                    end = end,
                                    strokeWidth = if (isCurrent) 3f else 1.5f,
                                    pathEffect = dashedEffect
                                )
                            }
                        }
                        else -> {
                            val lineColor = when {
                                isCurrent -> Color.White
                                isPastOrActive -> NcTertiaryGreen
                                else -> NcToolpathFeed
                            }
                            val width = if (isCurrent) 4.5f else 2.5f

                            drawLine(
                                color = lineColor,
                                start = start,
                                end = end,
                                strokeWidth = width
                            )
                        }
                    }
                }
            }

            // 4. Draw Live Tool Position Reticle (Crosshair & Glowing Circle)
            val toolScreen = toCanvas(toolCoordinates.x, toolCoordinates.y)
            drawCircle(
                color = NcPrimaryCyan.copy(alpha = 0.25f),
                radius = 16f,
                center = toolScreen
            )
            drawCircle(
                color = NcPrimaryCyan,
                radius = 7f,
                center = toolScreen
            )
            // Crosshairs
            drawLine(
                color = NcPrimaryCyan,
                start = Offset(toolScreen.x - 18f, toolScreen.y),
                end = Offset(toolScreen.x + 18f, toolScreen.y),
                strokeWidth = 2f
            )
            drawLine(
                color = NcPrimaryCyan,
                start = Offset(toolScreen.x, toolScreen.y - 18f),
                end = Offset(toolScreen.x, toolScreen.y + 18f),
                strokeWidth = 2f
            )
        }

        // Top Metadata Overlay (Program name & Bounding Dimensions)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Surface(
                color = NcDarkSurfaceElevated.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NcBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        text = program?.name ?: "No Job Loaded",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = NcPrimaryCyan
                    )
                    if (program != null) {
                        val b = program.bounds
                        Text(
                            text = String.format(
                                Locale.US,
                                "W: %.1fmm | H: %.1fmm | Cut: %.1fm",
                                b.width, b.height, (b.totalCutDistance / 1000f)
                            ),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NcTextSecondary
                        )
                    }
                }
            }
        }

        // Bottom Tool Position Coordinate Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Surface(
                color = NcDarkSurfaceElevated.copy(alpha = 0.85f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NcBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TOOL:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NcPrimaryCyan
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "X:%.2f  Y:%.2f  Z:%.2f",
                            toolCoordinates.x, toolCoordinates.y, toolCoordinates.z
                        ),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NcTextPrimary
                    )
                }
            }
        }

        // Right Overlay Controls (Zoom In, Zoom Out, Reset, Toggle Grid, Toggle Rapids)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VisualizerIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Zoom In",
                onClick = { onZoomChange(zoomLevel * 1.3f) },
                testTag = "viz_zoom_in"
            )

            VisualizerIconButton(
                icon = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                onClick = { onZoomChange(zoomLevel * 0.77f) },
                testTag = "viz_zoom_out"
            )

            VisualizerIconButton(
                icon = Icons.Default.CropFree,
                contentDescription = "Fit & Center View",
                onClick = onResetView,
                testTag = "viz_reset_view"
            )

            VisualizerIconButton(
                icon = Icons.Default.GridOn,
                contentDescription = "Toggle Grid",
                onClick = onToggleGrid,
                tint = if (showGrid) NcPrimaryCyan else NcTextMuted,
                testTag = "viz_toggle_grid"
            )

            VisualizerIconButton(
                icon = Icons.Default.Route,
                contentDescription = "Toggle Rapids",
                onClick = onToggleRapids,
                tint = if (showRapids) NcSecondaryAmber else NcTextMuted,
                testTag = "viz_toggle_rapids"
            )
        }
    }
}

@Composable
private fun VisualizerIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = NcTextPrimary,
    testTag: String
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .border(1.dp, NcBorder, CircleShape)
            .testTag(testTag),
        color = NcDarkSurfaceElevated.copy(alpha = 0.9f),
        contentColor = tint,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
        }
    }
}

private fun DrawScope.drawCncGrid(
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float,
    scale: Float
) {
    val gridStepMm = 10f
    val stepPx = gridStepMm * scale

    if (stepPx < 8f) return // Avoid rendering sub-pixel grid overload

    // Vertical grid lines
    var x = centerX % stepPx
    while (x < canvasW) {
        drawLine(
            color = NcGridLines,
            start = Offset(x, 0f),
            end = Offset(x, canvasH),
            strokeWidth = 1f
        )
        x += stepPx
    }

    // Horizontal grid lines
    var y = centerY % stepPx
    while (y < canvasH) {
        drawLine(
            color = NcGridLines,
            start = Offset(0f, y),
            end = Offset(canvasW, y),
            strokeWidth = 1f
        )
        y += stepPx
    }
}
