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
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF8F7BA4),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFD0BCFF),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFEDEDED),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

// 丹红色主题
private val RoseDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFF2B8B5),
    onSecondary = Color(0xFF601410),
    secondaryContainer = Color(0xFF7D2B25),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFFFD8E4),
    onTertiary = Color(0xFF31111D),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF201A1A),
    onBackground = Color(0xFFEDE0DF),
    surface = Color(0xFF201A1A),
    onSurface = Color(0xFFEDE0DF),
    surfaceVariant = Color(0xFF534341),
    onSurfaceVariant = Color(0xFFD8C2BF),
    outline = Color(0xFFA08C8A),
    outlineVariant = Color(0xFF534341)
)

private val RoseLightColorScheme = lightColorScheme(
    primary = Color(0xFFBF1B2C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFA73638),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = Color(0xFFBF3B44),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFEDEDED),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857371),
    outlineVariant = Color(0xFFD8C2BF)
)

// 浅蓝色主题
private val LightBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DCEFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004881),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFFB8E5FF),
    onTertiary = Color(0xFF003547),
    tertiaryContainer = Color(0xFF004D66),
    onTertiaryContainer = Color(0xFFBDE9FF),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7CF),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E)
)

private val LightBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF006397),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCDE5FF),
    onTertiaryContainer = Color(0xFF001D31),
    background = Color(0xFFEDEDED),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7CF)
)

// 橙色主题
private val OrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9800), // 橙色
    onPrimary = Color(0xFFFFFFFF), // 白色
    primaryContainer = Color(0xFF422800), // 深棕色
    onPrimaryContainer = Color(0xFFFFE08B), // 浅橙色
    secondary = Color(0xFFFFB74D), // 浅橙色
    onSecondary = Color(0xFF000000), // 黑色
    secondaryContainer = Color(0xFF6E4000), // 深棕色
    onSecondaryContainer = Color(0xFFFFE08B), // 浅橙色
    tertiary = Color(0xFFFFD8E4), // 浅粉红色
    onTertiary = Color(0xFF442B23), // 棕色
    tertiaryContainer = Color(0xFF5D3F37), // 深棕色
    onTertiaryContainer = Color(0xFFFFDBD1), // 浅粉红色
    background = Color(0xFF1C1B1F), // 深灰色
    onBackground = Color(0xFFE6E1E5), // 浅灰色
    surface = Color(0xFF1C1B1F), // 深灰色
    onSurface = Color(0xFFE6E1E5), // 浅灰色
    surfaceVariant = Color(0xFF49454F), // 深灰色
    onSurfaceVariant = Color(0xFFCAC4D0), // 浅灰色
    outline = Color(0xFF938F99), // 灰色
    outlineVariant = Color(0xFF49454F) // 深灰色
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF9800), // 橙色
    onPrimary = Color(0xFFFFFFFF), // 白色
    primaryContainer = Color(0xFFFFE08B), // 浅橙色
    onPrimaryContainer = Color(0xFF422800), // 深棕色
    secondary = Color(0xFFFFB74D), // 浅橙色
    onSecondary = Color(0xFF000000), // 黑色
    secondaryContainer = Color(0xFFFFE08B), // 浅橙色
    onSecondaryContainer = Color(0xFF422800), // 深棕色
    tertiary = Color(0xFFFFD8E4), // 浅粉红色
    onTertiary = Color(0xFF442B23), // 棕色
    tertiaryContainer = Color(0xFFFFE08B), // 浅橙色
    onTertiaryContainer = Color(0xFF422800), // 深棕色
    background = Color(0xFFEDEDED), // 白色
    onBackground = Color(0xFF1C1B1F), // 深灰色
    surface = Color(0xFFFFFFFF), // 白色
    onSurface = Color(0xFF1C1B1F), // 深灰色
    surfaceVariant = Color(0xFFF5F0E5), // 浅米色
    onSurfaceVariant = Color(0xFF4A4639), // 棕色
    outline = Color(0xFF7B7767), // 灰色
    outlineVariant = Color(0xFFCDC6B4) // 浅灰色
)

// 深蓝色主题
private val DeepBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DB2FF),
    onPrimary = Color(0xFF002785),
    primaryContainer = Color(0xFF0039B9),
    onPrimaryContainer = Color(0xFFDBE1FF),
    secondary = Color(0xFFB3BAE5),
    onSecondary = Color(0xFF252F4C),
    secondaryContainer = Color(0xFF3B4664),
    onSecondaryContainer = Color(0xFFDBE1FF),
    tertiary = Color(0xFFCFD6FF),
    onTertiary = Color(0xFF252F4C),
    tertiaryContainer = Color(0xFF3B4664),
    onTertiaryContainer = Color(0xFFDBE1FF),
    background = Color(0xFF1B1B1F),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F)
)

private val DeepBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0033BF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBE1FF),
    onPrimaryContainer = Color(0xFF00174D),
    secondary = Color(0xFF334BA6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDBE1FF),
    onSecondaryContainer = Color(0xFF00174D),
    tertiary = Color(0xFF526BBF),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDBE1FF),
    onTertiaryContainer = Color(0xFF00174D),
    background = Color(0xFFEDEDED),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF757780),
    outlineVariant = Color(0xFFC4C6D0)
)

// 黄色主题
private val YellowDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFE59D),
    onPrimary = Color(0xFF3F2E00),
    primaryContainer = Color(0xFF5B4300),
    onPrimaryContainer = Color(0xFFFFE08B),
    secondary = Color(0xFFE5DBB3),
    onSecondary = Color(0xFF3F3423),
    secondaryContainer = Color(0xFF574A37),
    onSecondaryContainer = Color(0xFFFFE08B),
    tertiary = Color(0xFFFFDBCF),
    onTertiary = Color(0xFF3F2E1B),
    tertiaryContainer = Color(0xFF57432E),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF1C1C17),
    onBackground = Color(0xFFE6E2D9),
    surface = Color(0xFF1C1C17),
    onSurface = Color(0xFFE6E2D9),
    surfaceVariant = Color(0xFF4A4639),
    onSurfaceVariant = Color(0xFFCDC6B4),
    outline = Color(0xFF969080),
    outlineVariant = Color(0xFF4A4639)
)

private val YellowLightColorScheme = lightColorScheme(
    primary = Color(0xFFBF9B00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE08B),
    onPrimaryContainer = Color(0xFF241A00),
    secondary = Color(0xFFA68E33),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE08B),
    onSecondaryContainer = Color(0xFF241A00),
    tertiary = Color(0xFFBF9B52),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF241A00),
    background = Color(0xFFEDEDED),
    onBackground = Color(0xFF1C1C17),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1C17),
    surfaceVariant = Color(0xFFEAE2CF),
    onSurfaceVariant = Color(0xFF4A4639),
    outline = Color(0xFF7B7767),
    outlineVariant = Color(0xFFCDC6B4)
)

// 粉色主题
private val PinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB5D6),
    secondary = Color(0xFFE5B3C7),
    tertiary = Color(0xFFFFCFE2)
)

private val PinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFBF4B7E),
    secondary = Color(0xFFA6336A),
    tertiary = Color(0xFFBF5286)
)

// 绿色主题
private val GreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9DFFB5),
    secondary = Color(0xFFB3E5BD),
    tertiary = Color(0xFFCFFFD8)
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF00BF4B),
    secondary = Color(0xFF33A648),
    tertiary = Color(0xFF52BF6E)
)

// 白色主题
private val WhiteDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    secondary = Color(0xFFCCCCCC),
    tertiary = Color(0xFFF2F2F2)
)

private val WhiteLightColorScheme = lightColorScheme(
    primary = Color(0xFF757575),
    secondary = Color(0xFF666666),
    tertiary = Color(0xFF999999)
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
        SettingsManager.ColorTheme.ORANGE -> {
            if (useDarkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
        }
        SettingsManager.ColorTheme.DEEP_BLUE -> {
            if (useDarkTheme) DeepBlueDarkColorScheme else DeepBlueLightColorScheme
        }
        SettingsManager.ColorTheme.YELLOW -> {
            if (useDarkTheme) YellowDarkColorScheme else YellowLightColorScheme
        }
        SettingsManager.ColorTheme.PINK -> {
            if (useDarkTheme) PinkDarkColorScheme else PinkLightColorScheme
        }
        SettingsManager.ColorTheme.GREEN -> {
            if (useDarkTheme) GreenDarkColorScheme else GreenLightColorScheme
        }
        SettingsManager.ColorTheme.WHITE -> {
            if (useDarkTheme) WhiteDarkColorScheme else WhiteLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
















