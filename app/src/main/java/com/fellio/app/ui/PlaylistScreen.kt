@file:OptIn(ExperimentalMaterial3Api::class)

package com.fellio.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fellio.app.data.isTv

@Composable
fun PlaylistScreen(vm: PlaylistViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val s = LocalStrings.current
    val playlist by vm.playlist.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val allTitles by vm.allTitles.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    val p = playlist

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    Column {
                        Text(
                            if (p != null) "${p.icon} ${p.name}" else "",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(s.itemsCount(entries.size), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(AppIcons.Back, contentDescription = s.back) }
                },
                actions = {
                    IconButton(onClick = { editing = true }) { Icon(AppIcons.Edit, contentDescription = s.editPlaylist) }
                    IconButton(onClick = { deleting = true }) { Icon(AppIcons.Delete, contentDescription = s.deletePlaylist) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { picking = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(AppIcons.Add, contentDescription = s.addTitlesToPlaylist) }
        }
    ) { pad ->
        if (entries.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(pad).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(s.playlistEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(entries, key = { _, e -> e.home.title.id }) { index, e ->
                    val t = e.home.title
                    val prog = e.home.progress
                    Card(
                        onClick = { onOpen(t.id) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent), modifier = Modifier.gradientCard()
                    ) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Poster(t.posterPath, Modifier.width(56.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(10.dp)))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(t.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(s.typeYear(t.isTv(), t.year), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (prog != null) {
                                    ProgressBar(prog.percent)
                                } else if (t.movieWatched) {
                                    Text("✓ ${s.watched}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { vm.move(t.id, true) }, enabled = index > 0, modifier = Modifier.size(36.dp)) {
                                    Icon(AppIcons.Up, contentDescription = s.moveUp, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { vm.move(t.id, false) }, enabled = index < entries.lastIndex, modifier = Modifier.size(36.dp)) {
                                    Icon(AppIcons.Down, contentDescription = s.moveDown, modifier = Modifier.size(20.dp))
                                }
                            }
                            IconButton(onClick = { vm.remove(t.id) }) {
                                Icon(AppIcons.Close, contentDescription = s.removeFromPlaylist)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editing && p != null) {
        PlaylistEditDialog(
            initial = p,
            onDismiss = { editing = false },
            onSave = { name, color, icon -> vm.edit(name, color, icon); editing = false }
        )
    }

    if (deleting && p != null) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text(s.deletePlaylist) },
            text = { Text(s.deletePlaylistMsg(p.name)) },
            confirmButton = {
                TextButton(onClick = {
                    deleting = false
                    vm.delete { onBack() }
                }) { Text(s.delete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleting = false }) { Text(s.cancel) } }
        )
    }

    if (picking) {
        val memberIds = entries.map { it.home.title.id }.toSet()
        AlertDialog(
            onDismissRequest = { picking = false },
            title = { Text(s.addTitlesToPlaylist) },
            text = {
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(allTitles, key = { it.id }) { t ->
                        val member = t.id in memberIds
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { vm.toggle(t.id, member) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = member, onCheckedChange = { vm.toggle(t.id, member) })
                            Text(t.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { picking = false }) { Text(s.close) } }
        )
    }
}
