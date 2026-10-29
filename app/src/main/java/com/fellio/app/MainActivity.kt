package com.fellio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fellio.app.data.Lang
import com.fellio.app.ui.AddScreen
import com.fellio.app.ui.DetailScreen
import com.fellio.app.ui.DidamTheme
import com.fellio.app.ui.DiscoverScreen
import com.fellio.app.ui.GenreScreen
import com.fellio.app.ui.HomeScreen
import com.fellio.app.ui.LocalJalali
import com.fellio.app.ui.LocalStrings
import com.fellio.app.ui.PersonScreen
import com.fellio.app.ui.PlaylistScreen
import com.fellio.app.ui.SettingsScreen
import com.fellio.app.ui.StatsScreen
import com.fellio.app.ui.Strings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = (application as DidamApp).settings
        setContent {
            val accent by settings.accent.collectAsStateWithLifecycle()
            val lang by settings.lang.collectAsStateWithLifecycle()
            val themeMode by settings.theme.collectAsStateWithLifecycle()
            val jalali by settings.jalali.collectAsStateWithLifecycle()
            val fontScale by settings.fontScale.collectAsStateWithLifecycle()
            val dark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                )
                onDispose { }
            }
            val strings = remember(lang) { Strings(lang) }
            val dens = androidx.compose.ui.platform.LocalDensity.current
            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(dens.density, dens.fontScale * fontScale),
                LocalStrings provides strings,
                LocalJalali provides (jalali && lang == Lang.FA)
            ) {
                DidamTheme(accent, dark, lang) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        DidamNav()
                    }
                }
            }
        }
    }
}

@Composable
private fun DidamNav() {
    val nav = rememberNavController()
    val idArg = listOf(navArgument("id") { type = NavType.StringType })
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val st = com.fellio.app.ui.LocalStrings.current
    val tabs = listOf(
        Triple("lists", st.lists, com.fellio.app.ui.AppIcons.NavLists),
        Triple("home", st.appName, com.fellio.app.ui.AppIcons.NavHome),
        Triple("settings", st.settings, com.fellio.app.ui.AppIcons.NavSettings)
    )
    Scaffold(
        bottomBar = {
            if (tabs.any { it.first == route }) {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth()
                            .shadow(12.dp, androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                            .padding(6.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                    ) {
                        tabs.forEach { (r, label, icon) ->
                            val sel = route == r
                            val tint by androidx.compose.animation.animateColorAsState(
                                if (sel) androidx.compose.material3.MaterialTheme.colorScheme.primary
                                else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, tween(250), label = "tint")
                            val sc by androidx.compose.animation.core.animateFloatAsState(if (sel) 1.15f else 1f, tween(250), label = "sc")
                            androidx.compose.foundation.layout.Column(
                                Modifier.weight(1f).clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                                    .background(if (sel) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.28f) else androidx.compose.ui.graphics.Color.Transparent)
                                    .clickable {
                                        if (route != r) nav.navigate(r) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp).scale(sc))
                                androidx.compose.animation.AnimatedVisibility(sel) {
                                    Text(label, color = tint, maxLines = 1, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { pad ->
    NavHost(
        modifier = Modifier.padding(pad).consumeWindowInsets(pad),
        navController = nav,
        startDestination = "home",
        enterTransition = { fadeIn(tween(300)) + androidx.compose.animation.slideInHorizontally(tween(300)) { it / 8 } + androidx.compose.animation.scaleIn(tween(300), 0.96f) },
        exitTransition = { fadeOut(tween(200)) + androidx.compose.animation.slideOutHorizontally(tween(300)) { -it / 10 } },
        popEnterTransition = { fadeIn(tween(300)) + androidx.compose.animation.slideInHorizontally(tween(300)) { -it / 10 } },
        popExitTransition = { fadeOut(tween(200)) + androidx.compose.animation.slideOutHorizontally(tween(300)) { it / 8 } }
    ) {
        composable("home") {
            DiscoverScreen(vm = viewModel(), home = viewModel(), onBack = { nav.popBackStack() }, onOpen = { nav.navigate("detail/$it") }, onSearch = { nav.navigate("add") })
        }
        composable("lists") {
            HomeScreen(
                vm = viewModel(),
                onAdd = { nav.navigate("add") },
                onOpen = { nav.navigate("detail/$it") },
                onDiscover = { nav.navigate("home") },
                onPlaylist = { nav.navigate("playlist/$it") }
            )
        }
        composable("add") {
            AddScreen(
                vm = viewModel(),
                onBack = { nav.popBackStack() },
                onOpen = { nav.navigate("detail/$it") },
                onSettings = { nav.navigate("settings") }
            )
        }
        composable("detail/{id}", arguments = idArg) {
            DetailScreen(
                vm = viewModel(),
                onBack = { nav.popBackStack() },
                onPerson = { nav.navigate("person/$it") },
                onOpen = { nav.navigate("detail/$it") },
                onGenre = { nav.navigate("genre/$it") }
            )
        }
        composable("genre/{id}", arguments = idArg) {
            GenreScreen(vm = viewModel(), onBack = { nav.popBackStack() }, onOpen = { nav.navigate("detail/$it") })
        }
        composable("person/{id}", arguments = idArg) {
            PersonScreen(vm = viewModel(), onBack = { nav.popBackStack() }, onOpen = { nav.navigate("detail/$it") })
        }
        composable("playlist/{id}", arguments = idArg) {
            PlaylistScreen(vm = viewModel(), onBack = { nav.popBackStack() }, onOpen = { nav.navigate("detail/$it") })
        }
        composable("stats") { StatsScreen(vm = viewModel(), onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(vm = viewModel(), onBack = { nav.popBackStack() }, onStats = { nav.navigate("stats") }) }
    }
    }
}
