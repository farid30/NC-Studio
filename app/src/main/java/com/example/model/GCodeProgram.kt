package com.example.model

data class GCodeProgram(
    val id: String,
    val name: String,
    val description: String,
    val rawGcode: String,
    val lines: List<String> = emptyList(),
    val segments: List<ToolpathSegment> = emptyList(),
    val bounds: ToolpathBounds = ToolpathBounds(),
    val estimatedSeconds: Long = 0L
)
