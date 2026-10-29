package com.fellio.app.ui

import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.collectIsPressedAsState

/** Platform image loader (Coil on Android). */
@Composable
fun RemoteImage(url: String?, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    if (url != null) AsyncImage(model = url, contentDescription = null, contentScale = contentScale, modifier = modifier)
}

@Composable
fun Poster(path: String?, modifier: Modifier = Modifier) {
    Box(modifier.shimmer()) {
        if (path != null) RemoteImage("https://image.tmdb.org/t/p/w342$path", Modifier.fillMaxSize())
    }
}

/** Skeleton loading: gently pulsing placeholder background. */
@Composable
fun Modifier.shimmer(): Modifier {
    val tr = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val a by tr.animateFloat(
        0.35f, 0.8f,
        androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(900), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "a"
    )
    return this.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = a))
}

/** Press effect: shrinks slightly while pressed. */
@Composable
fun Modifier.pressScale(src: androidx.compose.foundation.interaction.MutableInteractionSource): Modifier {
    val pressed by src.collectIsPressedAsState()
    val sc by androidx.compose.animation.core.animateFloatAsState(if (pressed) 0.95f else 1f, androidx.compose.animation.core.tween(120), label = "press")
    return this.scale(sc)
}

/** Soft gradient card background (surface -> slightly lighter/tinted toward accent). */
@Composable
fun Modifier.gradientCard(): Modifier {
    val sf = MaterialTheme.colorScheme.surface
    val pr = MaterialTheme.colorScheme.primary
    return this.clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp)).background(
        androidx.compose.ui.graphics.Brush.linearGradient(listOf(sf, androidx.compose.ui.graphics.lerp(sf, pr, 0.12f)))
    )
}


/** Vivid accent color derived from a poster; null until loaded. */
@Composable
fun rememberPosterColor(path: String?): androidx.compose.ui.graphics.Color? {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var color by androidx.compose.runtime.remember(path) { androidx.compose.runtime.mutableStateOf<androidx.compose.ui.graphics.Color?>(null) }
    androidx.compose.runtime.LaunchedEffect(path) {
        if (path == null) return@LaunchedEffect
        val req = coil.request.ImageRequest.Builder(ctx)
            .data("https://image.tmdb.org/t/p/w92$path").allowHardware(false).build()
        val res = coil.Coil.imageLoader(ctx).execute(req)
        val bmp = (res.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap ?: return@LaunchedEffect
        val px = android.graphics.Bitmap.createScaledBitmap(bmp, 1, 1, true).getPixel(0, 0)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(px, hsv)
        hsv[1] = hsv[1].coerceIn(0.55f, 0.9f)
        hsv[2] = hsv[2].coerceIn(0.8f, 1f)
        color = androidx.compose.ui.graphics.Color(android.graphics.Color.HSVToColor(hsv))
    }
    return color
}
