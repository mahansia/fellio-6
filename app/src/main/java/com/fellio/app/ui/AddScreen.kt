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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.fellio.app.data.Genres
import com.fellio.app.data.SearchItem
import kotlinx.coroutines.launch

@Composable
fun AddScreen(vm: AddViewModel, onBack: () -> Unit, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    val s = LocalStrings.current
    val st by vm.state.collectAsStateWithLifecycle()
    val existing by vm.existing.collectAsStateWithLifecycle()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var genrePicker by remember { mutableStateOf(false) }
    var langPicker by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun addItem(item: SearchItem, watchlist: Boolean) {
        vm.add(item, watchlist) { ok ->
            if (ok) scope.launch { snack.showSnackbar(if (watchlist) s.addedWatchSnack(item.name) else s.addedSnack(item.name)) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text(s.addTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(AppIcons.Back, contentDescription = s.back) } }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = st.query, onValueChange = vm::onQuery, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(s.searchHint) }, singleLine = true, shape = RoundedCornerShape(28.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { vm.search() }),
                    leadingIcon = { Icon(AppIcons.Search, contentDescription = s.search) },
                    trailingIcon = {
                        if (st.query.isNotEmpty()) IconButton(onClick = { vm.onQuery(""); vm.search() }) {
                            Icon(AppIcons.Close, contentDescription = null)
                        }
                    }
                )
            }
            item(key = "category") {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = st.filterCount > 0, onClick = { showFilters = true },
                        shape = RoundedCornerShape(20.dp),
                        label = { Text(if (st.filterCount > 0) "${s.filters} (${st.filterCount})" else s.filters) }
                    )
                    FilterChip(st.category == "all", { vm.setCategory("all") }, { Text(s.all) }, shape = RoundedCornerShape(20.dp))
                    FilterChip(st.category == "movie", { vm.setCategory("movie") }, { Text(s.movie) }, shape = RoundedCornerShape(20.dp))
                    FilterChip(st.category == "tv", { vm.setCategory("tv") }, { Text(s.series) }, shape = RoundedCornerShape(20.dp))
                    FilterChip(st.category == "anime", { vm.setCategory("anime") }, { Text(s.anime) }, shape = RoundedCornerShape(20.dp))
                    if (st.filterCount > 0) TextButton(onClick = { vm.clearFilters() }) { Text(s.clearFilters) }
                }
            }
            if (st.errorKind != null) {
                item(key = "error") {
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.error(st.errorKind!!), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        if (st.errorKind == "no_key" || st.errorKind == "bad_key") OutlinedButton(onClick = onSettings) { Text(s.goSettings) }
                    }
                }
            }
            if (st.loading) {
                item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
            } else {
                if (st.results.isNotEmpty()) {
                    item(key = "header") {
                        val isTrending = st.activeQuery.isBlank() && st.filterCount == 0
                        Text(
                            if (isTrending) s.trending else s.results,
                            style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(st.results, key = { it.id }) { item ->
                    ResultRow(
                        item = item, inList = item.id in existing, adding = st.addingId == item.id,
                        onOpen = { onOpen(item.id) }
                    )
                }
                if (st.searched && st.results.isEmpty() && st.errorKind == null) {
                    item(key = "empty") {
                        Text(s.noResults, Modifier.fillMaxWidth().padding(top = 24.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (st.hasMore) {
                    item(key = "more") {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (st.loadingMore) CircularProgressIndicator(Modifier.size(28.dp))
                            else OutlinedButton(onClick = { vm.loadMore() }) { Text(s.loadMore) }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilters = false }, sheetState = sheet,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
                Text(s.filters, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                FiltersCard(st, vm, onGenre = { genrePicker = true }, onLang = { langPicker = true })
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (st.filterCount > 0) OutlinedButton(onClick = { vm.clearFilters() }) { Text(s.clearFilters) }
                    Button(
                        onClick = { showFilters = false; vm.search() },
                        modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(26.dp)
                    ) { Text(s.search, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (genrePicker) {
        SearchablePickerDialog(
            title = s.selectGenres, searchHint = s.searchGenres,
            options = Genres.options.map { it.key to it.name.of(s.lang) },
            selected = st.genreKeys, multi = true,
            onToggle = { vm.toggleGenre(it) }, onClose = { genrePicker = false }
        )
    }
    if (langPicker) {
        SearchablePickerDialog(
            title = s.origLanguage, searchHint = s.searchLanguages,
            options = Genres.languages.map { it.first to it.second.of(s.lang) },
            selected = if (st.origLang.isBlank()) emptySet() else setOf(st.origLang), multi = false,
            onToggle = { vm.setOrigLang(it) }, onClose = { langPicker = false }
        )
    }
}

@Composable
private fun FiltersCard(st: AddState, vm: AddViewModel, onGenre: () -> Unit, onLang: () -> Unit) {
    val s = LocalStrings.current
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(s.sortLabel, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(st.sort == "relevance", { vm.setSort("relevance") }, { Text(s.sortRelevance) })
            FilterChip(st.sort == "popularity", { vm.setSort("popularity") }, { Text(s.sortPopular) })
            FilterChip(st.sort == "rating", { vm.setSort("rating") }, { Text(s.sortRating) })
            FilterChip(st.sort == "newest", { vm.setSort("newest") }, { Text(s.sortNewest) })
        }
        Text(s.yearLabel, style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = st.year, onValueChange = vm::setYear, modifier = Modifier.width(160.dp), singleLine = true,
            placeholder = { Text(s.yearHint) }, shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(s.minRating, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(st.minRating == 0, { vm.setMinRating(0) }, { Text(s.ratingAny) })
            listOf(6, 7, 8).forEach { r -> FilterChip(st.minRating == r, { vm.setMinRating(r) }, { Text(s.ratingAtLeast(r)) }) }
        }
        Text(s.genreLabel, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = onGenre, modifier = Modifier.fillMaxWidth()) {
            Text(if (st.genreKeys.isEmpty()) s.selectGenres else s.selectedCount(st.genreKeys.size))
        }
        Text(s.origLanguage, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = onLang, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (st.origLang.isBlank()) s.allGenres
                else Genres.languages.firstOrNull { it.first == st.origLang }?.second?.of(s.lang) ?: st.origLang
            )
        }
    }
}

@Composable
private fun SearchablePickerDialog(
    title: String, searchHint: String, options: List<Pair<String, String>>,
    selected: Set<String>, multi: Boolean, onToggle: (String) -> Unit, onClose: () -> Unit
) {
    val s = LocalStrings.current
    var query by remember { mutableStateOf("") }
    val filtered = options.filter { it.second.contains(query.trim(), ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    placeholder = { Text(searchHint) }, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(AppIcons.Search, contentDescription = null) }
                )
                LazyColumn(Modifier.heightIn(max = 360.dp).padding(top = 8.dp)) {
                    items(filtered, key = { it.first }) { (key, label) ->
                        FilterChip(
                            selected = key in selected,
                            onClick = { onToggle(key) },
                            label = { Text(label) },
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(s.done) } }
    )
}

@Composable
private fun ResultRow(
    item: SearchItem, inList: Boolean, adding: Boolean,
    onOpen: () -> Unit
) {
    val s = LocalStrings.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onOpen).padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Poster(item.posterPath, Modifier.width(56.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(10.dp)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val rating = if (item.rating > 0) "  •  ★ " + String.format(java.util.Locale.US, "%.1f", item.rating) else ""
            Text(s.typeYear(item.mediaType == "tv", item.year) + rating, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when {
            adding -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            inList -> Icon(AppIcons.Check, contentDescription = s.inList, tint = MaterialTheme.colorScheme.primary)
            else -> {}
        }
    }
}
