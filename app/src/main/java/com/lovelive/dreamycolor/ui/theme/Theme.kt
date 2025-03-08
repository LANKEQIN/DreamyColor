package com.lovelive.dreamycolor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.lovelive.dreamycolor.SettingsManager
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color


// Material You的默认颜色方案
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// 紫色主题
private val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB69DF8),
    secondary = Color(0xFF8F7BA4),
    tertiary = Color(0xFFD0BCFF)
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260)
)

// 丹红色主题
private val RoseDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    secondary = Color(0xFFF2B8B5),
    tertiary = Color(0xFFFFD8E4)
)

private val RoseLightColorScheme = lightColorScheme(
    primary = Color(0xFFBF1B2C),
    secondary = Color(0xFFA73638),
    tertiary = Color(0xFFBF3B44)
)

// 浅蓝色主题
private val LightBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DCEFF),
    secondary = Color(0xFFBBC7DB),
    tertiary = Color(0xFFB8E5FF)
)

private val LightBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    secondary = Color(0xFF535F70),
    tertiary = Color(0xFF006397)
)

@Composable
fun DreamyColorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: SettingsManager.ThemeMode = SettingsManager.ThemeMode.FOLLOW_SYSTEM,
    textSize: SettingsManager.TextSize,
    dynamicColor: Boolean = true,
    colorTheme: SettingsManager.ColorTheme = SettingsManager.ColorTheme.MATERIAL_YOU,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        SettingsManager.ThemeMode.LIGHT -> false
        SettingsManager.ThemeMode.DARK -> true
        else -> darkTheme // 跟随系统
    }

    // 字体缩放计算
    val textScaleRatio = remember(textSize) {
        when (textSize) {
            SettingsManager.TextSize.FOLLOW_SYSTEM -> 1.0f
            SettingsManager.TextSize.SMALL -> 0.85f
            SettingsManager.TextSize.MEDIUM -> 1.0f
            SettingsManager.TextSize.LARGE -> 1.15f
        }
    }

    val scaledTypography = remember(textScaleRatio) {
        Typography.scaleStyle(textScaleRatio)
    }

    val colorScheme = when (colorTheme) {
        SettingsManager.ColorTheme.MATERIAL_YOU -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (useDarkTheme) DarkColorScheme else LightColorScheme
            }
        }
        SettingsManager.ColorTheme.PURPLE -> {
            if (useDarkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        }
        SettingsManager.ColorTheme.ROSE -> {
            if (useDarkTheme) RoseDarkColorScheme else RoseLightColorScheme
        }
        SettingsManager.ColorTheme.LIGHT_BLUE -> {
            if (useDarkTheme) LightBlueDarkColorScheme else LightBlueLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
















