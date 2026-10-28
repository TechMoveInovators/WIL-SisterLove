package com.example.slisaconnect.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.slisaconnect.model.TeamStatus

val Night = Color(0xFF18101F)
val NightText = Color(0xFFF4ECFB)
val NightMuted = Color(0xFFBFAED1)
val Lime = Color(0xFFC9EA6B)
val LimeText = Color(0xFF1C2A00)
val Magenta = Color(0xFFC0179A)

// Dashboard tiles from the design. They keep their colours in both themes and always carry dark text.
val TileBlue = Color(0xFF8DB8D4)
val TilePink = Color(0xFFFF6BF2)
val TileLime = Color(0xFFC6EB6E)
val TileViolet = Color(0xFFC77DFF)
val TileText = Color(0xFF1D1029)

/** The soft purple glow behind the welcome, sign in and sign up screens. */
@Composable
fun authBackground(): Brush =
    if (isSystemInDarkTheme()) Brush.radialGradient(listOf(Color(0xFF3A2354), Color(0xFF1D1129), Color(0xFF150C20)))
    else Brush.radialGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE6D2F6), Color(0xFFC79BE6)))

private val LightColors = lightColorScheme(
    primary = Color(0xFF6E2AA8), onPrimary = Color.White,
    primaryContainer = Color(0xFFEBD9FA), onPrimaryContainer = Color(0xFF3A0B63),
    secondary = Magenta, onSecondary = Color.White,
    tertiary = Lime, onTertiary = LimeText,
    background = Color(0xFFF7EEFE), onBackground = Color(0xFF1D1029),
    surface = Color.White, onSurface = Color(0xFF1D1029),
    surfaceVariant = Color(0xFFF1E6FA), onSurfaceVariant = Color(0xFF665777),
    outline = Color(0xFFE0D2EE),
    error = Color(0xFFA31D1D), errorContainer = Color(0xFFFDE0E0), onErrorContainer = Color(0xFFA31D1D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9A4BE0), onPrimary = Color.White,
    primaryContainer = Color(0xFF3A1A5C), onPrimaryContainer = Color(0xFFEBD9FA),
    secondary = Color(0xFFF06AC8), onSecondary = Color(0xFF30001F),
    tertiary = Lime, onTertiary = LimeText,
    background = Color(0xFF150C20), onBackground = Color(0xFFF3EAFB),
    surface = Color(0xFF261736), onSurface = Color(0xFFF3EAFB),
    surfaceVariant = Color(0xFF2F1D43), onSurfaceVariant = Color(0xFFB8A6CA),
    outline = Color(0xFF3D2A56),
    error = Color(0xFFF4A1A1), errorContainer = Color(0xFF4A1B1B), onErrorContainer = Color(0xFFF4A1A1),
)

@Composable
fun SlisaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}

/** Background and text colours for each registration status, tuned for both themes. */
@Composable
fun statusColors(status: TeamStatus): Pair<Color, Color> {
    val dark = isSystemInDarkTheme()
    return when (status) {
        TeamStatus.SUBMITTED -> if (dark) Color(0xFF26334F) to Color(0xFFA9C1F5) else Color(0xFFE3E9F7) to Color(0xFF2C4A8C)
        TeamStatus.UNDER_REVIEW -> if (dark) Color(0xFF4A3A10) to Color(0xFFF2CF7A) else Color(0xFFFDF0CF) to Color(0xFF7A5200)
        TeamStatus.APPROVED -> if (dark) Color(0xFF173A1E) to Color(0xFF8FD69A) else Color(0xFFDFF3DD) to Color(0xFF1D6A2A)
        TeamStatus.ACTION_REQUIRED -> if (dark) Color(0xFF4A1B1B) to Color(0xFFF4A1A1) else Color(0xFFFDE0E0) to Color(0xFFA31D1D)
    }
}

@Composable
fun goodColors(): Pair<Color, Color> =
    if (isSystemInDarkTheme()) Color(0xFF173A1E) to Color(0xFF8FD69A) else Color(0xFFDFF3DD) to Color(0xFF1D6A2A)

@Composable
fun warnColors(): Pair<Color, Color> =
    if (isSystemInDarkTheme()) Color(0xFF4A3A10) to Color(0xFFF2CF7A) else Color(0xFFFDF0CF) to Color(0xFF7A5200)
