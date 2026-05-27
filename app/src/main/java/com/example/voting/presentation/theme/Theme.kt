package com.example.voting.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Определяем цвета здесь
private val GreenPrimary = Color(0xFF4CAF50)      // основной зелёный
private val GreenOnPrimary = Color(0xFFFFFFFF)   // белый текст на кнопках
private val GreenSecondary = Color(0xFF66BB6A)   // дополнительный зелёный
private val GreenBackground = Color(0xFFF5FCF5)  // фон
private val GreenSurface = Color(0xFFFFFFFF)     // поверхность карточек
private val GreenError = Color(0xFFE57373)       // ошибки

private val DarkGreenPrimary = Color(0xFF81C784)
private val DarkGreenOnPrimary = Color(0xFF1B5E20)
private val DarkGreenBackground = Color(0xFF1A2E1A)
private val DarkGreenSurface = Color(0xFF2C4A2C)
private val DarkGreenError = Color(0xFFFF8A8A)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = GreenPrimary,
    secondary = GreenSecondary,
    onSecondary = GreenOnPrimary,
    secondaryContainer = GreenSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = GreenSecondary,
    tertiary = GreenSecondary,
    onTertiary = GreenOnPrimary,
    background = GreenBackground,
    onBackground = GreenPrimary,
    surface = GreenSurface,
    onSurface = Color(0xFF2E4A2E),
    surfaceVariant = GreenBackground,
    onSurfaceVariant = GreenPrimary,
    error = GreenError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = DarkGreenOnPrimary,
    primaryContainer = DarkGreenPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = DarkGreenPrimary,
    secondary = DarkGreenPrimary,
    onSecondary = DarkGreenOnPrimary,
    secondaryContainer = DarkGreenPrimary.copy(alpha = 0.2f),
    onSecondaryContainer = DarkGreenPrimary,
    tertiary = DarkGreenPrimary,
    onTertiary = DarkGreenOnPrimary,
    background = DarkGreenBackground,
    onBackground = DarkGreenPrimary,
    surface = DarkGreenSurface,
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = DarkGreenBackground,
    onSurfaceVariant = DarkGreenPrimary,
    error = DarkGreenError,
    onError = Color.Black
)

@Composable
fun VotingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}