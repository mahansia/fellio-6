package com.fellio.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.fellio.app.data.Lang

val AccentChoices: List<Int> = listOf(
    0xFFE50914, 0xFF5B7CFA, 0xFF22A06B, 0xFF0EA5A4, 0xFFE8590C, 0xFFE5484D, 0xFF9B5DE5
).map { it.toInt() }

@Composable
fun DidamTheme(accent: Int, dark: Boolean, lang: Lang, content: @Composable () -> Unit) {
    val primary = Color(accent)
    val onPrimary = if (primary.luminance() > 0.5f) Color(0xFF0B0D12) else Color.White
    val scheme = if (dark) {
        val surface = Color(0xFF141414)
        darkColorScheme(
            primary = primary, onPrimary = onPrimary,
            secondary = primary, onSecondary = onPrimary,
            primaryContainer = primary.copy(alpha = 0.22f).compositeOver(surface),
            onPrimaryContainer = Color(0xFFE8EAF0),
            secondaryContainer = primary.copy(alpha = 0.22f).compositeOver(surface),
            onSecondaryContainer = Color(0xFFE8EAF0),
            background = Color(0xFF000000), onBackground = Color(0xFFE8EAF0),
            surface = surface, onSurface = Color(0xFFE8EAF0),
            surfaceVariant = Color(0xFF1F1F1F), onSurfaceVariant = Color(0xFF9AA0B0),
            outline = Color(0xFF2A2F3C), error = Color(0xFFFF6B6B)
        )
    } else {
        val surface = Color.White
        lightColorScheme(
            primary = primary, onPrimary = onPrimary,
            secondary = primary, onSecondary = onPrimary,
            primaryContainer = primary.copy(alpha = 0.16f).compositeOver(surface),
            onPrimaryContainer = Color(0xFF14171F),
            secondaryContainer = primary.copy(alpha = 0.16f).compositeOver(surface),
            onSecondaryContainer = Color(0xFF14171F),
            background = Color(0xFFF6F7FB), onBackground = Color(0xFF14171F),
            surface = surface, onSurface = Color(0xFF14171F),
            surfaceVariant = Color(0xFFE9ECF5), onSurfaceVariant = Color(0xFF5B6273),
            outline = Color(0xFFD5D9E6), error = Color(0xFFD92D3A)
        )
    }
    CompositionLocalProvider(
        LocalLayoutDirection provides if (lang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = didamTypography(),
            shapes = Shapes(
                small = RoundedCornerShape(14.dp),
                medium = RoundedCornerShape(20.dp),
                large = RoundedCornerShape(28.dp)
            ),
            content = content
        )
    }
}
