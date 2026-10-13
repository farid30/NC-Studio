package com.example.model

import java.util.Locale

data class AxisCoordinates(
    val x: Float = 0.000f,
    val y: Float = 0.000f,
    val z: Float = 0.000f,
    val a: Float = 0.000f
) {
    fun formatX(): String = String.format(Locale.US, "%+08.3f", x)
    fun formatY(): String = String.format(Locale.US, "%+08.3f", y)
    fun formatZ(): String = String.format(Locale.US, "%+08.3f", z)
    fun formatA(): String = String.format(Locale.US, "%+08.3f", a)
}

enum class CoordinateDisplayMode(val label: String, val shortName: String) {
    WORKPIECE("Workpiece Coordinates (WCS)", "WCS"),
    MACHINE("Machine Coordinates (MCS)", "MCS")
}
