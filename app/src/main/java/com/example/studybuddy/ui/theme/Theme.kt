package com.example.studybuddy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.studybuddy.data.StudyBuddyViewModel

private val lightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline
)

private val darkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun StudyBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    typography: Typography = Typography,
    colorScheme: androidx.compose.material3.ColorScheme = if (darkTheme) darkColorScheme else lightColorScheme,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> darkColorScheme
//        else -> lightColorScheme
//    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

// Setting function
@Composable
fun DynamicStudyBuddyTheme(
    viewModel: StudyBuddyViewModel,
    content: @Composable () -> Unit
) {
    val darkTheme = viewModel.theme == "Dark"
    val highContrast = viewModel.contrast == "High"
    val fontScale = when (viewModel.fontSize) {
        "Small" -> 0.85f
        "Large" -> 1.25f
        else -> 1.0f
    }

    val scaledTypography = Typography(
        displayLarge = Typography().displayLarge.copy(
            fontSize = Typography().displayLarge.fontSize * fontScale
        ),
        headlineMedium = Typography().headlineMedium.copy(
            fontSize = Typography().headlineMedium.fontSize * fontScale
        ),
        titleMedium = Typography().titleMedium.copy(
            fontSize = Typography().titleMedium.fontSize * fontScale
        ),
        bodyLarge = Typography().bodyLarge.copy(
            fontSize = Typography().bodyLarge.fontSize * fontScale
        ),
        bodyMedium = Typography().bodyMedium.copy(
            fontSize = Typography().bodyMedium.fontSize * fontScale
        ),
        labelLarge = Typography().labelLarge.copy(
            fontSize = Typography().labelLarge.fontSize * fontScale
        )
    )

    val colorScheme = when {
        highContrast && darkTheme -> darkColorScheme.copy(
            primary = Color.White,
            onPrimary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color.Black,
            onSurface = Color.White
        )
        highContrast && !darkTheme -> lightColorScheme.copy(
            primary = Color.Black,
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black
        )
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    StudyBuddyTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
        typography = scaledTypography,
        colorScheme = colorScheme,
        content = content
    )
}
