@file:OptIn(ExperimentalMaterial3Api::class)

package com.fellio.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fellio.app.data.SearchItem
import com.fellio.app.data.formatDate
import kotlinx.coroutines.launch

@Composable
fun PersonScreen(vm: PersonViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val s = LocalStrings.current
    val jalali = LocalJalali.current
    val st by vm.state.collectAsStateWithLifecycle()
    val existing by vm.existing.collectAsStateWithLifecycle()
    val filmo by vm.filmography.collectAsStateWithLifecycle()
    val filmCategory by vm.filmCategory.collectAsStateWithLifecycle()
    val filmSort by vm.filmSort.collectAsStateWithLifecycle()
    var quick by remember { mutableStateOf<SearchItem?>(null) }
    var bioExpanded by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { },
                navigationIcon = { IconButton(onClick = onBack) { Icon(AppIcons.Back, contentDescription = s.back) } }
            )
        }
    ) { pad ->
        val data = st.data
        when {
            st.loading -> Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            data == null -> Column(
                Modifier.fillMaxSize().padding(pad).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                Text(s.error(st.errorKind ?: "unknown"), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                OutlinedButton(onClick = { vm.load() }, modifier = Modifier.padding(top = 12.dp)) { Text(s.search) }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(104.dp),
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(104.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            if (data.profilePath != null) RemoteImage("https://image.tmdb.org/t/p/w342${data.profilePath}", Modifier.fillMaxSize())
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(data.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (data.knownFor.isNotBlank()) Text(data.knownFor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            if (data.birthday.isNotBlank()) Text(s.born(formatDate(data.birthday, jalali)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (data.placeOfBirth.isNotBlank()) Text(data.placeOfBirth, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item(key = "bio", span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(s.biography, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            data.bio.ifBlank { s.noBio }, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (bioExpanded) Int.MAX_VALUE else 7, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { bioExpanded = !bioExpanded }
                        )
                        if (data.bio.length > 250) androidx.compose.material3.TextButton(onClick = { bioExpanded = !bioExpanded }) {
                            Text(if (bioExpanded) (if (s.fa) "کمتر" else "Less") else (if (s.fa) "بیشتر" else "More"))
                        }
                    }
                }
                if (data.filmography.isNotEmpty()) {
                    item(key = "filmo_header", span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(s.filmography, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(filmCategory == "all", { vm.setFilmCategory("all") }, { Text(s.all) })
                                FilterChip(filmCategory == "movie", { vm.setFilmCategory("movie") }, { Text(s.movies) })
                                FilterChip(filmCategory == "tv", { vm.setFilmCategory("tv") }, { Text(s.seriesPlural) })
                                Box(Modifier.width(8.dp))
                                FilterChip(filmSort == "popularity", { vm.setFilmSort("popularity") }, { Text(s.sortPopular) })
                                FilterChip(filmSort == "year", { vm.setFilmSort("year") }, { Text(s.sortNewest) })
                                FilterChip(filmSort == "rating", { vm.setFilmSort("rating") }, { Text(s.sortRating) })
                            }
                        }
                    }
                    items(filmo, key = { it.id }) { item ->
                        Column(
                            Modifier.clip(RoundedCornerShape(14.dp)).clickable { onOpen(item.id) },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box {
                                Poster(item.posterPath, Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(14.dp)))
                                if (item.id in existing) {
                                    Box(
                                        Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) { Icon(AppIcons.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp)) }
                                }
                            }
                            Text(item.name, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (item.year.isNotBlank()) Text(item.year, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
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
