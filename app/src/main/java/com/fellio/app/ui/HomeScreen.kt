@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.fellio.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import com.fellio.app.data.SearchItem
import com.fellio.app.data.isTv
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onDiscover: () -> Unit,
    onPlaylist: (Long) -> Unit
) {
    val s = LocalStrings.current
    val tabs by vm.tabs.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val category by vm.category.collectAsStateWithLifecycle()
    val grid by vm.grid.collectAsStateWithLifecycle()
    val chips by vm.chips.collectAsStateWithLifecycle()
    val suggestRows by vm.suggestRows.collectAsStateWithLifecycle()
    val banner by vm.banner.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val existing by vm.existing.collectAsStateWithLifecycle()
    val tabIndex by vm.tabIndex.collectAsStateWithLifecycle()
    val searching by vm.searching.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var showEditChips by remember { mutableStateOf(false) }
    var quick by remember { mutableStateOf<SearchItem?>(null) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tab = HomeTab.values()[tabIndex]
    val items = tabs[tab].orEmpty()
    val bg = MaterialTheme.colorScheme.background

    fun label(id: String, name: String): String = when (id) {
        "all" -> s.all; "movie" -> s.movies; "tv" -> s.seriesPlural; "anime" -> s.anime; else -> name
    }

    Scaffold(
        containerColor = bg,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bg),
                    title = {
                        if (searching) {
                            TextField(
                                value = query, onValueChange = vm::setQuery,
                                placeholder = { Text(s.searchMyList) }, singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        } else {
                            var shown by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { shown = true }
                            AnimatedVisibility(
                                visible = shown,
                                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 2 }
                            ) {
                                LightSweepTitle(s.appName)
                            }
                        }
                    },
                    actions = {
                        FilledTonalIconButton(onClick = {
                            if (searching) vm.setQuery("")
                            vm.setSearching(!searching)
                        }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Icon(if (searching) AppIcons.Close else AppIcons.Search, contentDescription = s.search) }
                        FilledTonalIconButton(onClick = { vm.toggleGrid() },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Icon(if (grid) AppIcons.Rows else AppIcons.Grid, contentDescription = if (grid) s.listView else s.gridView)
                        }
                    }
                )
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeTab.values().forEachIndexed { i, ht ->
                        val tLabel = when (ht) {
                            HomeTab.WATCHING -> s.tabWatching
                            HomeTab.WATCHLIST -> s.tabWatchlist
                            HomeTab.COMPLETED -> s.tabCompleted
                        }
                        FilterChip(selected = i == tabIndex, onClick = { vm.setTab(i) },
                            label = { Text("$tLabel ${tabs[ht].orEmpty().size}", maxLines = 1) })
                    }
                    chips.filter { !it.builtIn }.forEach { c ->
                        val selected = category == c.id
                        if (c.builtIn) {
                            FilterChip(selected = selected, onClick = { vm.setCategory(c.id) }, label = { Text(label(c.id, c.label)) })
                        } else {
                            val pid = c.id.removePrefix("pl:")
                            AssistChip(
                                onClick = { onPlaylist(pid.toLong()) },
                                label = { Text("${c.icon ?: ""} ${c.label}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = (c.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.22f)
                                )
                            )
                        }
                    }
                    IconButton(onClick = { showEditChips = true }, modifier = Modifier.size(32.dp)) {
                        Icon(AppIcons.Edit, contentDescription = s.customizeChips, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { showCreate = true }, modifier = Modifier.size(32.dp)) {
                        Icon(AppIcons.Add, contentDescription = s.newPlaylist, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(if (grid) 100.dp else 400.dp),
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (items.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        if (query.isNotBlank() || category != "all") s.noResults else when (tab) {
                            HomeTab.WATCHING -> s.emptyWatching
                            HomeTab.WATCHLIST -> s.emptyWatchlist
                            HomeTab.COMPLETED -> s.emptyCompleted
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                    )
                }
            }
            items(items, key = { it.title.id }) { item ->
                Box(Modifier.animateItem()) {
                    if (grid) PosterCard(item) { onOpen(item.title.id) }
                    else HomeCard(item, onClick = { onOpen(item.title.id) }, onQuick = { vm.quick(item) })
                }
            }
        }
    }

    if (showCreate) {
        PlaylistEditDialog(
            initial = null, onDismiss = { showCreate = false },
            onSave = { name, color, icon -> vm.createPlaylist(name, color, icon); showCreate = false }
        )
    }

    if (showEditChips) {
        EditChipsDialog(vm = vm, label = ::label, onClose = { showEditChips = false })
    }

    quick?.let { item ->
        QuickAddDialog(
            item = item, inList = item.id in existing,
            onAddWatching = {
                quick = null
                vm.addItem(item, false) { ok -> scope.launch { snack.showSnackbar(if (ok) s.addedSnack(item.name) else s.error("network")) } }
            },
            onAddLater = {
                quick = null
                vm.addItem(item, true) { ok -> scope.launch { snack.showSnackbar(if (ok) s.addedWatchSnack(item.name) else s.error("network")) } }
            },
            onOpen = { quick = null; onOpen(item.id) },
            onDismiss = { quick = null }
        )
    }
}

@Composable
internal fun EditChipsDialog(vm: HomeViewModel, label: (String, String) -> String, onClose: () -> Unit) {
    val s = LocalStrings.current
    val all by vm.allChips.collectAsStateWithLifecycle()
    val hidden by vm.hiddenChips.collectAsStateWithLifecycle()
    var order by remember(all) { mutableStateOf(all.map { it.id }) }
    val ordered = order.mapNotNull { id -> all.firstOrNull { it.id == id } }

    AlertDialog(
        onDismissRequest = { vm.setChipOrder(order); onClose() },
        title = { Text(s.customizeChips) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.chipsHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ordered.forEachIndexed { idx, c ->
                    val isHidden = c.id in hidden
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = !isHidden, onCheckedChange = { vm.setChipHidden(if (it) hidden - c.id else hidden + c.id) })
                        Text(label(c.id, c.label), Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        IconButton(onClick = {
                            if (idx > 0) order = order.toMutableList().apply { add(idx - 1, removeAt(idx)) }
                        }, enabled = idx > 0, modifier = Modifier.size(32.dp)) { Icon(AppIcons.Up, null, modifier = Modifier.size(16.dp)) }
                        IconButton(onClick = {
                            if (idx < order.lastIndex) order = order.toMutableList().apply { add(idx + 1, removeAt(idx)) }
                        }, enabled = idx < order.lastIndex, modifier = Modifier.size(32.dp)) { Icon(AppIcons.Down, null, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { vm.setChipOrder(order); onClose() }) { Text(s.done) } }
    )
}

@Composable
internal fun PosterCard(item: HomeItem, onClick: () -> Unit) {
    val t = item.title
    Column(Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box {
            Poster(t.posterPath, Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(14.dp)))
            val p = item.progress
            if (isUnreleased(t.year)) {
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)).padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) { Text(LocalStrings.current.unreleased, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1) }
            } else if (p != null && p.total > 0) ProgressBar(p.percent, Modifier.align(Alignment.BottomCenter).padding(8.dp))
            if (item.isCompleted) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) { Icon(AppIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp)) }
            }
        }
        Text(t.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun HomeCard(item: HomeItem, onClick: () -> Unit, onQuick: () -> Unit) {
    val s = LocalStrings.current
    val t = item.title
    val p = item.progress
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent), modifier = Modifier.gradientCard()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Poster(t.posterPath, Modifier.width(72.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(12.dp)))
            Column(Modifier.weight(1f).align(Alignment.CenterVertically), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(t.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(s.typeYear(t.isTv(), t.year), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (p != null) {
                    Text("${s.episodesCount(p.watched, p.total)}  •  ${p.percent}%", style = MaterialTheme.typography.bodyMedium)
                    ProgressBar(p.percent)
                    val next = p.next
                    when {
                        p.complete -> Text(s.seriesComplete, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        next != null -> Text(s.nextEpisode(next.season, next.episode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Text(
                        if (t.movieWatched) "✓ ${s.watched}" else s.notWatched,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (t.movieWatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            val canQuick = if (p != null) p.next != null else !t.movieWatched
            if (canQuick) {
                FilledTonalIconButton(onClick = onQuick, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(AppIcons.Check, contentDescription = if (p != null) s.markNext else s.markWatched)
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeBanner(items: List<SearchItem>, onOpen: (String) -> Unit) {
    val s = LocalStrings.current
    val pager = rememberPagerState { items.size }
    LaunchedEffect(items.size) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            pager.animateScrollToPage((pager.currentPage + 1) % items.size, animationSpec = tween(700))
        }
    }
    Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        HorizontalPager(pager, Modifier.fillMaxWidth().height(440.dp)) { i ->
            val it = items[i]
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).clickable { onOpen(it.id) }) {
                Poster(it.posterPath, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(s.trendingNow, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    Text(it.name, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2)
                    if (it.year.isNotBlank()) Text(it.year, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpen(it.id) }, shape = RoundedCornerShape(24.dp)) { Text(s.add) }
                        OutlinedButton(onClick = { onOpen(it.id) }, shape = RoundedCornerShape(24.dp)) { Text(s.details, color = Color.White) }
                    }
                }
            }
        }
        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(items.size) { i ->
                Box(Modifier.size(if (i == pager.currentPage) 8.dp else 5.dp).clip(CircleShape)
                    .background(if (i == pager.currentPage) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)))
            }
        }
    }
}


/** App title with a light beam that periodically sweeps across the letters. */
@Composable
internal fun LightSweepTitle(text: String) {
    val base = MaterialTheme.colorScheme.primary
    val tr = androidx.compose.animation.core.rememberInfiniteTransition(label = "sweep")
    val x by tr.animateFloat(
        initialValue = -150f, targetValue = 450f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.keyframes {
                durationMillis = 3200
                -150f at 0
                450f at 1400
                450f at 3200
            }
        ), label = "x"
    )
    val brush = Brush.linearGradient(
        colors = listOf(base, Color.White, base),
        start = androidx.compose.ui.geometry.Offset(x - 60f, 0f),
        end = androidx.compose.ui.geometry.Offset(x + 60f, 40f)
    )
    Text(text, fontFamily = Orbitron, fontWeight = FontWeight.Bold, fontSize = 26.sp,
        style = androidx.compose.ui.text.TextStyle(brush = brush))
}
