package com.fellio.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(vm: HomeViewModel, onPlaylist: (Long) -> Unit, onOpen: (String) -> Unit) {
    val s = LocalStrings.current
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val existing by vm.existing.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopAppBar(title = { Text(s.lists) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(AppIcons.Add, contentDescription = s.newPlaylist) }
        }
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (playlists.isEmpty()) item {
                Text(s.noPlaylists, Modifier.fillMaxWidth().padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(playlists, key = { it.id }) { p ->
                Row(
                    Modifier.fillMaxWidth().animateItem().clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onPlaylist(p.id) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(p.color)),
                        contentAlignment = Alignment.Center
                    ) { Text(p.name.take(1), color = Color.White, fontWeight = FontWeight.Bold) }
                    Text(p.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                }
            }
            if (history.isNotEmpty()) item(key = "history") {
                Column(Modifier.padding(top = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.recent, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { vm.clearHistory() }) { Text(s.clear) }
                    }
                    PosterRow(
                        title = "", items = history, existing = existing,
                        onClick = { onOpen(it.id) }, horizontalPadding = 0.dp
                    )
                }
            }
        }
    }
    if (showCreate) {
        PlaylistEditDialog(
            initial = null, onDismiss = { showCreate = false },
            onSave = { n, c, i -> vm.createPlaylist(n, c, i); showCreate = false }
        )
    }
}
