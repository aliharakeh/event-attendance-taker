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
    primary = Yellow80,
    onPrimary = DarkYellow,
    primaryContainer = YellowGrey40,
    onPrimaryContainer = Yellow80,
    secondary = YellowGrey80,
    onSecondary = DarkYellow,
    secondaryContainer = YellowGrey40,
    onSecondaryContainer = YellowGrey80,
    tertiary = Amber80,
    onTertiary = Color(0xFF332600),
    tertiaryContainer = Amber40,
    onTertiaryContainer = Amber80,
    error = YellowError,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B16),
    onBackground = Color(0xFFE6E1D6),
    surface = Color(0xFF1C1B16),
    onSurface = Color(0xFFE6E1D6),
    surfaceVariant = Color(0xFF4A4739),
    onSurfaceVariant = Color(0xFFCBC5B4),
    outline = Color(0xFF948F7E),
    outlineVariant = Color(0xFF4A4739),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE6E1D6),
    inverseOnSurface = Color(0xFF313026),
    inversePrimary = Yellow40
)

private val LightColorScheme = lightColorScheme(
    primary = Yellow40,
    onPrimary = Color.White,
    primaryContainer = LightYellow,
    onPrimaryContainer = DarkYellow,
    secondary = YellowGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E5D4),
    onSecondaryContainer = Color(0xFF1E1E11),
    tertiary = Amber40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC0),
    onTertiaryContainer = Color(0xFF2D1600),
    error = YellowError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF1C1B16),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF1C1B16),
    surfaceVariant = Color(0xFFE8E2D0),
    onSurfaceVariant = Color(0xFF4A4739),
    outline = Color(0xFF7B7767),
    outlineVariant = Color(0xFFCBC5B4),
    scrim = Color.Black,
    inverseSurface = Color(0xFF313026),
    inverseOnSurface = Color(0xFFF4F0E4),
    inversePrimary = Yellow80
)

@Composable
fun AttendanceTakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom yellow theme
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}