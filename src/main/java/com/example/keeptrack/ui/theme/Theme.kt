package com.example.keeptrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KeepTrackLightColorScheme = lightColorScheme(

    // ---------------------------------------------------------
    // PRIMARY
    // ---------------------------------------------------------

    primary = KeepTrackBlue,
    onPrimary = KeepTrackWhite,

    primaryContainer = KeepTrackLightBlue,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.Black,

    // ---------------------------------------------------------
    // SECONDARY
    // ---------------------------------------------------------

    secondary = KeepTrackDarkBlue,
    onSecondary = KeepTrackWhite,

    secondaryContainer = KeepTrackLightBlue,
    onSecondaryContainer = androidx.compose.ui.graphics.Color.Black,

    // ---------------------------------------------------------
    // BACKGROUND
    // ---------------------------------------------------------

    background = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color.Black,

    // ---------------------------------------------------------
    // SURFACE
    // ---------------------------------------------------------

    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.Black,

    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF444444),

    // ---------------------------------------------------------
    // OUTLINES
    // ---------------------------------------------------------

    outline = androidx.compose.ui.graphics.Color(0xFF777777),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFCCCCCC),

    // ---------------------------------------------------------
    // ERROR
    // ---------------------------------------------------------

    error = KeepTrackError,
    onError = KeepTrackWhite
)

private val KeepTrackDarkColorScheme = darkColorScheme(

    // ---------------------------------------------------------
    // PRIMARY
    // ---------------------------------------------------------

    primary = KeepTrackBlue,
    onPrimary = KeepTrackWhite,

    primaryContainer = KeepTrackDarkBlue,
    onPrimaryContainer = KeepTrackWhite,

    // ---------------------------------------------------------
    // SECONDARY
    // ---------------------------------------------------------

    secondary = KeepTrackBlue,
    onSecondary = KeepTrackWhite,

    secondaryContainer = KeepTrackDarkBlue,
    onSecondaryContainer = KeepTrackWhite,

    // ---------------------------------------------------------
    // BACKGROUND
    // ---------------------------------------------------------

    background = KeepTrackBlack,
    onBackground = KeepTrackWhite,

    // ---------------------------------------------------------
    // SURFACE
    // ---------------------------------------------------------

    surface = KeepTrackBlack,
    onSurface = KeepTrackWhite,

    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF303030),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFDDDDDD),

    // ---------------------------------------------------------
    // OUTLINES
    // ---------------------------------------------------------

    outline = androidx.compose.ui.graphics.Color(0xFFAAAAAA),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF555555),

    // ---------------------------------------------------------
    // ERROR
    // ---------------------------------------------------------

    error = KeepTrackError,
    onError = KeepTrackWhite
)

@Composable
fun KeepTrackTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme = when {
        darkTheme -> KeepTrackDarkColorScheme
        else -> KeepTrackLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}