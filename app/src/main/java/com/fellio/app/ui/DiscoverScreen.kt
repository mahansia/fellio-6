@file:OptIn(ExperimentalMaterial3Api::class)

package com.fellio.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fellio.app.data.Genres
import com.fellio.app.data.SearchItem
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(vm: DiscoverViewModel, home: HomeViewModel, onBack: () -> Unit, onOpen: (String) -> Unit, onSearch: () -> Unit = {}) {
    val banner by home.banner.collectAsStateWithLifecycle()
    val history by home.history.collectAsStateWithLifecycle()
    val s = LocalStrings.current
    val rows by vm.rows.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val existing by vm.existing.collectAsStateWithLifecycle()
    var quick by remember { mutableStateOf<SearchItem?>(null) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val interestsSet by vm.settings.interestsSet.collectAsStateWithLifecycle()
    var showInterests by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(interestsSet) { if (!interestsSet) showInterests = true }
    if (showInterests) InterestsDialog(vm.settings.prefTypes.value, vm.settings.favGenres.value, onDismiss = {
        showInterests = false
        if (!interestsSet) vm.saveInterests(vm.settings.prefTypes.value, vm.settings.favGenres.value)
    }) { ty, ge -> showInterests = false; vm.saveInterests(ty, ge) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { LightSweepTitle(s.appName) },
                navigationIcon = {
                    IconButton(onClick = onSearch) { Icon(AppIcons.Search, contentDescription = s.search) }
                },
                actions = {
                    IconButton(onClick = { showInterests = true }) { Icon(AppIcons.Edit, contentDescription = s.interests) }
                    IconButton(onClick = { vm.reload() }) { Icon(AppIcons.Refresh, contentDescription = s.reload) }
                }
            )
        }
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (banner.isNotEmpty()) item(key = "banner") {
                Box(Modifier.padding(horizontal = 16.dp).animateItem()) { HomeBanner(banner, onOpen) }
            }
            if (history.isNotEmpty()) item(key = "history") {
                PosterRow(title = s.recent, items = history, existing = existing, onClick = { onOpen(it.id) }, modifier = Modifier.animateItem())
            }
            if (loading) item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (rows.isEmpty()) item(key = "empty") {
                Text(s.discoverEmpty, Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(rows, key = { it.kind + it.refId + it.refName }) { row ->
                val title = when (row.kind) {
                    "similar" -> s.becauseWatched(row.refName)
                    "genre" -> s.becauseGenre(Genres.label(row.refId, s.lang) ?: "")
                    "list" -> s.rowTitles[row.refName] ?: row.refName
                    "top" -> s.topRated
                    "anime" -> s.animePicks
                    else -> s.trendingWeek
                }
                Box(Modifier.animateItem()) {
                    DismissablePosterRow(
                        title = title, items = row.items, existing = existing,
                        onClick = { item -> onOpen(item.id) },
                        onDismiss = { vm.dismiss(it) }
                    )
                }
            }
        }
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

/** Same as PosterRow but each poster can be long-pressed to show a "not interested" action. */
@Composable
private fun DismissablePosterRow(
    title: String, items: List<SearchItem>, existing: Map<String, Boolean>,
    onClick: (SearchItem) -> Unit, onDismiss: (String) -> Unit
) {
    val s = LocalStrings.current
    var toDismiss by remember { mutableStateOf<SearchItem?>(null) }
    PosterRow(title = title, items = items, existing = existing, onClick = onClick, onLongClick = { toDismiss = it })
    toDismiss?.let { item ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { toDismiss = null },
            title = { Text(item.name, maxLines = 1) },
            text = { Text(s.notInterested) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { onDismiss(item.id); toDismiss = null }) { Text(s.confirm) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { toDismiss = null }) { Text(s.cancel) } }
        )
    }
}


@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun InterestsDialog(
    types: Map<String, Int>, genres: Set<Int>, onDismiss: () -> Unit,
    onSave: (Map<String, Int>, Set<Int>) -> Unit
) {
    val s = LocalStrings.current
    var ty by remember { mutableStateOf(types) }
    var ge by remember { mutableStateOf(genres) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.interests) },
        text = {
            Column(Modifier.androidx_scroll(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.interestsHint, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf("anime" to s.kindAnime, "tv" to s.kindTv, "movie" to s.kindMovie).forEach { (k, label) ->
                    Text(label, fontWeight = FontWeight.SemiBold)
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0 to s.impOff, 1 to s.impNormal, 2 to s.impHigh).forEach { (v, l) ->
                            androidx.compose.material3.FilterChip(
                                selected = (ty[k] ?: 1) == v, onClick = { ty = ty + (k to v) }, label = { Text(l) }
                            )
                        }
                    }
                }
                Text(s.genresLabel, fontWeight = FontWeight.SemiBold)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Genres.names.keys.forEach { g ->
                        androidx.compose.material3.FilterChip(
                            selected = g in ge, onClick = { ge = if (g in ge) ge - g else ge + g },
                            label = { Text(Genres.label(g, s.lang) ?: "") }
                        )
                    }
                }
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = { onSave(ty, ge) }) { Text(s.confirm) } },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

@Composable
private fun Modifier.androidx_scroll(): Modifier =
    this.then(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()))
