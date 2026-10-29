@file:OptIn(ExperimentalMaterial3Api::class)

package com.fellio.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fellio.app.data.Genres

@Composable
fun GenreScreen(vm: GenreViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val s = LocalStrings.current
    val items by vm.items.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val type by vm.type.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text(Genres.label(vm.genreId, s.lang) ?: "") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(AppIcons.Back, contentDescription = s.back) } }
            )
        }
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(110.dp),
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "movie", onClick = { vm.setType("movie") }, label = { Text(s.t("فیلم", "Movies", "Filmler", "أفلام")) })
                    FilterChip(selected = type == "tv", onClick = { vm.setType("tv") }, label = { Text(s.t("سریال", "Series", "Diziler", "مسلسلات")) })
                }
            }
            items(items, key = { it.id }) { item ->
                Poster(item.posterPath, Modifier.fillMaxWidth().aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(10.dp)).clickable { onOpen(item.id) })
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    if (loading) CircularProgressIndicator()
                    else if (items.isNotEmpty()) {
                        LaunchedEffect(items.size) { vm.more() }
                    }
                }
            }
        }
    }
}
