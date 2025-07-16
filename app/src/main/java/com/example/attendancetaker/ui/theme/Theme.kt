package com.example.attendancetaker.ui.theme

import android.app.Activity
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = Blue80,
    onPrimary = Color(0xFF003258),
    primaryContainer = Blue40,
    onPrimaryContainer = Blue80,

    // Secondary colors
    secondary = BlueGrey80,
    onSecondary = Color(0xFF263238),
    secondaryContainer = BlueGrey40,
    onSecondaryContainer = BlueGrey80,

    // Tertiary colors
    tertiary = Indigo80,
    onTertiary = Color(0xFF1A1B3A),
    tertiaryContainer = Indigo40,
    onTertiaryContainer = Indigo80,

    // Error colors
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Background and surface
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),

    // Outline and other colors
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF475569),
    scrim = Color.Black,
    inverseSurface = Color(0xFFF1F5F9),
    inverseOnSurface = Color(0xFF1E293B),
    inversePrimary = Blue40
)

private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),

    // Secondary colors
    secondary = BlueGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECEFF1),
    onSecondaryContainer = Color(0xFF263238),

    // Tertiary colors
    tertiary = Indigo40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8EAF6),
    onTertiaryContainer = Color(0xFF1A237E),

    // Error colors
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF410E0B),

    // Background and surface
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A202C),
    surface = LightSurface,
    onSurface = Color(0xFF1A202C),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A5568),

    // Outline and other colors
    outline = Color(0xFF718096),
    outlineVariant = Color(0xFFCBD5E0),
    scrim = Color.Black,
    inverseSurface = Color(0xFF2D3748),
    inverseOnSurface = Color(0xFFF7FAFC),
    inversePrimary = Blue80
)

@Composable
fun AttendanceTakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom blue theme
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
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}