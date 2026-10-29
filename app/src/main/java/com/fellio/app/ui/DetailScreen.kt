@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.fellio.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fellio.app.data.EpisodeEntity
import com.fellio.app.data.Genres
import com.fellio.app.data.PlaylistEntity
import com.fellio.app.data.SearchItem
import com.fellio.app.data.SubtitleResult
import com.fellio.app.data.TitleEntity
import com.fellio.app.data.castMembers
import com.fellio.app.data.formatDate
import com.fellio.app.data.isTv
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    vm: DetailViewModel,
    onBack: () -> Unit,
    onPerson: (Int) -> Unit,
    onOpen: (String) -> Unit,
    onGenre: (Int) -> Unit = {}
) {
    val head by vm.title.collectAsStateWithLifecycle()
    val pc = rememberPosterColor(head?.posterPath)
    val animated by androidx.compose.animation.animateColorAsState(
        pc ?: MaterialTheme.colorScheme.primary, androidx.compose.animation.core.tween(600), label = "accent")
    val base = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = if (pc != null) base.copy(primary = animated, onPrimary = Color.Black, primaryContainer = animated.copy(alpha = 0.25f).compositeOver(base.surface)) else base,
        typography = MaterialTheme.typography, shapes = MaterialTheme.shapes) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            head?.posterPath?.let { p ->
                RemoteImage("https://image.tmdb.org/t/p/w342$p",
                    Modifier.fillMaxSize().graphicsLayer { alpha = 0.3f })
            }
            DetailScreenBody(vm, onBack, onPerson, onOpen, onGenre)
        }
    }
}

@Composable
private fun DetailScreenBody(
    vm: DetailViewModel,
    onBack: () -> Unit,
    onPerson: (Int) -> Unit,
    onOpen: (String) -> Unit,
    onGenre: (Int) -> Unit
) {
    val s = LocalStrings.current
    val jalali = LocalJalali.current
    val title by vm.title.collectAsStateWithLifecycle()
    val episodes by vm.episodes.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val similar by vm.similar.collectAsStateWithLifecycle()
    val trailerKey by vm.trailer.collectAsStateWithLifecycle()
    val trailerLoading by vm.trailerLoading.collectAsStateWithLifecycle()
    val trailerMissing by vm.trailerMissing.collectAsStateWithLifecycle()
    trailerKey?.let { k -> TrailerDialog(k) { vm.trailer.value = null } }
    if (trailerMissing) AlertDialog(
        onDismissRequest = { vm.trailerMissing.value = false },
        text = { Text(s.noTrailer) },
        confirmButton = { TextButton(onClick = { vm.trailerMissing.value = false }) { Text(s.close) } }
    )
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    val memberOf by vm.memberOf.collectAsStateWithLifecycle()
    val existing by vm.existing.collectAsStateWithLifecycle()
    val subs by vm.subs.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var showUpTo by remember { mutableStateOf(false) }
    var showPlaylists by remember { mutableStateOf(false) }
    var quick by remember { mutableStateOf<SearchItem?>(null) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val saveSubtitle = rememberSaveFileLauncher("*/*") { target ->
        vm.writeSubtitle(target) { ok ->
            scope.launch { snack.showSnackbar(if (ok) s.message("sub_saved") else s.message("backup_failed")) }
        }
    }
    LaunchedEffect(subs.readyName) { subs.readyName?.let { saveSubtitle(it) } }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(AppIcons.Back, contentDescription = s.back) }
                },
                actions = {
                    val t = title
                    if (t != null) {
                        IconButton(onClick = { showPlaylists = true }) {
                            Icon(AppIcons.PlaylistAdd, contentDescription = s.addToPlaylist)
                        }
                        IconButton(onClick = { vm.toggleWatchlist(t.inWatchlist) }) {
                            Icon(
                                if (t.inWatchlist) AppIcons.Bookmark else AppIcons.BookmarkOutline,
                                contentDescription = s.toggleWatchlist,
                                tint = if (t.inWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (t.isTv()) {
                            if (refreshing) {
                                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                                }
                            } else {
                                IconButton(onClick = {
                                    vm.refresh { n, err ->
                                        scope.launch { snack.showSnackbar(if (err != null) s.error(err) else s.refreshed(n)) }
                                    }
                                }) { Icon(AppIcons.Refresh, contentDescription = s.refreshEpisodes) }
                            }
                        }
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(AppIcons.Delete, contentDescription = s.delete)
                    }
                }
            )
        }
    ) { pad ->
        val t = title
        if (t == null) {
            val failed by vm.fetchError.collectAsStateWithLifecycle()
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                if (failed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.loadFailed)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { vm.load() }) { Text(s.retry) }
                    }
                } else CircularProgressIndicator()
            }
            return@Scaffold
        }
        val progress = if (t.isTv()) episodes.progress() else null
        val grouped = episodes.groupBy { it.season }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item(key = "hero") { Hero(t) }
            item(key = "actions") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { vm.toggleWatchlist(t.inWatchlist) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (t.inWatchlist) AppIcons.Bookmark else AppIcons.BookmarkOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.toggleWatchlist, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { showPlaylists = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(AppIcons.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.addToPlaylist, maxLines = 1)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { vm.playTrailer() },
                    enabled = !trailerLoading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) { Text(if (trailerLoading) "…" else "▶  " + s.trailer) }
            }
            item(key = "info") {
                InfoBlock(t, onGenre = onGenre, onPerson = onPerson, onSubtitles = { vm.openSubs(null, null) })
            }

            if (progress == null) {
                item(key = "movie") {
                    Card(
                        onClick = { vm.setMovieWatched(!t.movieWatched) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().gradientCard()
                    ) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = t.movieWatched, onCheckedChange = { vm.setMovieWatched(it) })
                            Text(s.watched, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else {
                item(key = "progress") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().gradientCard()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(s.episodesCount(progress.watched, progress.total), style = MaterialTheme.typography.titleMedium)
                                Text("${progress.percent}%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            ProgressBar(progress.percent)
                            val next = progress.next
                            when {
                                progress.complete -> Text(s.seriesComplete, color = MaterialTheme.colorScheme.primary)
                                next != null -> Text(s.continueLabel(next.season, next.episode), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(onClick = { showUpTo = true }, modifier = Modifier.fillMaxWidth(), enabled = episodes.isNotEmpty()) {
                                Text(s.markUpTo)
                            }
                        }
                    }
                }

                grouped.forEach { (season, list) ->
                    item(key = "season_$season") {
                        SeasonHeader(
                            season = season,
                            complete = list.all { it.watched },
                            onAll = { vm.setSeasonWatched(season, true) },
                            onNone = { vm.setSeasonWatched(season, false) },
                            onSubs = { vm.openSubs(season, null) }
                        )
                    }
                    items(list, key = { it.id }) { ep ->
                        EpisodeRow(
                            ep = ep,
                            titleRuntime = t.runtime,
                            jalali = jalali,
                            onChange = { vm.setEpisodeWatched(ep, it) },
                            onSubtitles = { vm.openSubs(ep.season, ep.episode) }
                        )
                    }
                }
            }

            if (similar.isNotEmpty()) {
                item(key = "similar") {
                    PosterRow(
                        title = s.similarTitles,
                        items = similar,
                        existing = existing,
                        onClick = { item -> onOpen(item.id) },
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
        }

        if (showUpTo) {
            UpToDialog(
                grouped = grouped,
                initial = progress?.next ?: episodes.lastOrNull(),
                onDismiss = { showUpTo = false },
                onConfirm = { sn, e ->
                    vm.markUpTo(sn, e)
                    showUpTo = false
                }
            )
        }
    }

    if (subs.open) {
        SubtitleDialog(
            title = title?.name ?: "",
            state = subs,
            onDownload = { vm.downloadSub(it) },
            onClose = { vm.closeSubs() }
        )
    }

    if (showPlaylists) {
        PlaylistPickerDialog(
            playlists = playlists,
            memberOf = memberOf,
            onToggle = { pid, isMember -> vm.togglePlaylist(pid, isMember) },
            onCreate = { n, c, i -> vm.createPlaylistAndAdd(n, c, i) },
            onClose = { showPlaylists = false }
        )
    }

    quick?.let { item ->
        QuickAddDialog(
            item = item,
            inList = item.id in existing,
            onAddWatching = {
                quick = null
                vm.addItem(item, false) { ok ->
                    scope.launch { snack.showSnackbar(if (ok) s.addedSnack(item.name) else s.error("network")) }
                }
            },
            onAddLater = {
                quick = null
                vm.addItem(item, true) { ok ->
                    scope.launch { snack.showSnackbar(if (ok) s.addedWatchSnack(item.name) else s.error("network")) }
                }
            },
            onOpen = { quick = null; onOpen(item.id) },
            onDismiss = { quick = null }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(s.delete) },
            text = { Text(s.deleteMsg(title?.name ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.delete()
                    onBack()
                }) { Text(s.delete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(s.cancel) } }
        )
    }
}

@Composable
private fun Hero(t: TitleEntity) {
    val s = LocalStrings.current
    val bg = MaterialTheme.colorScheme.background
    Box(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(190.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (t.backdropPath.isNotBlank()) {
                RemoteImage("https://image.tmdb.org/t/p/w780${t.backdropPath}", Modifier.fillMaxSize())
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, bg))))
        }
        Row(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Poster(t.posterPath, Modifier.width(110.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(16.dp)))
            Column(Modifier.weight(1f).padding(top = 84.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(t.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    s.typeYear(t.isTv(), t.year),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoBlock(t: TitleEntity, onGenre: (Int) -> Unit, onPerson: (Int) -> Unit, onSubtitles: () -> Unit) {
    val s = LocalStrings.current
    var expanded by remember { mutableStateOf(false) }
    val genres = t.genreIds.split(",").mapNotNull { it.toIntOrNull() }.mapNotNull { g -> Genres.label(g, s.lang)?.let { g to it } }
    val cast = t.castMembers().distinctBy { it.personId }
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isUnreleased(t.year)) Pill(s.unreleased, true)
            if (t.rating > 0) Pill("★ " + String.format(java.util.Locale.US, "%.1f", t.rating))
            if (t.runtime > 0) Pill(s.minutes(t.runtime))
            genres.forEach { (g, n) -> Box(Modifier.clip(RoundedCornerShape(50)).clickable { onGenre(g) }) { Pill(n) } }
        }
        if (t.overview.isNotBlank()) {
            Text(
                t.overview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
        if (cast.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.cast, style = MaterialTheme.typography.labelLarge)
                CastRow(cast, onPerson)
            }
        } else if (t.castNames.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.cast, style = MaterialTheme.typography.labelLarge)
                Text(t.castNames, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!t.isTv()) {
            OutlinedButton(onClick = onSubtitles) {
                Icon(AppIcons.Subtitles, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.downloadSubtitle)
            }
        }
    }
}

@Composable
private fun SeasonHeader(season: Int, complete: Boolean, onAll: () -> Unit, onNone: () -> Unit, onSubs: () -> Unit) {
    val s = LocalStrings.current
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(s.season(season), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (complete) {
                Text(s.seasonComplete(season), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Box {
            IconButton(onClick = { menu = true }) { Icon(AppIcons.MoreVert, contentDescription = s.seasonOptions) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text(s.markAllSeason) }, onClick = { menu = false; onAll() })
                DropdownMenuItem(text = { Text(s.unmarkAllSeason) }, onClick = { menu = false; onNone() })
                DropdownMenuItem(text = { Text(s.seasonSubs) }, onClick = { menu = false; onSubs() })
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    ep: EpisodeEntity,
    titleRuntime: Int,
    jalali: Boolean,
    onChange: (Boolean) -> Unit,
    onSubtitles: () -> Unit
) {
    val s = LocalStrings.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onChange(!ep.watched) }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = ep.watched, onCheckedChange = onChange)
        Column(Modifier.weight(1f)) {
            Text(
                s.episode(ep.episode) + if (ep.name.isNotBlank()) " — ${ep.name}" else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val runtime = if (ep.runtime > 0) ep.runtime else titleRuntime
            val meta = listOfNotNull(
                ep.airDate.takeIf { it.isNotBlank() }?.let { formatDate(it, jalali) },
                runtime.takeIf { it > 0 }?.let { s.minutes(it) }
            ).joinToString("  •  ")
            if (meta.isNotEmpty()) {
                Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onSubtitles) {
            Icon(AppIcons.Subtitles, contentDescription = s.downloadSubtitle, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SubtitleDialog(
    title: String,
    state: SubsState,
    onDownload: (SubtitleResult) -> Unit,
    onClose: () -> Unit
) {
    val s = LocalStrings.current
    val target = if (state.season != null && state.episode != null) " S${state.season}E${state.episode}" else ""
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(s.downloadSubtitle, maxLines = 1) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title + target, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (state.errorKind != null) {
                    Text(s.error(state.errorKind), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Box(Modifier.fillMaxWidth().heightIn(max = 340.dp)) {
                    when {
                        state.loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        state.results.isEmpty() && state.errorKind == null -> Text(
                            s.noSubtitles,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(state.results, key = { it.url }) { r ->
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                        .clickable { onDownload(r) }.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Pill(r.language)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            r.release.ifBlank { "—" },
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (r.hearingImpaired) {
                                            Text(s.hearingImpaired, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (state.downloadingUrl == r.url) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(s.close) } }
    )
}

@Composable
private fun PlaylistPickerDialog(
    playlists: List<PlaylistEntity>,
    memberOf: Set<Long>,
    onToggle: (Long, Boolean) -> Unit,
    onCreate: (String, Int, String) -> Unit,
    onClose: () -> Unit
) {
    val s = LocalStrings.current
    var creating by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(s.addToPlaylist) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                if (playlists.isEmpty()) {
                    Text(s.noPlaylistsYet, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                }
                playlists.forEach { p ->
                    val member = p.id in memberOf
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onToggle(p.id, member) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = member, onCheckedChange = { onToggle(p.id, member) })
                        Text("${p.icon} ${p.name}")
                    }
                }
                TextButton(onClick = { creating = true }) {
                    Icon(AppIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.newPlaylist)
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(s.close) } }
    )
    if (creating) {
        PlaylistEditDialog(
            initial = null,
            onDismiss = { creating = false },
            onSave = { n, c, i -> onCreate(n, c, i); creating = false }
        )
    }
}

@Composable
private fun UpToDialog(
    grouped: Map<Int, List<EpisodeEntity>>,
    initial: EpisodeEntity?,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val s = LocalStrings.current
    val seasons = grouped.keys.sorted()
    var season by remember { mutableIntStateOf(initial?.season ?: seasons.first()) }
    val epNumbers = grouped[season].orEmpty().map { it.episode }
    var ep by remember { mutableIntStateOf(initial?.episode ?: epNumbers.firstOrNull() ?: 1) }
    if (epNumbers.isNotEmpty() && ep !in epNumbers) ep = epNumbers.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.upToTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.upToHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Picker(s.season(season), seasons.map { it to s.season(it) }) { season = it }
                    Picker(s.episode(ep), epNumbers.map { it to s.episode(it) }) { ep = it }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(season, ep) }) { Text(s.confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

@Composable
private fun Picker(label: String, options: List<Pair<Int, String>>, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text(label) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(value); open = false })
            }
        }
    }
}

@Composable
private fun TrailerDialog(key: String, onClose: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(key) {
        val web = android.content.Intent(android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://www.youtube.com/watch?v=$key"))
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { ctx.startActivity(web) }
        onClose()
    }
}
