package com.example.model

data class ToolpathSegment(
    val startX: Float,
    val startY: Float,
    val startZ: Float,
    val endX: Float,
    val endY: Float,
    val endZ: Float,
    val isRapid: Boolean, // true = G0 (orange/dashed), false = G1/G2/G3 (cyan/solid)
    val feedRate: Float = 0f,
    val lineIndex: Int = 0
)

data class ToolpathBounds(
    val minX: Float = 0f,
    val maxX: Float = 0f,
    val minY: Float = 0f,
    val maxY: Float = 0f,
    val minZ: Float = 0f,
    val maxZ: Float = 0f,
    val totalCutDistance: Float = 0f,
    val totalRapidDistance: Float = 0f
) {
    val width: Float get() = (maxX - minX).coerceAtLeast(1f)
    val height: Float get() = (maxY - minY).coerceAtLeast(1f)
    val depth: Float get() = (maxZ - minZ).coerceAtLeast(0.1f)
}
