package com.example.parser

import com.example.model.GCodeProgram
import com.example.model.ToolpathBounds
import com.example.model.ToolpathSegment
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GCodeParser {

    fun parse(id: String, name: String, description: String, rawGcode: String): GCodeProgram {
        val lines = rawGcode.lines()
        val segments = mutableListOf<ToolpathSegment>()

        var currentX = 0f
        var currentY = 0f
        var currentZ = 0f

        var minX = 0f
        var maxX = 0f
        var minY = 0f
        var maxY = 0f
        var minZ = 0f
        var maxZ = 0f
        var hasCoords = false

        var isRapid = true
        var isAbsolute = true
        var currentFeed = 1200f
        var totalCutDistance = 0f
        var totalRapidDistance = 0f

        lines.forEachIndexed { index, rawLine ->
            // Remove comments ( ... ) and ;...
            var clean = rawLine.replace(Regex("\\(.*?\\)"), "").trim()
            val commentIdx = clean.indexOf(';')
            if (commentIdx != -1) {
                clean = clean.substring(0, commentIdx).trim()
            }
            if (clean.isBlank()) return@forEachIndexed

            val tokens = clean.uppercase().split(Regex("\\s+")).filter { it.isNotBlank() }
            var targetX = currentX
            var targetY = currentY
            var targetZ = currentZ
            var moved = false

            // Parse modal commands
            for (token in tokens) {
                when {
                    token == "G90" -> isAbsolute = true
                    token == "G91" -> isAbsolute = false
                    token == "G0" || token == "G00" -> isRapid = true
                    token == "G1" || token == "G01" -> isRapid = false
                    token.startsWith("F") -> {
                        token.substring(1).toFloatOrNull()?.let { currentFeed = it }
                    }
                    token.startsWith("X") -> {
                        token.substring(1).toFloatOrNull()?.let {
                            targetX = if (isAbsolute) it else currentX + it
                            moved = true
                        }
                    }
                    token.startsWith("Y") -> {
                        token.substring(1).toFloatOrNull()?.let {
                            targetY = if (isAbsolute) it else currentY + it
                            moved = true
                        }
                    }
                    token.startsWith("Z") -> {
                        token.substring(1).toFloatOrNull()?.let {
                            targetZ = if (isAbsolute) it else currentZ + it
                            moved = true
                        }
                    }
                }
            }

            if (moved) {
                val dx = targetX - currentX
                val dy = targetY - currentY
                val dz = targetZ - currentZ
                val distance = sqrt(dx * dx + dy * dy + dz * dz)

                if (distance > 0.001f) {
                    segments.add(
                        ToolpathSegment(
                            startX = currentX,
                            startY = currentY,
                            startZ = currentZ,
                            endX = targetX,
                            endY = targetY,
                            endZ = targetZ,
                            isRapid = isRapid,
                            feedRate = currentFeed,
                            lineIndex = index
                        )
                    )

                    if (isRapid) {
                        totalRapidDistance += distance
                    } else {
                        totalCutDistance += distance
                    }

                    if (!hasCoords) {
                        minX = minOf(currentX, targetX)
                        maxX = maxOf(currentX, targetX)
                        minY = minOf(currentY, targetY)
                        maxY = maxOf(currentY, targetY)
                        minZ = minOf(currentZ, targetZ)
                        maxZ = maxOf(currentZ, targetZ)
                        hasCoords = true
                    } else {
                        minX = minOf(minX, targetX)
                        maxX = maxOf(maxX, targetX)
                        minY = minOf(minY, targetY)
                        maxY = maxOf(maxY, targetY)
                        minZ = minOf(minZ, targetZ)
                        maxZ = maxOf(maxZ, targetZ)
                    }

                    currentX = targetX
                    currentY = targetY
                    currentZ = targetZ
                }
            }
        }

        val rapidFeed = 3000f // mm/min default
        val cutMinutes = if (currentFeed > 0f) totalCutDistance / currentFeed else 0f
        val rapidMinutes = totalRapidDistance / rapidFeed
        val estimatedSeconds = ((cutMinutes + rapidMinutes) * 60f).toLong().coerceAtLeast(1L)

        val bounds = ToolpathBounds(
            minX = minX,
            maxX = maxX,
            minY = minY,
            maxY = maxY,
            minZ = minZ,
            maxZ = maxZ,
            totalCutDistance = totalCutDistance,
            totalRapidDistance = totalRapidDistance
        )

        return GCodeProgram(
            id = id,
            name = name,
            description = description,
            rawGcode = rawGcode,
            lines = lines,
            segments = segments,
            bounds = bounds,
            estimatedSeconds = estimatedSeconds
        )
    }
}
