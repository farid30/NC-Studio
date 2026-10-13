package com.example.model

data class MacroItem(
    val id: String,
    val title: String,
    val gcode: String,
    val description: String,
    val iconKey: String = "bolt"
)
