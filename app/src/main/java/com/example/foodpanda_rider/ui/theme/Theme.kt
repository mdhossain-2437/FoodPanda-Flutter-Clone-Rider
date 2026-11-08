package com.example.foodpanda_rider.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.foodpanda_rider.R

val FoodPandaPrimary = Color(0xFFD60265)
val FoodPandaPrimaryContainer = Color(0xFFC63065)
val FoodPandaSecondary = Color(0xFF1E1E1E)
val FoodPandaBackground = Color(0xFFF6F6F6)
val FoodPandaSurface = Color(0xFFFFFFFF)
val FoodPandaError = Color(0xFFE74C3C)
val SecondaryTextColor = Color(0xFF707070)
val TitleTextColor = Color(0xFF323232)
val BorderGrayColor = Color(0xFFE5E5E5)

val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins, FontWeight.Normal),
    Font(R.font.poppins, FontWeight.Medium),
    Font(R.font.poppins, FontWeight.SemiBold),
    Font(R.font.poppins, FontWeight.Bold)
)

private val FoodPandaColorScheme = lightColorScheme(
    primary = FoodPandaPrimary,
    onPrimary = Color.White,
    primaryContainer = FoodPandaPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = FoodPandaSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color.Black,
    onSecondaryContainer = Color.White,
    background = FoodPandaBackground,
    onBackground = FoodPandaSecondary,
    surface = FoodPandaSurface,
    onSurface = FoodPandaSecondary,
    surfaceVariant = Color(0xFFFDF2F7),
    onSurfaceVariant = SecondaryTextColor,
    error = FoodPandaError,
    onError = Color.White
)

val FoodPandaTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun FoodPandaRiderTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FoodPandaColorScheme,
        typography = FoodPandaTypography,
        content = content
    )
}
