package com.wortschatz.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * The palette borrows Duolingo's friendly, high-energy feel: a vivid learning green,
 * warm streak golds, sky blue and soft red for errors. Every pair used for text
 * keeps a comfortable contrast ratio (dark ink on bright fills, not white on green).
 */

internal val DuoGreen = Color(0xFF58CC02)
internal val DuoGreenShadow = Color(0xFF46A302)
internal val DuoInk = Color(0xFF263238)
internal val DuoGold = Color(0xFFFFC800)
internal val DuoOrange = Color(0xFFFF9600)
internal val DuoBlue = Color(0xFF1CB0F6)
internal val DuoRed = Color(0xFFEA2B2B)

@Immutable
data class DuoColors(
    val green: Color,
    val greenDark: Color,
    val greenSoft: Color,
    val greenInk: Color,
    /** Ink for text/icons on a solid green fill (e.g. primary buttons). */
    val greenSolidInk: Color,
    val blue: Color,
    val blueDeep: Color,
    val blueSoft: Color,
    val streak: Color,
    val streakDeep: Color,
    val streakSoft: Color,
    val gold: Color,
    val goldDeep: Color,
    /** Ink for text/icons on a solid gold fill. */
    val goldSolidInk: Color,
    val red: Color,
    val redSoft: Color,
    val redDeep: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val outline: Color,
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val shadow: Color,
    val disabled: Color,
    val disabledText: Color,
    val scrim: Color,
)

val LightDuoColors = DuoColors(
    green = DuoGreen,
    greenDark = DuoGreenShadow,
    greenSoft = Color(0xFFE9F8DA),
    greenInk = Color(0xFF2F6B00),
    greenSolidInk = Color(0xFF1D3B00),
    blue = DuoBlue,
    blueDeep = Color(0xFF0B6E99),
    blueSoft = Color(0xFFDDF4FF),
    streak = DuoOrange,
    streakDeep = Color(0xFF8A4B00),
    streakSoft = Color(0xFFFFF1D6),
    gold = DuoGold,
    goldDeep = Color(0xFF7A5A00),
    goldSolidInk = Color(0xFF3E2C00),
    red = DuoRed,
    redSoft = Color(0xFFFFE3E3),
    redDeep = Color(0xFF9B1C1C),
    textPrimary = DuoInk,
    textSecondary = Color(0xFF5B6A6F),
    outline = Color(0xFFE1E8E4),
    background = Color(0xFFF7F9F6),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFF0F4F0),
    shadow = Color(0xFFD6DED8),
    disabled = Color(0xFFE7ECE8),
    disabledText = Color(0xFF9AA7A1),
    scrim = Color(0x66000000),
)

val DarkDuoColors = DuoColors(
    green = Color(0xFF6FDB1B),
    greenDark = Color(0xFF3F8A00),
    greenSoft = Color(0xFF22351A),
    greenInk = Color(0xFFB9F58C),
    greenSolidInk = Color(0xFF102400),
    blue = Color(0xFF4FC3F7),
    blueDeep = Color(0xFF9BDFFF),
    blueSoft = Color(0xFF14303C),
    streak = Color(0xFFFFB020),
    streakDeep = Color(0xFFFFD18A),
    streakSoft = Color(0xFF332914),
    gold = Color(0xFFFFD54F),
    goldDeep = Color(0xFFFFE59A),
    goldSolidInk = Color(0xFF3A2A00),
    red = Color(0xFFFF6B6B),
    redSoft = Color(0xFF3A1E1E),
    redDeep = Color(0xFFFFB4B4),
    textPrimary = Color(0xFFF1F7F2),
    textSecondary = Color(0xFFA7B8BE),
    outline = Color(0xFF2C414B),
    background = Color(0xFF0E1A20),
    surface = Color(0xFF17272F),
    surfaceMuted = Color(0xFF1F323B),
    shadow = Color(0xFF000000),
    disabled = Color(0xFF26363E),
    disabledText = Color(0xFF7D8F95),
    scrim = Color(0x99000000),
)

val LocalDuoColors = staticCompositionLocalOf { LightDuoColors }
