package com.example.companyanalysis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    primary = AccentRed,
    onPrimary = SoftWhite,
    primaryContainer = AccentRedDark,
    background = CoalBlack,
    onBackground = SoftWhite,
    surface = Graphite,
    onSurface = SoftWhite,
    surfaceVariant = SteelGray,
    onSurfaceVariant = AshGray
)

@Composable
fun CompanyAnalysisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
