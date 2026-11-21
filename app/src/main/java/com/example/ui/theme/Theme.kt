package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun FriendsPortfolioTheme(
    themeId: GlassThemeId = GlassThemeId.LAVENDER,
    content: @Composable () -> Unit
) {
    val palette = paletteForTheme(themeId)
    val colorScheme = materialColorSchemeFor(palette)

    CompositionLocalProvider(LocalGlassPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
