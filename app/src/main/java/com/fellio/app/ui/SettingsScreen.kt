@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.fellio.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fellio.app.data.Config
import com.fellio.app.data.Lang
import com.fellio.app.data.Platform
import com.fellio.app.data.SubtitleLangs

@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit, onStats: () -> Unit = {}) {
    val s = LocalStrings.current
    val customKey by vm.customKey.collectAsStateWithLifecycle()
    val customSubsKey by vm.customSubsKey.collectAsStateWithLifecycle()
    val subtitleLangs by vm.subtitleLangs.collectAsStateWithLifecycle()
    val accent by vm.accent.collectAsStateWithLifecycle()
    val lang by vm.lang.collectAsStateWithLifecycle()
    val theme by vm.theme.collectAsStateWithLifecycle()
    val jalali by vm.jalali.collectAsStateWithLifecycle()
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val dismissedCount by vm.dismissedCount.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val tmdbOk by vm.tmdbOk.collectAsStateWithLifecycle()
    val subsOk by vm.subsOk.collectAsStateWithLifecycle()

    var pendingImport by remember { mutableStateOf<Any?>(null) }
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snack.showSnackbar(s.message(it))
            vm.clearMessage()
        }
    }

    val exportFile = rememberSaveFileLauncher("application/json") { vm.exportBackup(it) }
    val importFile = rememberOpenFileLauncher { pendingImport = it }
    val notificationSwitch = rememberNotificationSwitch { vm.setNotifications(it) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text(s.settings, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(AppIcons.Back, contentDescription = s.back) } }
            )
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpandableSection(title = s.sectionAppearance, icon = AppIcons.Palette, subtitle = "${lang.native} · ${
                when (theme) { "light" -> s.themeLight; "dark" -> s.themeDark; else -> s.themeSystem }
            }", initiallyOpen = true) {
                Text(s.language, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Lang.values().forEach { l -> FilterChip(selected = lang == l, onClick = { vm.setLang(l) }, label = { Text(l.native) }) }
                }
                val tm by vm.titleMode.collectAsStateWithLifecycle()
                                Text(s.titleModeLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf("local" to (if (s.fa) "هم‌زبان اپ" else "Same as app")) + Lang.values().map { it.code to it.native }).forEach { (k, l) ->
                        FilterChip(selected = tm == k, onClick = { vm.setTitleMode(k) }, label = { Text(l) })
                    }
                }
                val tp by vm.titleParen.collectAsStateWithLifecycle()
                Text(s.titleParenLabel, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("none" to s.titleParenNone, "local" to s.titleParenLocal, "original" to s.titleParenOriginal).forEach { (k, l) ->
                        FilterChip(selected = tp == k, onClick = { vm.setTitleParen(k) }, label = { Text(l) })
                    }
                }
                Text(s.theme, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(theme == "system", { vm.setTheme("system") }, { Text(s.themeSystem) })
                    FilterChip(theme == "light", { vm.setTheme("light") }, { Text(s.themeLight) })
                    FilterChip(theme == "dark", { vm.setTheme("dark") }, { Text(s.themeDark) })
                }
                val fs by vm.fontScale.collectAsStateWithLifecycle()
                Text(if (s.fa) "اندازه متن" else "Font size", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.85f to (if (s.fa) "کوچک" else "Small"), 1f to (if (s.fa) "متوسط" else "Medium"),
                        1.15f to (if (s.fa) "بزرگ" else "Large"), 1.3f to (if (s.fa) "خیلی بزرگ" else "XL")).forEach { (v, l) ->
                        FilterChip(fs == v, { vm.setFontScale(v) }, { Text(l) })
                    }
                }
                Text(s.accent, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccentChoices.forEach { c ->
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(Color(c))
                                .border(width = if (c == accent) 2.dp else 0.dp, color = MaterialTheme.colorScheme.onBackground, shape = CircleShape)
                                .clickable { vm.setAccent(c) },
                            contentAlignment = Alignment.Center
                        ) { if (c == accent) Icon(AppIcons.Check, contentDescription = null, tint = Color.White) }
                    }
                }
                if (lang == Lang.FA) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.jalaliDates, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = jalali, onCheckedChange = { vm.setJalali(it) })
                    }
                }
            }

            if (Platform.supportsNotifications) {
                ExpandableSection(title = s.notifications, icon = AppIcons.Bell) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.notifications, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = notifications, onCheckedChange = { notificationSwitch(it) })
                    }
                    Text(s.notificationsHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            ExpandableSection(title = s.refreshAll, icon = AppIcons.Refresh) {
                OutlinedButton(onClick = { vm.refreshAll() }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (busy) s.refreshingAll else s.refreshAll)
                }
                if (dismissedCount > 0) {
                    TextButton(onClick = { vm.clearDismissed() }) { Text(s.resetHidden) }
                }
            }

            ExpandableSection(title = s.sectionSubtitles, icon = AppIcons.Subtitles, subtitle = subtitleLangs.joinToString(", ") { it.uppercase() }) {
                Text(s.subsLangs, style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubtitleLangs.options.forEach { (code, label) ->
                        FilterChip(selected = code in subtitleLangs, onClick = { vm.toggleSubLang(code) }, label = { Text(label) })
                    }
                }
                Text(s.subsLangsHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            ExpandableSection(
                title = s.apiKeyTitle, icon = AppIcons.Key,
                subtitle = if (customKey.isBlank() && customSubsKey.isBlank()) s.keyBuiltIn else s.keyCustom
            ) {
                Text("TMDB", style = MaterialTheme.typography.labelLarge)
                KeySection(
                    customKey = customKey,
                    hint = if (customKey.isBlank()) s.apiKeyDefault else s.apiKeyCustom,
                    guideButton = s.howToKey, guideSteps = s.guideSteps,
                    primaryLabel = s.openSignup, primaryUrl = Config.TMDB_SIGNUP_PAGE,
                    secondaryLabel = s.openTmdb, secondaryUrl = Config.TMDB_API_PAGE,
                    status = tmdbOk, busy = busy, onSave = { vm.saveKey(it) }, onReset = { vm.resetKey() }, onTest = { vm.testKey() }
                )
                Text("SubDL", style = MaterialTheme.typography.labelLarge)
                KeySection(
                    customKey = customSubsKey,
                    hint = if (customSubsKey.isBlank()) s.subsKeyDefault else s.apiKeyCustom,
                    guideButton = s.subsGuideBtn, guideSteps = s.subsGuideSteps,
                    primaryLabel = s.openSubdlSite, primaryUrl = Config.SUBDL_SITE,
                    secondaryLabel = s.openSubdlApi, secondaryUrl = Config.SUBDL_API_PAGE,
                    status = subsOk, busy = busy, onSave = { vm.saveSubsKey(it) }, onReset = { vm.resetSubsKey() }, onTest = { vm.testSubsKey() }
                )
            }

            OutlinedButton(onClick = onStats, modifier = Modifier.fillMaxWidth()) { Text(s.stats) }
            ExpandableSection(title = s.sectionData, icon = AppIcons.Database) {
                Text(s.backup, style = MaterialTheme.typography.labelLarge)
                Button(onClick = { exportFile("didam-backup.json") }, modifier = Modifier.fillMaxWidth()) { Text(s.exportBtn) }
                OutlinedButton(onClick = { importFile() }, modifier = Modifier.fillMaxWidth()) { Text(s.importBtn) }
                Text(s.about, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    pendingImport?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text(s.importConfirmTitle) },
            text = { Text(s.importConfirmMsg) },
            confirmButton = { TextButton(onClick = { vm.importBackup(target); pendingImport = null }) { Text(s.confirm) } },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text(s.cancel) } }
        )
    }
}

@Composable
private fun KeySection(
    customKey: String, hint: String, guideButton: String, guideSteps: List<String>,
    primaryLabel: String, primaryUrl: String, secondaryLabel: String, secondaryUrl: String,
    status: Boolean?, busy: Boolean, onSave: (String) -> Unit, onReset: () -> Unit, onTest: () -> Unit
) {
    val s = LocalStrings.current
    val uriHandler = LocalUriHandler.current
    var keyText by remember(customKey) { mutableStateOf(customKey) }
    var guideOpen by rememberSaveable(guideButton) { mutableStateOf(false) }

    Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    OutlinedTextField(
        value = keyText, onValueChange = { keyText = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        shape = RoundedCornerShape(16.dp), placeholder = { Text(s.apiKeyPlaceholder) },
        trailingIcon = {
            if (status != null) Box(
                Modifier.size(14.dp).clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (status) Color(0xFF2ECC71) else Color(0xFFE50914))
            )
        }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { onSave(keyText) }, enabled = keyText.isNotBlank() && keyText.trim() != customKey) { Text(s.save) }
        OutlinedButton(onClick = onTest, enabled = !busy) { Text(s.testKey) }
        if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
    }
    if (customKey.isNotBlank()) TextButton(onClick = { onReset(); keyText = "" }) { Text(s.resetKey) }
    OutlinedButton(onClick = { guideOpen = !guideOpen }, modifier = Modifier.fillMaxWidth()) { Text(guideButton) }
    if (guideOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            guideSteps.forEach { step -> Text(step, style = MaterialTheme.typography.bodyMedium) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { uriHandler.openUri(primaryUrl) }) { Text(primaryLabel) }
                OutlinedButton(onClick = { uriHandler.openUri(secondaryUrl) }) { Text(secondaryLabel) }
            }
        }
    }
}
