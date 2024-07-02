package me.salby.podcasts.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.salby.podcasts.R

@OptIn(ExperimentalTextApi::class)
val displayFontFamily = FontFamily(
    Font(
        R.font.archivo,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(112.5f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val headlineFontFamily = FontFamily(
    Font(
        R.font.archivo,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.width(105f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val labelFontFamily = FontFamily(
    Font(
        R.font.archivo,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(550),
            FontVariation.width(102.5f)
        )
    )
)

// Set of Material typography styles to start with
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = headlineFontFamily,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = headlineFontFamily,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = headlineFontFamily,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = headlineFontFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = headlineFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = .1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = headlineFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = .1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = labelFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = .1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = labelFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = .5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = labelFontFamily,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = .5.sp
    )
)