package com.airesume.builder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Color tokens ─────────────────────────────────────────────────────────────

val Navy900   = Color(0xFF0D1B2A)
val Navy800   = Color(0xFF1A2F4A)
val Navy700   = Color(0xFF1E3A5F)
val Indigo600 = Color(0xFF3949AB)
val Indigo500 = Color(0xFF3F51B5)
val Indigo400 = Color(0xFF5C6BC0)
val Teal400   = Color(0xFF26C6DA)
val Teal300   = Color(0xFF4DD0E1)
val Amber400  = Color(0xFFFFCA28)
val GreenAI   = Color(0xFF00C853)
val Surface0  = Color(0xFFF8F9FE)
val Surface1  = Color(0xFFEEF0F8)

private val LightColorScheme = lightColorScheme(
    primary          = Indigo500,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFE8EAF6),
    onPrimaryContainer = Navy900,
    secondary        = Teal400,
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    tertiary         = Amber400,
    onTertiary       = Color.Black,
    background       = Surface0,
    onBackground     = Navy900,
    surface          = Color.White,
    onSurface        = Navy900,
    surfaceVariant   = Surface1,
    onSurfaceVariant = Color(0xFF44464F),
    outline          = Color(0xFF74777F),
    error            = Color(0xFFBA1A1A),
    onError          = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Indigo400,
    onPrimary        = Color.White,
    primaryContainer = Navy700,
    onPrimaryContainer = Color(0xFFBBC3FF),
    secondary        = Teal300,
    onSecondary      = Color.Black,
    secondaryContainer = Color(0xFF004D52),
    onSecondaryContainer = Color(0xFF84FFFF),
    background       = Color(0xFF1A1C23),
    onBackground     = Color(0xFFE3E2E6),
    surface          = Color(0xFF1A1C23),
    onSurface        = Color(0xFFE3E2E6),
    surfaceVariant   = Color(0xFF282A36),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline          = Color(0xFF8E9099),
    error            = Color(0xFFFFB4AB),
    onError          = Color(0xFF690005),
)

@Composable
fun AIResumeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
