package com.griffith.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable


// Rewritten ai generated code and removed hard coded colors

private val DarkColors = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    primary = Accent,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TasteTheme(
    dark: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}