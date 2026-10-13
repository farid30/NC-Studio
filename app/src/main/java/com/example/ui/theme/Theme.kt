package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NcStudioColorScheme = darkColorScheme(
    primary = NcPrimaryCyan,
    onPrimary = Color(0xFF002229),
    primaryContainer = NcDarkSurfaceElevated,
    onPrimaryContainer = NcPrimaryCyan,
    secondary = NcSecondaryAmber,
    onSecondary = Color(0xFF261800),
    secondaryContainer = Color(0xFF3E2800),
    onSecondaryContainer = NcSecondaryAmber,
    tertiary = NcTertiaryGreen,
    onTertiary = Color(0xFF002910),
    background = NcDarkBackground,
    onBackground = NcTextPrimary,
    surface = NcDarkSurface,
    onSurface = NcTextPrimary,
    surfaceVariant = NcDarkSurfaceVariant,
    onSurfaceVariant = NcTextSecondary,
    surfaceContainer = NcDarkSurfaceContainer,
    surfaceContainerHigh = NcDarkSurfaceElevated,
    outline = NcBorder,
    outlineVariant = NcGridLines,
    error = NcRedEmergency,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // NC Studio enforces the industrial high-contrast dark cockpit theme for maximum readability in workshop/CNC environments
    MaterialTheme(
        colorScheme = NcStudioColorScheme,
        typography = Typography,
        content = content
    )
}
