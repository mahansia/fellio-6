@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.fellio.app.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fellio.app.R

private fun vf(res: Int, w: FontWeight) =
    Font(res, weight = w, variationSettings = FontVariation.Settings(FontVariation.weight(w.weight)))

val Vazirmatn = FontFamily(
    vf(R.font.vazirmatn, FontWeight.Normal),
    vf(R.font.vazirmatn, FontWeight.Medium),
    vf(R.font.vazirmatn, FontWeight.SemiBold),
    vf(R.font.vazirmatn, FontWeight.Bold)
)

/** Logo / headline font (Latin only; falls back to Vazirmatn for Persian). */
val Orbitron = FontFamily(
    vf(R.font.orbitron, FontWeight.Normal),
    vf(R.font.orbitron, FontWeight.Bold)
)

fun didamTypography(): Typography {
    val d = Typography()
    fun TextStyle.f() = copy(fontFamily = Vazirmatn)
    return Typography(
        displayLarge = d.displayLarge.f(), displayMedium = d.displayMedium.f(), displaySmall = d.displaySmall.f(),
        headlineLarge = d.headlineLarge.f(), headlineMedium = d.headlineMedium.f(), headlineSmall = d.headlineSmall.f(),
        titleLarge = d.titleLarge.f().copy(fontWeight = FontWeight.Bold),
        titleMedium = d.titleMedium.f().copy(fontWeight = FontWeight.SemiBold),
        titleSmall = d.titleSmall.f(),
        bodyLarge = d.bodyLarge.f(), bodyMedium = d.bodyMedium.f(), bodySmall = d.bodySmall.f(),
        labelLarge = d.labelLarge.f(), labelMedium = d.labelMedium.f(), labelSmall = d.labelSmall.f()
    )
}
