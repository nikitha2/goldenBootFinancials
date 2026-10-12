package com.goldenboot.goldenbootfinancial.core.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Reference Color Palettes
// =============================================================================

object GoldenbootColors {
    // Blue
    val Blue50 = Color(0xFFF3FAFF)
    val Blue100 = Color(0xFFE7F5FF)
    val Blue150 = Color(0xFFD2EBFF)
    val Blue200 = Color(0xFFA5D8FF)
    val Blue300 = Color(0xFF4DABF7)
    val Blue400 = Color(0xFF228BE6)
    val Blue500 = Color(0xFF106BC7)
    val Blue600 = Color(0xFF004C8F)
    val Blue700 = Color(0xFF0D375E)
    val Blue800 = Color(0xFF0A2237)

    // Slate
    val Slate50 = Color(0xFFFAFBFD)
    val Slate100 = Color(0xFFF6F9FC)
    val Slate150 = Color(0xFFEEF1F6)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate300 = Color(0xFFBECBDA)
    val Slate400 = Color(0xFF8496AE)
    val Slate500 = Color(0xFF64748B)
    val Slate600 = Color(0xFF475569)
    val Slate700 = Color(0xFF1E293B)
    val Slate800 = Color(0xFF151D29)

    // Gray
    val Gray50 = Color(0xFFFBFBFC)
    val Gray100 = Color(0xFFF6F7F9)
    val Gray150 = Color(0xFFEDEFF1)
    val Gray200 = Color(0xFFE1E4E8)
    val Gray300 = Color(0xFFBDC1C7)
    val Gray400 = Color(0xFF8B949D)
    val Gray500 = Color(0xFF6A737D)
    val Gray600 = Color(0xFF444D56)
    val Gray700 = Color(0xFF24292E)
    val Gray800 = Color(0xFF1A1D21)

    // Red
    val Red50 = Color(0xFFFFFAFA)
    val Red100 = Color(0xFFFFF5F5)
    val Red150 = Color(0xFFFFE9E9)
    val Red200 = Color(0xFFFFC9C9)
    val Red300 = Color(0xFFFF8787)
    val Red400 = Color(0xFFFA5252)
    val Red500 = Color(0xFFE03131)
    val Red600 = Color(0xFFB02525)
    val Red700 = Color(0xFF7D1A1A)
    val Red800 = Color(0xFF5B1313)

    // Green
    val Green50 = Color(0xFFF5FEFA)
    val Green100 = Color(0xFFEBFDF5)
    val Green150 = Color(0xFFCBFADB)
    val Green200 = Color(0xFF96F5B6)
    val Green300 = Color(0xFF16D97B)
    val Green400 = Color(0xFF13BF6D)
    val Green500 = Color(0xFF0F9654)
    val Green600 = Color(0xFF0A6A3C)
    val Green700 = Color(0xFF05361F)
    val Green800 = Color(0xFF032012)

    // Yellow
    val Yellow50 = Color(0xFFFFFCED)
    val Yellow100 = Color(0xFFFFF9DB)
    val Yellow150 = Color(0xFFFFF4C2)
    val Yellow200 = Color(0xFFFFEC99)
    val Yellow300 = Color(0xFFFFD43B)
    val Yellow400 = Color(0xFFFAB005)
    val Yellow500 = Color(0xFFE27808)
    val Yellow600 = Color(0xFFB35C00)
    val Yellow700 = Color(0xFF663500)
    val Yellow800 = Color(0xFF3A1E00)

    // Orange
    val Orange100 = Color(0xFFFFF4E6)
    val Orange200 = Color(0xFFFFD8A8)
    val Orange300 = Color(0xFFFFA94D)
    val Orange400 = Color(0xFFFD7E14)
    val Orange500 = Color(0xFFF76707)
    val Orange600 = Color(0xFFBF400D)

    // Indigo
    val Indigo100 = Color(0xFFEDF2FF)
    val Indigo200 = Color(0xFFBAC8FF)
    val Indigo300 = Color(0xFF6E89F7)
    val Indigo400 = Color(0xFF4C6EF5)
    val Indigo500 = Color(0xFF3B5BDB)
    val Indigo600 = Color(0xFF2F44AD)
    val Indigo700 = Color(0xFF1B2764)
    val Indigo800 = Color(0xFF141C47)

    // Cyan
    val Cyan100 = Color(0xFFE3FAFC)
    val Cyan300 = Color(0xFF3BC9DB)
    val Cyan500 = Color(0xFF0C8599)

    // Violet
    val Violet100 = Color(0xFFF3F0FF)
    val Violet200 = Color(0xFFD0BFFF)
    val Violet500 = Color(0xFF6741D9)
    val Violet600 = Color(0xFF5235AB)

    // Pink
    val Pink100 = Color(0xFFFFF0F6)
    val Pink300 = Color(0xFFF783AC)
    val Pink500 = Color(0xFFC2255C)
    val Pink600 = Color(0xFF8C1941)

    // Grape
    val Grape200 = Color(0xFFEEBEFA)
    val Grape600 = Color(0xFF702682)

    // Lime
    val Lime150 = Color(0xFFE7FFC1)
    val Lime300 = Color(0xFFA9E34B)

    // Other
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
}

// =============================================================================
// Brand Colors
// =============================================================================
val GoldenbootBrandPrimary = GoldenbootColors.Blue400    // #228BE6
val GoldenbootBrandSecondary = GoldenbootColors.Blue700  // #0D375E
val GoldenbootBrandTertiary = GoldenbootColors.Yellow400 // #FAB005

// Legacy aliases
val GoldenbootNavy = GoldenbootColors.Blue700
val GoldenbootBlue = GoldenbootColors.Blue400
val GoldenbootWhite = GoldenbootColors.White
val GoldenbootBlack = GoldenbootColors.Black

// =============================================================================
// Light Theme Colors
// =============================================================================

val LightPrimary = Color(0xFFFDE84F)
val LightOnPrimary = Color(0xFF1A1A1A)
val LightPrimaryContainer = GoldenbootColors.Blue100
val LightOnPrimaryContainer = GoldenbootColors.Blue800

val LightSecondary = GoldenbootColors.Blue700
val LightOnSecondary = GoldenbootColors.White
val LightSecondaryContainer = GoldenbootColors.Blue200
val LightOnSecondaryContainer = GoldenbootColors.Blue700

val LightTertiary = GoldenbootColors.Yellow400
val LightOnTertiary = GoldenbootColors.Blue800
val LightTertiaryContainer = GoldenbootColors.Orange100
val LightOnTertiaryContainer = GoldenbootColors.Orange600

val LightBackground = GoldenbootColors.White
val LightOnBackground = GoldenbootColors.Slate600
val LightSurface = GoldenbootColors.White
val LightOnSurface = GoldenbootColors.Slate600
val LightSurfaceVariant = GoldenbootColors.Slate200
val LightOnSurfaceVariant = GoldenbootColors.Slate500

val LightError = GoldenbootColors.Red500
val LightOnError = GoldenbootColors.White

val LightOutline = GoldenbootColors.Slate400
val LightOutlineVariant = GoldenbootColors.Slate200

// =============================================================================
// Dark Theme Colors
// =============================================================================

val DarkPrimary = GoldenbootColors.Blue300
val DarkOnPrimary = GoldenbootColors.Blue800
val DarkPrimaryContainer = GoldenbootColors.Blue700
val DarkOnPrimaryContainer = GoldenbootColors.Blue100

val DarkSecondary = GoldenbootColors.Blue200
val DarkOnSecondary = GoldenbootColors.Blue800
val DarkSecondaryContainer = GoldenbootColors.Blue600
val DarkOnSecondaryContainer = GoldenbootColors.Blue200

val DarkTertiary = GoldenbootColors.Yellow300
val DarkOnTertiary = GoldenbootColors.Yellow800
val DarkTertiaryContainer = GoldenbootColors.Yellow700
val DarkOnTertiaryContainer = GoldenbootColors.Yellow300

val DarkBackground = GoldenbootColors.Slate800
val DarkOnBackground = GoldenbootColors.Slate200
val DarkSurface = GoldenbootColors.Slate800
val DarkOnSurface = GoldenbootColors.Slate200
val DarkSurfaceVariant = GoldenbootColors.Slate600
val DarkOnSurfaceVariant = GoldenbootColors.Slate400

val DarkError = GoldenbootColors.Red300
val DarkOnError = GoldenbootColors.Red800

val DarkOutline = GoldenbootColors.Slate400
val DarkOutlineVariant = GoldenbootColors.Slate600
