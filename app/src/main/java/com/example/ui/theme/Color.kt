package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class GlassThemeId(val id: String, val displayName: String) {
    LAVENDER("lavender", "Lavender"),
    AURORA("aurora", "Aurora"),
    ROSEWOOD("rosewood", "Rosewood"),
    OCEAN("ocean", "Ocean");

    companion object {
        fun fromId(raw: String?): GlassThemeId =
            entries.firstOrNull { it.id == raw } ?: LAVENDER
    }
}

@Immutable
data class GlassPalette(
    val themeId: GlassThemeId,
    val bg1: Color,
    val bg2: Color,
    val bg3: Color,
    val accent: Color,
    val accent2: Color,
    val gold: Color,
    val gold2: Color,
    val goldSoftText: Color,
    val bloodBg: Color,
    val bloodBorder: Color,
    val bloodText: Color,
    val glass: Color,
    val glassStrong: Color,
    val glassBorder: Color,
    val topBarBg: Color,
    val inputBg: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val particle1: Color,
    val particle2: Color,
    val aurora1: Color,
    val aurora2: Color,
    val dotPreviewColor: Color
)

private val SharedGold = Color(0xFFFBBF24)
private val SharedGold2 = Color(0xFFF59E0B)
private val SharedGoldSoft = Color(0xFFFDE68A)
private val SharedBloodBg = Color(0x38F43F5E)
private val SharedBloodBorder = Color(0x8CFB7185)
private val SharedBloodText = Color(0xFFFECDD3)
private val SharedGlass = Color(0x18FFFFFF)
private val SharedGlassStrong = Color(0x26FFFFFF)
private val SharedGlassBorder = Color(0x38FFFFFF)
private val SharedTopBarBg = Color(0x8C0A0A19)
private val SharedInputBg = Color(0x660F172A)
private val SharedTextPrimary = Color(0xFFF8FAFC)
private val SharedTextMuted = Color(0xFFCBD5E1)

val LavenderGlassPalette = GlassPalette(
    themeId = GlassThemeId.LAVENDER,
    bg1 = Color(0xFF16102B),
    bg2 = Color(0xFF2C1A4D),
    bg3 = Color(0xFF10203A),
    accent = Color(0xFFC084FC),
    accent2 = Color(0xFFF472B6),
    gold = SharedGold,
    gold2 = SharedGold2,
    goldSoftText = SharedGoldSoft,
    bloodBg = SharedBloodBg,
    bloodBorder = SharedBloodBorder,
    bloodText = SharedBloodText,
    glass = SharedGlass,
    glassStrong = SharedGlassStrong,
    glassBorder = SharedGlassBorder,
    topBarBg = SharedTopBarBg,
    inputBg = SharedInputBg,
    textPrimary = SharedTextPrimary,
    textMuted = SharedTextMuted,
    particle1 = Color(0xA6C084FC),
    particle2 = Color(0x8CF472B6),
    aurora1 = Color(0x47A855F7),
    aurora2 = Color(0x38EC4899),
    dotPreviewColor = Color(0xFFC084FC)
)

val AuroraGlassPalette = GlassPalette(
    themeId = GlassThemeId.AURORA,
    bg1 = Color(0xFF071324),
    bg2 = Color(0xFF0F2942),
    bg3 = Color(0xFF091D26),
    accent = Color(0xFF38BDF8),
    accent2 = Color(0xFF34D399),
    gold = SharedGold,
    gold2 = SharedGold2,
    goldSoftText = SharedGoldSoft,
    bloodBg = SharedBloodBg,
    bloodBorder = SharedBloodBorder,
    bloodText = SharedBloodText,
    glass = SharedGlass,
    glassStrong = SharedGlassStrong,
    glassBorder = SharedGlassBorder,
    topBarBg = Color(0x8C05101F),
    inputBg = SharedInputBg,
    textPrimary = SharedTextPrimary,
    textMuted = SharedTextMuted,
    particle1 = Color(0xA638BDF8),
    particle2 = Color(0x8C34D399),
    aurora1 = Color(0x420EA5E9),
    aurora2 = Color(0x3D10B981),
    dotPreviewColor = Color(0xFF38BDF8)
)

val RosewoodGlassPalette = GlassPalette(
    themeId = GlassThemeId.ROSEWOOD,
    bg1 = Color(0xFF240C18),
    bg2 = Color(0xFF47162A),
    bg3 = Color(0xFF2A1220),
    accent = Color(0xFFFB7185),
    accent2 = Color(0xFFFDBA74),
    gold = SharedGold,
    gold2 = SharedGold2,
    goldSoftText = SharedGoldSoft,
    bloodBg = SharedBloodBg,
    bloodBorder = SharedBloodBorder,
    bloodText = SharedBloodText,
    glass = SharedGlass,
    glassStrong = SharedGlassStrong,
    glassBorder = SharedGlassBorder,
    topBarBg = Color(0x8C1E0913),
    inputBg = SharedInputBg,
    textPrimary = SharedTextPrimary,
    textMuted = SharedTextMuted,
    particle1 = Color(0xA6FB7185),
    particle2 = Color(0x8CFDBA74),
    aurora1 = Color(0x42F43F5E),
    aurora2 = Color(0x38FB923C),
    dotPreviewColor = Color(0xFFFB7185)
)

val OceanGlassPalette = GlassPalette(
    themeId = GlassThemeId.OCEAN,
    bg1 = Color(0xFF061826),
    bg2 = Color(0xFF0C3547),
    bg3 = Color(0xFF0A2438),
    accent = Color(0xFF22D3EE),
    accent2 = Color(0xFF818CF8),
    gold = SharedGold,
    gold2 = SharedGold2,
    goldSoftText = SharedGoldSoft,
    bloodBg = SharedBloodBg,
    bloodBorder = SharedBloodBorder,
    bloodText = SharedBloodText,
    glass = SharedGlass,
    glassStrong = SharedGlassStrong,
    glassBorder = SharedGlassBorder,
    topBarBg = Color(0x8C04121E),
    inputBg = SharedInputBg,
    textPrimary = SharedTextPrimary,
    textMuted = SharedTextMuted,
    particle1 = Color(0xA622D3EE),
    particle2 = Color(0x8C818CF8),
    aurora1 = Color(0x4206B6D4),
    aurora2 = Color(0x3D6366F1),
    dotPreviewColor = Color(0xFF22D3EE)
)

fun paletteForTheme(themeId: GlassThemeId): GlassPalette = when (themeId) {
    GlassThemeId.LAVENDER -> LavenderGlassPalette
    GlassThemeId.AURORA -> AuroraGlassPalette
    GlassThemeId.ROSEWOOD -> RosewoodGlassPalette
    GlassThemeId.OCEAN -> OceanGlassPalette
}

fun materialColorSchemeFor(palette: GlassPalette): ColorScheme = darkColorScheme(
    primary = palette.accent,
    onPrimary = Color(0xFF140D24),
    primaryContainer = palette.bg2,
    onPrimaryContainer = palette.textPrimary,
    secondary = palette.accent2,
    onSecondary = Color(0xFF1B0B14),
    secondaryContainer = palette.bg3,
    onSecondaryContainer = palette.textPrimary,
    tertiary = palette.gold,
    onTertiary = Color(0xFF1F1300),
    background = palette.bg1,
    onBackground = palette.textPrimary,
    surface = palette.bg2,
    onSurface = palette.textPrimary,
    surfaceVariant = Color(0xFF211A3D),
    onSurfaceVariant = palette.textMuted,
    outline = palette.glassBorder,
    error = Color(0xFFFB7185),
    onError = Color(0xFF2D0610)
)

val LocalGlassPalette = staticCompositionLocalOf { LavenderGlassPalette }
