package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = Color.White,
    secondary = SecondarySlateLight,
    onSecondary = Color.Black,
    secondaryContainer = SecondarySlateDark,
    onSecondaryContainer = Color.White,
    tertiary = WarningAmber,
    onTertiary = Color.Black,
    tertiaryContainer = OnWarningAmberContainer,
    onTertiaryContainer = WarningAmberContainer,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkBorder,
    error = ErrorRed,
    errorContainer = OnErrorRedContainer,
    onError = Color.White,
    onErrorContainer = ErrorRedContainer
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenContainer,
    onPrimaryContainer = OnPrimaryGreenContainer,
    secondary = SecondarySlate,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = WarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = WarningAmberContainer,
    onTertiaryContainer = OnWarningAmberContainer,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightBorder,
    error = ErrorRed,
    errorContainer = ErrorRedContainer,
    onError = Color.White,
    onErrorContainer = OnErrorRedContainer
)

@Composable
fun SmartStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
