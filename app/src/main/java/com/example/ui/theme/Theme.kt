package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = WhatsappTealLight,
    onPrimary = Color.Black,
    primaryContainer = UserBubbleDark,
    onPrimaryContainer = Color.White,
    secondary = OnlineGreen,
    background = WhatsappDarkBg,
    onBackground = Color(0xFFE9EDF0),
    surface = WhatsappDarkSurface,
    onSurface = Color(0xFFE9EDF0),
    surfaceVariant = DarkBorder,
    onSurfaceVariant = TextGray
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WhatsappTealPrimary,
    onPrimary = Color.White,
    primaryContainer = UserBubbleLight,
    onPrimaryContainer = Color.Black,
    secondary = OnlineGreen,
    background = Color(0xFFF0F2F5),
    onBackground = Color(0xFF111B21),
    surface = Color.White,
    onSurface = Color(0xFF111B21),
    surfaceVariant = LightBorder,
    onSurfaceVariant = TextGray

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+ (disable by default to guarantee our custom theme is shown!)
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val view = androidx.compose.ui.platform.LocalView.current
  if (!view.isInEditMode) {
    val window = (view.context as? android.app.Activity)?.window
    if (window != null) {
      window.statusBarColor = colorScheme.primary.toArgb()
      androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
