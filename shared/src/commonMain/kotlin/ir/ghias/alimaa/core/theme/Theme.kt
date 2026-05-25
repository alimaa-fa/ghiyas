package ir.ghias.alimaa.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = BackgroundSoft,
    surface = SurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun QiyasTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = QiyasTypography,
        content = content
    )
}
