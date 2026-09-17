package org.catholiccompanion.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Burgundy = Color(0xFF6D1F35)
private val BurgundyLight = Color(0xFFFFD9E2)
private val Gold = Color(0xFF7A5900)
private val Ivory = Color(0xFFFFF8F3)
private val Ink = Color(0xFF24191C)

private val LightColors = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    primaryContainer = BurgundyLight,
    onPrimaryContainer = Color(0xFF3F0019),
    secondary = Gold,
    background = Ivory,
    onBackground = Ink,
    surface = Color(0xFFFFF8F3),
    onSurface = Ink,
    surfaceVariant = Color(0xFFF3E3E5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB1C5),
    onPrimary = Color(0xFF640B2D),
    primaryContainer = Color(0xFF882A45),
    onPrimaryContainer = BurgundyLight,
    secondary = Color(0xFFF2C34F),
    background = Color(0xFF1B1114),
    onBackground = Color(0xFFF2DEE3),
    surface = Color(0xFF1B1114),
    onSurface = Color(0xFFF2DEE3),
    surfaceVariant = Color(0xFF503D42),
)

@Composable
fun CatholicCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CatholicTypography,
        content = content,
    )
}

