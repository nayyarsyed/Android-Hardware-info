package com.example.hardwareinfo.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RetroGreen,
    onPrimary = BackgroundBlack,
    primaryContainer = RetroBlue,
    onPrimaryContainer = OnBackgroundWhite,
    secondary = RetroYellow,
    onSecondary = BackgroundBlack,
    secondaryContainer = RetroPurple,
    onSecondaryContainer = OnBackgroundWhite,
    tertiary = RetroCyan,
    onTertiary = BackgroundBlack,
    tertiaryContainer = RetroOrange,
    onTertiaryContainer = OnBackgroundWhite,
    error = RetroRed,
    onError = OnBackgroundWhite,
    errorContainer = RetroRed,
    onErrorContainer = OnBackgroundWhite,
    background = BackgroundBlack,
    onBackground = OnBackgroundWhite,
    surface = SurfaceBlack,
    onSurface = OnSurfaceWhite,
    surfaceVariant = SurfaceBlack,
    onSurfaceVariant = OnSurfaceWhite,
    outline = RetroGreen
)

private val LightColorScheme = lightColorScheme(
    primary = RetroGreen,
    onPrimary = BackgroundBlack,
    primaryContainer = RetroBlue,
    onPrimaryContainer = OnBackgroundWhite,
    secondary = RetroYellow,
    onSecondary = BackgroundBlack,
    secondaryContainer = RetroPurple,
    onSecondaryContainer = OnBackgroundWhite,
    tertiary = RetroCyan,
    onTertiary = BackgroundBlack,
    tertiaryContainer = RetroOrange,
    onTertiaryContainer = OnBackgroundWhite,
    error = RetroRed,
    onError = OnBackgroundWhite,
    errorContainer = RetroRed,
    onErrorContainer = OnBackgroundWhite,
    background = BackgroundBlack,
    onBackground = OnBackgroundWhite,
    surface = SurfaceBlack,
    onSurface = OnSurfaceWhite,
    surfaceVariant = SurfaceBlack,
    onSurfaceVariant = OnSurfaceWhite,
    outline = RetroGreen
)

@Composable
fun HardwareinfoTheme(
    darkTheme: Boolean = true, // Force dark theme for pure black background
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
