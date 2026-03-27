package com.encer.splitwise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.encer.splitwise.R

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF1D4ED8),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFB45309),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F7F2),
    onBackground = Color(0xFF122023),
    surface = Color(0xFFFFFCF5),
    onSurface = Color(0xFF122023),
    surfaceVariant = Color(0xFFE3E8DD),
    onSurfaceVariant = Color(0xFF415458),
    outline = Color(0xFF8B9494),
    error = Color(0xFFB91C1C),
    onError = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF072F2D),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF0B2A4B),
    tertiary = Color(0xFFF9C46B),
    onTertiary = Color(0xFF4C3310),
    background = Color(0xFF101B1D),
    onBackground = Color(0xFFF1F6F4),
    surface = Color(0xFF172427),
    onSurface = Color(0xFFF6F8F3),
    surfaceVariant = Color(0xFF334045),
    onSurfaceVariant = Color(0xFFC2CFD0),
    outline = Color(0xFF8A989A),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF5C1410)
)

private val IranYekan = FontFamily(
    Font(R.raw.iran_yekan_thin, FontWeight.Thin),
    Font(R.raw.iran_yekan_light, FontWeight.Light),
    Font(R.raw.iran_yekan_regular, FontWeight.Normal),
    Font(R.raw.iran_yekan_medium, FontWeight.Medium),
    Font(R.raw.iran_yekan_bold, FontWeight.Bold),
    Font(R.raw.iran_yekan_extra_bold, FontWeight.ExtraBold),
    Font(R.raw.iran_yekan_black, FontWeight.Black)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = IranYekan,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 29.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = IranYekan,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 33.sp
    ),
    titleLarge = TextStyle(
        fontFamily = IranYekan,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp
    ),
    titleMedium = TextStyle(
        fontFamily = IranYekan,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = IranYekan,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = IranYekan,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = IranYekan,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp
    )
)

@Composable
fun SplitwiseTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
