@file:OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)

package com.fellio.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fellio.app.data.CastMember
import com.fellio.app.data.PlaylistEntity
import com.fellio.app.data.SearchItem

@Composable
fun ProgressBar(percent: Int, modifier: Modifier = Modifier) {
    val f by animateFloatAsState(percent / 100f, tween(400), label = "progress")
    LinearProgressIndicator(
        progress = { f },
        modifier = modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
        trackColor = MaterialTheme.colorScheme.outline
    )
}

@Composable
fun Pill(text: String, tonal: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (tonal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (tonal) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Collapsible card section used throughout Settings. */
@Composable
fun ExpandableSection(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    initiallyOpen: Boolean = false,
    content: @Composable () -> Unit
) {
    var open by remember { mutableStateOf(initiallyOpen) }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent), modifier = Modifier.gradientCard()
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().clickable { open = !open }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }
                if (icon != null) Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    if (open) AppIcons.Up else AppIcons.Down,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = open, enter = expandVertically(), exit = shrinkVertically()) {
                Column(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) { content() }
            }
        }
    }
}

/** Horizontal row of posters (suggestions / similar titles / discover). */
@Composable
fun PosterRow(
    title: String,
    subtitle: String? = null,
    items: List<SearchItem>,
    existing: Map<String, Boolean>,
    onClick: (SearchItem) -> Unit,
    onMore: (() -> Unit)? = null,
    moreLabel: String = "",
    onLongClick: ((SearchItem) -> Unit)? = null,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (onMore != null) {
                TextButton(onClick = onMore) { Text(moreLabel) }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val isrc = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Column(
                    Modifier.width(120.dp).pressScale(isrc).clip(RoundedCornerShape(12.dp)).combinedClickable(
                        interactionSource = isrc, indication = null,
                        onClick = { onClick(item) },
                        onLongClick = { onLongClick?.invoke(item) }
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        Poster(item.posterPath, Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(12.dp)))
                        if (isUnreleased(item.year)) {
                            Box(
                                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f))
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(LocalStrings.current.unreleased, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1)
                            }
                        }
                        Box(Modifier.matchParentSize().clip(RoundedCornerShape(12.dp)).background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)))))
                        if (item.id in existing) {
                            Box(
                                Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(AppIcons.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Text(item.name, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun CastRow(cast: List<CastMember>, onClick: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cast, key = { it.personId }) { c ->
            Column(
                Modifier.width(76.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick(c.personId) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    if (c.profilePath != null) {
                        RemoteImage("https://image.tmdb.org/t/p/w185${c.profilePath}", Modifier.fillMaxSize())
                    }
                }
                Text(c.name, style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Shown when a suggested / similar / filmography title is tapped: two clear icon actions. */
@Composable
fun QuickAddDialog(
    item: SearchItem,
    inList: Boolean,
    onAddWatching: () -> Unit,
    onAddLater: () -> Unit,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Poster(item.posterPath, Modifier.width(80.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(12.dp)))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            s.typeYear(item.mediaType == "tv", item.year),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.rating > 0) {
                            Text("★ " + String.format(java.util.Locale.US, "%.1f", item.rating), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (item.overview.isNotBlank()) {
                    Text(
                        item.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (inList) {
                    Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text(s.openLabel) }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        IconTextButton(AppIcons.EyeFilled, s.addToWatching, Modifier.weight(1f), onAddWatching)
                        IconTextButton(AppIcons.ClockFilled, s.watchLater, Modifier.weight(1f), onAddLater)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.close) } }
    )
}

@Composable
private fun IconTextButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Box(Modifier.width(6.dp))
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

val PlaylistIcons = listOf("🎬", "📺", "⭐", "🔥", "🍿", "❤️", "🎯", "📌", "🌙", "😂", "😱", "🧠")

/** Create (initial == null) or edit a custom playlist: name, icon, color. */
@Composable
fun PlaylistEditDialog(initial: PlaylistEntity?, onDismiss: () -> Unit, onSave: (String, Int, String) -> Unit) {
    val s = LocalStrings.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableIntStateOf(initial?.color ?: AccentChoices.first()) }
    var icon by remember { mutableStateOf(initial?.icon ?: PlaylistIcons.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) s.newPlaylist else s.editPlaylist) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text(s.playlistName) }, modifier = Modifier.fillMaxWidth()
                )
                Text(s.chooseIcon, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlaylistIcons.forEach { e ->
                        Box(
                            Modifier.size(40.dp).clip(CircleShape)
                                .background(if (e == icon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { icon = e },
                            contentAlignment = Alignment.Center
                        ) { Text(e) }
                    }
                }
                Text(s.chooseColor, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccentChoices.forEach { c ->
                        Box(
                            Modifier.size(32.dp).clip(CircleShape).background(Color(c))
                                .border(
                                    width = if (c == color) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = CircleShape
                                )
                                .clickable { color = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), color, icon) }, enabled = name.isNotBlank()) {
                Text(if (initial == null) s.create else s.save)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

/** Simple vector icons shared by the phone and desktop apps (no icon library needed). */
object AppIcons {
    private fun stroke(name: String, d: String, width: Float = 2f, mirror: Boolean = false): ImageVector =
        ImageVector.Builder(
            name = name, defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f, autoMirror = mirror
        ).addPath(
            pathData = addPathNodes(d), stroke = SolidColor(Color.Black),
            strokeLineWidth = width, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
        ).build()

    private fun filled(name: String, d: String): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(pathData = addPathNodes(d), fill = SolidColor(Color.Black), strokeLineJoin = StrokeJoin.Round).build()

    private const val SEARCH = "M10.5 4a6.5 6.5 0 1 0 0 13a6.5 6.5 0 1 0 0-13 M15.5 15.5L20 20"

    val Add: ImageVector by lazy { stroke("add", "M12 5.5V18.5 M5.5 12H18.5", width = 2.3f) }
    val Close: ImageVector by lazy { stroke("close", "M6.5 6.5L17.5 17.5 M17.5 6.5L6.5 17.5", width = 2.2f) }
    val Check: ImageVector by lazy { stroke("check", "M5 12.5L9.5 17L19 7.5", width = 2.5f) }
    val Search: ImageVector by lazy { stroke("search", SEARCH, width = 2.1f) }
    val Settings: ImageVector by lazy {
        stroke("settings", "M4 7H9 M13 7H20 M4 17H11 M15 17H20 M9 4.5V9.5 M13 14.5V19.5", width = 2.1f)
    }
    val Delete: ImageVector by lazy { stroke("delete", "M4 7h16 M9.5 7V4.8h5V7 M6.5 7l1 12.2h9l1-12.2 M10 11v5 M14 11v5") }
    val Refresh: ImageVector by lazy { stroke("refresh", "M19.5 12a7.5 7.5 0 1 1-2.2-5.3 M19.5 4.5v4.5h-4.5", width = 2.1f) }
    val Back: ImageVector by lazy { stroke("back", "M19 12H5.5 M11 6l-6 6 6 6", width = 2.2f, mirror = true) }
    val MoreVert: ImageVector by lazy { stroke("more", "M12 5v.01 M12 12v.01 M12 19v.01", width = 3.2f) }
    val Edit: ImageVector by lazy { stroke("edit", "M4.5 19.5l.9-4 10.6-10.6a1.8 1.8 0 0 1 2.6 0l.5.5a1.8 1.8 0 0 1 0 2.6L8.5 18.6z M14 6.5l3.5 3.5") }
    val PlaylistAdd: ImageVector by lazy { stroke("playlist_add", "M4 7h10 M4 12h10 M4 17h6 M18 11v8 M14 15h8") }
    val Subtitles: ImageVector by lazy { stroke("subtitles", "M3.5 6.5h17v11h-17z M7 11h4 M13 11h4 M7 14.5h6 M14.5 14.5h2.5") }
    val BookmarkOutline: ImageVector by lazy { stroke("bookmark_outline", "M6.5 3.5h11v17.5l-5.5-4-5.5 4z") }
    val Grid: ImageVector by lazy { filled("grid", "M4 4h7v7H4z M13 4h7v7h-7z M4 13h7v7H4z M13 13h7v7h-7z") }
    val Rows: ImageVector by lazy { filled("rows", "M3.5 4.5h17v4h-17z M3.5 10h17v4h-17z M3.5 15.5h17v4h-17z") }
    val Chart: ImageVector by lazy { filled("chart", "M4 12h4.5v8H4z M9.75 5h4.5v15h-4.5z M15.5 9h4.5v11h-4.5z") }
    val Bookmark: ImageVector by lazy { filled("bookmark", "M6.5 3.5h11v17.5l-5.5-4-5.5 4z") }
    val Up: ImageVector by lazy { filled("up", "M12 8l8 9H4z") }
    val Down: ImageVector by lazy { filled("down", "M12 16L4 7h16z") }
    val NavHome: ImageVector by lazy { stroke("nav_home", "M3.5 10.5L12 3.5l8.5 7 M5.5 9v10.5h4.5v-6h4v6h4.5V9", width = 2f) }
    val NavSearch: ImageVector by lazy { stroke("nav_search", SEARCH, width = 2.2f) }
    val NavLists: ImageVector by lazy { stroke("nav_lists", "M9 6.5h11 M9 12h11 M9 17.5h11 M4.5 6.5h.01 M4.5 12h.01 M4.5 17.5h.01", width = 2.6f) }
    val NavDiscover: ImageVector by lazy { stroke("nav_discover", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18 M15.5 8.5l-2 5-5 2 2-5z", width = 1.9f) }
    val NavSettings: ImageVector by lazy { stroke("nav_settings", "M4 7h9 M17 7h3 M4 17h3 M11 17h9 M13 4.5v5 M7 14.5v5", width = 2.1f) }
    val Compass: ImageVector by lazy { stroke("compass", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18 M15.5 8.5l-2 5-5 2 2-5z", width = 1.9f) }
    val Palette: ImageVector by lazy { stroke("palette", "M12 3a9 9 0 1 0 0 18c1.5 0 2-1 1.5-2s.5-2 2-2H17a4 4 0 0 0 4-4c0-5-4-10-9-10z M7.5 11v.01 M10 7.5v.01 M14.5 7.5v.01") }
    val Bell: ImageVector by lazy { stroke("bell", "M6 17V11a6 6 0 0 1 12 0v6l2 2H4z M10 21h4") }
    val Key: ImageVector by lazy { stroke("key", "M8 9a4 4 0 1 0 0 8a4 4 0 1 0 0-8z M11 13h10 M18 13v4") }
    val Database: ImageVector by lazy { stroke("database", "M4 6c0-2 16-2 16 0s-16 2-16 0z M4 6v12c0 2 16 2 16 0V6 M4 12c0 2 16 2 16 0") }
    /** Watching = eye. */
    val EyeOutline: ImageVector by lazy { stroke("eye_outline", "M2 12c2.5-5.5 7-8 10-8s7.5 2.5 10 8c-2.5 5.5-7 8-10 8S4.5 17.5 2 12z M12 15.5a3.5 3.5 0 1 0 0-7a3.5 3.5 0 1 0 0 7z") }
    val EyeFilled: ImageVector by lazy { filled("eye_filled", "M12 4C9 4 4.5 6.5 2 12c2.5 5.5 7 8 10 8s7.5-2.5 10-8C19.5 6.5 15 4 12 4z M12 15.5a3.5 3.5 0 1 1 0-7a3.5 3.5 0 1 1 0 7z") }
    /** Watch later = clock. */
    val ClockOutline: ImageVector by lazy { stroke("clock_outline", "M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0 -18 0 M12 7v5l3.5 2") }
    val ClockFilled: ImageVector by lazy {
        ImageVector.Builder("clock_filled", 24.dp, 24.dp, 24f, 24f)
            .addPath(pathData = addPathNodes("M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0 -18 0"), fill = SolidColor(Color.Black))
            .addPath(
                pathData = addPathNodes("M12 7v5l3.5 2"), fill = null, stroke = SolidColor(Color.White),
                strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
            ).build()
    }
}

/** True when the release year is missing or in the future. */
fun isUnreleased(year: String): Boolean {
    val y = year.take(4).toIntOrNull() ?: return true
    return y > java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
}
