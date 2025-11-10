package com.griffith.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Close to your screenshots (cream + terracotta, dark charcoal)
val Cream = Color(0xFFFAF7F2)
val Charcoal = Color(0xFF2A2A2A)
val Terracotta = Color(0xFFE36A3E)
val CardDark = Color(0xFF1E1E1E)
val CardLight = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    background = Cream,
    surface = CardLight,
    primary = Terracotta,
    onPrimary = Color.White,
    onBackground = Charcoal,
    onSurface = Charcoal
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF0F1112),
    surface = CardDark,
    primary = Terracotta,
    onPrimary = Color.White,
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED)
)

@Composable
fun TasteTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}