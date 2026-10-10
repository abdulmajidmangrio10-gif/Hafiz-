package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.local.AccentColorChoice
import com.example.data.local.AppThemeMode

@Composable
fun QuranVideoEditorTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentChoice: AccentColorChoice = AccentColorChoice.GOLD,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val accent = when (accentChoice) {
        AccentColorChoice.GOLD -> QuranGoldPrimary
        AccentColorChoice.TEAL -> IslamicTeal
        AccentColorChoice.BLUE -> RoyalBlue
        AccentColorChoice.PURPLE -> MajesticPurple
        AccentColorChoice.GRAY -> Color(0xFF8B949E)
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            primaryContainer = accent.copy(alpha = 0.2f),
            onPrimaryContainer = QuranGoldLight,
            secondary = IslamicTeal,
            onSecondary = Color.Black,
            background = BackgroundDark,
            onBackground = TextWhite,
            surface = SurfaceDark,
            onSurface = TextWhite,
            surfaceVariant = SurfaceVariantDark,
            onSurfaceVariant = TextGray,
            outline = CardBorderDark,
            error = PlayheadRed
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = accent.copy(alpha = 0.15f),
            onPrimaryContainer = Color(0xFF7A5400),
            secondary = IslamicTeal,
            onSecondary = Color.White,
            background = Color(0xFFF6F8FA),
            onBackground = Color(0xFF1F2328),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1F2328),
            surfaceVariant = Color(0xFFEAEFF5),
            onSurfaceVariant = Color(0xFF57606A),
            outline = Color(0xFFD0D7DE),
            error = PlayheadRed
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) BackgroundDark.toArgb() else Color(0xFFF6F8FA).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
