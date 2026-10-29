package com.fellio.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fellio.app.DidamApp
import com.fellio.app.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Progress(val watched: Int, val total: Int, val next: EpisodeEntity?) {
    val percent: Int get() = if (total == 0) 0 else watched * 100 / total
    val complete: Boolean get() = total > 0 && watched == total
}

fun List<EpisodeEntity>.progress() = Progress(count { it.watched }, size, firstOrNull { !it.watched })

enum class HomeTab { WATCHING, WATCHLIST, COMPLETED }

data class HomeItem(val title: TitleEntity, val progress: Progress?) {
    val isCompleted: Boolean get() = if (progress != null) progress.complete else title.movieWatched
    val tab: HomeTab
        get() = when {
            isCompleted -> HomeTab.COMPLETED
            title.inWatchlist -> HomeTab.WATCHLIST
            else -> HomeTab.WATCHING
        }
}

/** One entry in the home screen's unified chip row. Built-ins can be hidden, never deleted. */
data class ChipDef(val id: String, val label: String, val icon: String?, val color: Int?, val builtIn: Boolean)

fun errKind(e: Throwable): String = when (e) {
    is TmdbException -> when (e.error) {
        TmdbError.NO_KEY -> "no_key"
        TmdbError.BAD_KEY -> "bad_key"
        TmdbError.SERVER -> "server"
        TmdbError.NETWORK -> "network"
    }
    is SubsException -> when (e.error) {
        SubsError.NO_KEY, SubsError.BAD_KEY -> "subs_bad_key"
        SubsError.LIMIT -> "subs_limit"
        SubsError.SERVER -> "subs_server"
        SubsError.NETWORK -> "subs_network"
        SubsError.NOT_FOUND -> "subs_notfound"
    }
    else -> "unknown"
}

private fun matchesCategory(t: TitleEntity, category: String): Boolean = when (category) {
    "movie" -> !t.isTv()
    "tv" -> t.isTv()
    "anime" -> t.isAnime()
    else -> true
}

private fun buildItems(titles: List<TitleEntity>, eps: List<EpisodeEntity>): List<HomeItem> {
    val by = eps.groupBy { it.titleId }
    return titles.map { HomeItem(it, if (it.isTv()) (by[it.id] ?: emptyList()).progress() else null) }
}

// ---------------------------------------------------------------- Home

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val da = app as DidamApp
    private val repo = da.repo
    private val settings = da.settings
    val query = MutableStateFlow("")
    /** id: "all" | "movie" | "tv" | "anime" | "pl:<playlistId>" */
    val category = MutableStateFlow("all")
    val tabIndex = MutableStateFlow(0)
    val searching = MutableStateFlow(false)
    val grid: StateFlow<Boolean> = da.settings.grid
    val suggestRows = MutableStateFlow<List<SuggestionRow>>(emptyList())
    val banner = MutableStateFlow<List<SearchItem>>(emptyList())
    val history: StateFlow<List<SearchItem>> = da.history.items
    fun clearHistory() = da.history.clear()

    val playlists: StateFlow<List<PlaylistEntity>> = repo.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val playlistItemsByPlaylist: StateFlow<Map<Long, Set<String>>> = repo.allPlaylistItems
        .map { l -> l.groupBy({ it.playlistId }, { it.titleId }).mapValues { it.value.toSet() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** id -> is in watchlist */
    val existing: StateFlow<Map<String, Boolean>> = repo.titles
        .map { l -> l.associate { it.id to it.inWatchlist } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** The ordered, visible chip list: built-ins (all/movie/tv/anime) + custom playlists. */
    val chips: StateFlow<List<ChipDef>> = combine(playlists, settings.collectionOrder, settings.collectionHidden) { pls, order, hidden ->
        val builtIns = listOf(
            ChipDef("all", "", null, null, true),
            ChipDef("movie", "", null, null, true),
            ChipDef("tv", "", null, null, true),
            ChipDef("anime", "", null, null, true)
        )
        val plChips = pls.map { ChipDef("pl:${it.id}", it.name, it.icon, it.color, false) }
        val all = (builtIns + plChips).associateBy { it.id }
        val ordered = (order.filter { it in all.keys } + all.keys.filter { it !in order }).map { all.getValue(it) }
        ordered.filter { it.id !in hidden }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All chips including hidden ones, for the "edit chips" dialog. */
    val allChips: StateFlow<List<ChipDef>> = combine(playlists, settings.collectionOrder) { pls, order ->
        val builtIns = listOf(
            ChipDef("all", "", null, null, true), ChipDef("movie", "", null, null, true),
            ChipDef("tv", "", null, null, true), ChipDef("anime", "", null, null, true)
        )
        val plChips = pls.map { ChipDef("pl:${it.id}", it.name, it.icon, it.color, false) }
        val all = (builtIns + plChips).associateBy { it.id }
        (order.filter { it in all.keys } + all.keys.filter { it !in order }).map { all.getValue(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val hiddenChips: StateFlow<Set<String>> = settings.collectionHidden

    val tabs: StateFlow<Map<HomeTab, List<HomeItem>>> =
        combine(repo.titles, repo.allEpisodes, query, category, playlistItemsByPlaylist) { titles, eps, q, cat, plMap ->
            val byCategory = if (cat.startsWith("pl:")) {
                val pid = cat.removePrefix("pl:").toLongOrNull()
                val ids = plMap[pid] ?: emptySet()
                titles.filter { it.id in ids }
            } else {
                titles.filter { matchesCategory(it, cat) }
            }
            val filtered = byCategory.filter { q.isBlank() || it.name.contains(q.trim(), ignoreCase = true) }
            val items = buildItems(filtered, eps)
            HomeTab.values().associateWith { tab -> items.filter { it.tab == tab } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch { banner.value = runCatching { repo.trendingItems().take(6) }.getOrDefault(emptyList()) }
        viewModelScope.launch {
            combine(repo.titles.map { it.size }.distinctUntilChanged(), settings.dismissed) { _, dismissed -> dismissed }
                .collectLatest { dismissed -> suggestRows.value = repo.suggestionRows(dismissed, full = false) }
        }
    }

    fun setQuery(q: String) { query.value = q }
    fun setCategory(c: String) { category.value = c }
    fun setTab(i: Int) { tabIndex.value = i }
    fun setSearching(b: Boolean) { searching.value = b }
    fun toggleGrid() = da.settings.setGrid(!grid.value)

    fun setChipOrder(ids: List<String>) = settings.setCollectionOrder(ids)
    fun setChipHidden(hidden: Set<String>) = settings.setCollectionHidden(hidden)

    fun dismissSuggestion(id: String) {
        settings.addDismissed(id)
        suggestRows.update { rows -> rows.map { r -> r.copy(items = r.items.filter { it.id != id }) }.filter { it.items.isNotEmpty() } }
    }

    fun quick(item: HomeItem) {
        viewModelScope.launch {
            val p = item.progress
            if (p != null) {
                p.next?.let { repo.setEpisodeWatched(it, true) }
            } else if (!item.title.movieWatched) {
                repo.setMovieWatched(item.title.id, true)
            }
        }
    }

    fun createPlaylist(name: String, color: Int, icon: String) {
        viewModelScope.launch { repo.createPlaylist(name, color, icon) }
    }

    fun addItem(item: SearchItem, watchlist: Boolean, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.add(item, watchlist)
                onDone(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }
}

// ---------------------------------------------------------------- Discover

class DiscoverViewModel(app: Application) : AndroidViewModel(app) {
    private val da = app as DidamApp
    private val repo = da.repo
    val rows = MutableStateFlow<List<SuggestionRow>>(emptyList())
    val loading = MutableStateFlow(true)
    val existing: StateFlow<Map<String, Boolean>> = repo.titles
        .map { l -> l.associate { it.id to it.inWatchlist } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val settings get() = da.settings

    init { reload() }

    fun saveInterests(types: Map<String, Int>, genres: Set<Int>) {
        da.settings.setInterests(types, genres)
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            loading.value = true
            val st = da.settings
            val dis = st.dismissed.value
            val types = st.prefTypes.value
            val mine = repo.interestRows(types, st.favGenres.value, st.learnedGenres(), dis)
            val base = repo.suggestionRows(dis, full = true).filter { it.kind != "anime" || (types["anime"] ?: 1) > 0 }
            val extra = repo.extraRows().filter { r ->
                val movie = r.refName in setOf("nowPlaying", "upcoming", "popularMovies")
                (types[if (movie) "movie" else "tv"] ?: 1) > 0
            }.map { r -> r.copy(items = r.items.filter { it.id !in dis }) }
            rows.value = (mine + base + extra).distinctBy { it.kind + it.refId + it.refName }
            loading.value = false
        }
    }

    fun dismiss(id: String) {
        da.settings.addDismissed(id)
        rows.update { l -> l.map { it.copy(items = it.items.filter { i -> i.id != id }) }.filter { it.items.isNotEmpty() } }
    }

    fun addItem(item: SearchItem, watchlist: Boolean, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.add(item, watchlist)
                onDone(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }
}

// ---------------------------------------------------------------- Add / Search

data class AddState(
    val query: String = "",
    val activeQuery: String = "",
    /** all | movie | tv | anime */
    val category: String = "all",
    val year: String = "",
    val genreKeys: Set<String> = emptySet(),
    val origLang: String = "",
    val minRating: Int = 0,
    /** relevance | popularity | rating | newest */
    val sort: String = "relevance",
    val results: List<SearchItem> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val searched: Boolean = false,
    val hasMore: Boolean = false,
    val page: Int = 1,
    val errorKind: String? = null,
    val addingId: String? = null
) {
    val filterCount: Int
        get() = (if (category != "all") 1 else 0) + (if (year.isNotEmpty()) 1 else 0) +
            genreKeys.size + (if (origLang.isNotEmpty()) 1 else 0) +
            (if (minRating > 0) 1 else 0) + (if (sort != "relevance") 1 else 0)
}

class AddViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as DidamApp).repo
    val state = MutableStateFlow(AddState())

    val existing: StateFlow<Map<String, Boolean>> = repo.titles
        .map { l -> l.associate { it.id to it.inWatchlist } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private var job: kotlinx.coroutines.Job? = null

    init { load(true) }

    fun onQuery(q: String) = state.update { it.copy(query = q) }
    fun setCategory(c: String) { state.update { it.copy(category = c) }; load(true) }
    fun setSort(s: String) { state.update { it.copy(sort = s) }; load(true) }
    fun toggleGenre(k: String) {
        state.update { it.copy(genreKeys = if (k in it.genreKeys) it.genreKeys - k else it.genreKeys + k) }
        load(true)
    }
    fun setOrigLang(l: String) { state.update { it.copy(origLang = if (it.origLang == l) "" else l) }; load(true) }
    fun setMinRating(r: Int) { state.update { it.copy(minRating = if (it.minRating == r) 0 else r) }; load(true) }
    fun setYear(y: String) {
        val v = y.filter { it.isDigit() }.take(4)
        state.update { it.copy(year = v) }
        if (v.length == 4 || v.isEmpty()) load(true)
    }
    fun clearFilters() {
        state.update { it.copy(category = "all", year = "", genreKeys = emptySet(), origLang = "", minRating = 0, sort = "relevance") }
        load(true)
    }
    fun search() = load(true)
    fun loadMore() {
        val s = state.value
        if (s.hasMore && !s.loading && !s.loadingMore) load(false)
    }

    private fun load(reset: Boolean) {
        job?.cancel()
        val s = state.value
        val page = if (reset) 1 else s.page + 1
        val q = if (reset) s.query else s.activeQuery
        job = viewModelScope.launch {
            state.update { it.copy(loading = reset, loadingMore = !reset, errorKind = null, activeQuery = q) }
            try {
                val genres = Genres.options.filter { it.key in s.genreKeys }
                val pg = repo.browse(q, s.category, s.year, genres, s.sort, s.minRating, s.origLang, page)
                state.update { cur ->
                    val merged = if (reset) pg.items else (cur.results + pg.items).distinctBy { it.id }
                    cur.copy(
                        loading = false, loadingMore = false, results = merged, page = page,
                        hasMore = page < pg.totalPages, searched = true
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update { it.copy(loading = false, loadingMore = false, errorKind = errKind(e)) }
            }
        }
    }

    fun add(item: SearchItem, watchlist: Boolean, onDone: (Boolean) -> Unit) {
        if (state.value.addingId != null) return
        viewModelScope.launch {
            state.update { it.copy(addingId = item.id, errorKind = null) }
            try {
                repo.add(item, watchlist)
                state.update { it.copy(addingId = null) }
                onDone(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update { it.copy(addingId = null, errorKind = errKind(e)) }
                onDone(false)
            }
        }
    }
}

// ---------------------------------------------------------------- Detail

data class SubsState(
    val open: Boolean = false,
    val season: Int? = null,
    val episode: Int? = null,
    val loading: Boolean = false,
    val results: List<SubtitleResult> = emptyList(),
    val errorKind: String? = null,
    val downloadingUrl: String? = null,
    val readyName: String? = null
)

class DetailViewModel(app: Application, handle: SavedStateHandle) : AndroidViewModel(app) {
    private val da = app as DidamApp
    private val repo = da.repo
    private val id: String = handle.get<String>("id") ?: ""

    val title: StateFlow<TitleEntity?> = repo.title(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val episodes: StateFlow<List<EpisodeEntity>> = repo.episodes(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val refreshing = MutableStateFlow(false)
    val similar = MutableStateFlow<List<SearchItem>>(emptyList())
    val subs = MutableStateFlow(SubsState())
    val trailer = MutableStateFlow<String?>(null)
    val trailerLoading = MutableStateFlow(false)
    val trailerMissing = MutableStateFlow(false)
    fun playTrailer() {
        val t = title.value ?: return
        viewModelScope.launch {
            trailerLoading.value = true
            val k = repo.trailerKey(t.mediaType, t.tmdbId)
            trailerLoading.value = false
            if (k == null) trailerMissing.value = true else trailer.value = k
        }
    }

    val playlists: StateFlow<List<PlaylistEntity>> = repo.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val memberOf: StateFlow<Set<Long>> = repo.playlistIdsFor(id)
        .map { l -> l.map { it.playlistId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val existing: StateFlow<Map<String, Boolean>> = repo.titles
        .map { l -> l.associate { it.id to it.inWatchlist } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private var pendingBytes: ByteArray? = null
    val fetchError = MutableStateFlow(false)

    private var autoSaved = false

    init { load() }

    // Preview only: if the title was saved just by opening it and the user did nothing, remove it again.
    override fun onCleared() {
        val t = title.value
        if (autoSaved && t != null && !t.inWatchlist && !t.movieWatched &&
            episodes.value.none { it.watched } && memberOf.value.isEmpty()
        ) {
            val r = repo
            val tid = id
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch { runCatching { r.delete(tid) } }
        }
        super.onCleared()
    }

    fun load() {
        viewModelScope.launch {
            fetchError.value = false
            if (title.value == null) autoSaved = true
            val t = runCatching { repo.fetchAndSave(id) }.getOrNull()
            if (t == null && title.value == null) { fetchError.value = true; return@launch }
            (t ?: title.value)?.let { x ->
                da.history.record(SearchItem(x.tmdbId, x.mediaType, x.name, x.year, x.posterPath, "", 0.0, 0.0, emptyList()))
            }
            runCatching { repo.backfillDetails(id) }
            (t ?: title.value)?.let { x -> da.settings.learn(x.genreIds.split(",").mapNotNull { it.toIntOrNull() }, 1) }
            similar.value = runCatching { repo.similar(t?.mediaType ?: title.value!!.mediaType, t?.tmdbId ?: title.value!!.tmdbId) }.getOrDefault(emptyList())
        }
    }

    fun setMovieWatched(w: Boolean) { viewModelScope.launch { repo.setMovieWatched(id, w) } }
    fun setEpisodeWatched(ep: EpisodeEntity, w: Boolean) { viewModelScope.launch { repo.setEpisodeWatched(ep, w) } }
    fun setSeasonWatched(season: Int, w: Boolean) { viewModelScope.launch { repo.setSeasonWatched(id, season, w) } }
    fun markUpTo(season: Int, ep: Int) { viewModelScope.launch { repo.markUpTo(id, season, ep) } }
    fun toggleWatchlist(current: Boolean) { viewModelScope.launch { repo.setWatchlist(id, !current) } }
    fun delete() { viewModelScope.launch { repo.delete(id) } }

    fun togglePlaylist(playlistId: Long, isMember: Boolean) {
        viewModelScope.launch {
            if (isMember) repo.removeFromPlaylist(playlistId, id) else repo.addToPlaylist(playlistId, id)
        }
    }

    fun createPlaylistAndAdd(name: String, color: Int, icon: String) {
        viewModelScope.launch {
            val pid = repo.createPlaylist(name, color, icon)
            repo.addToPlaylist(pid, id)
        }
    }

    fun addItem(item: SearchItem, watchlist: Boolean, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.add(item, watchlist)
                onDone(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }

    fun refresh(done: (Int, String?) -> Unit) {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            try {
                val n = repo.refreshSeries(id)
                refreshing.value = false
                done(n, null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                refreshing.value = false
                done(0, errKind(e))
            }
        }
    }

    fun openSubs(season: Int?, episode: Int?) {
        val t = title.value ?: return
        pendingBytes = null
        subs.value = SubsState(open = true, season = season, episode = episode, loading = true)
        viewModelScope.launch {
            try {
                val r = repo.searchSubtitles(t.tmdbId, t.isTv(), season, episode, da.settings.subtitleLangs.value)
                subs.update { it.copy(loading = false, results = r) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                subs.update { it.copy(loading = false, errorKind = errKind(e)) }
            }
        }
    }

    fun closeSubs() {
        pendingBytes = null
        subs.value = SubsState()
    }

    fun downloadSub(r: SubtitleResult) {
        if (subs.value.downloadingUrl != null) return
        val season = subs.value.season
        val episode = subs.value.episode
        viewModelScope.launch {
            subs.update { it.copy(downloadingUrl = r.url, errorKind = null, readyName = null) }
            try {
                val (name, bytes) = repo.downloadSubtitle(r.url, season, episode)
                pendingBytes = bytes
                subs.update { it.copy(downloadingUrl = null, readyName = name) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                subs.update { it.copy(downloadingUrl = null, errorKind = errKind(e)) }
            }
        }
    }

    fun writeSubtitle(target: Any, done: (Boolean) -> Unit) {
        val bytes = pendingBytes
        if (bytes == null) {
            done(false)
            return
        }
        viewModelScope.launch {
            val ok = try {
                withContext(Dispatchers.IO) { Platform.writeBytes(target, bytes) }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
            pendingBytes = null
            subs.update { it.copy(readyName = null) }
            done(ok)
        }
    }
}

// ---------------------------------------------------------------- Person

data class PersonState(
    val loading: Boolean = true,
    val data: PersonDetails? = null,
    val errorKind: String? = null
)

class PersonViewModel(app: Application, handle: SavedStateHandle) : AndroidViewModel(app) {
    private val idStr: String = handle.get<String>("id") ?: ""
    private val repo = (app as DidamApp).repo
    private val personId: Int = idStr.toIntOrNull() ?: 0

    val state = MutableStateFlow(PersonState())
    /** all | movie | tv */
    val filmCategory = MutableStateFlow("all")
    /** popularity | year | rating */
    val filmSort = MutableStateFlow("popularity")
    val existing: StateFlow<Map<String, Boolean>> = repo.titles
        .map { l -> l.associate { it.id to it.inWatchlist } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val filmography: StateFlow<List<SearchItem>> = combine(state, filmCategory, filmSort) { st, cat, sort ->
        var list = st.data?.filmography.orEmpty()
        if (cat != "all") list = list.filter { it.mediaType == cat }
        list = when (sort) {
            "year" -> list.sortedByDescending { it.year }
            "rating" -> list.sortedByDescending { it.rating }
            else -> list.sortedByDescending { it.popularity }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { load() }

    fun load() {
        viewModelScope.launch {
            state.value = PersonState(loading = true)
            try {
                state.value = PersonState(loading = false, data = repo.personDetails(personId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.value = PersonState(loading = false, errorKind = errKind(e))
            }
        }
    }

    fun setFilmCategory(c: String) { filmCategory.value = c }
    fun setFilmSort(s: String) { filmSort.value = s }

    fun addItem(item: SearchItem, watchlist: Boolean, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repo.add(item, watchlist)
                onDone(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }
}

// ---------------------------------------------------------------- Playlist

data class PlaylistEntry(val item: PlaylistItemEntity, val home: HomeItem)

class PlaylistViewModel(app: Application, handle: SavedStateHandle) : AndroidViewModel(app) {
    private val idStr: String = handle.get<String>("id") ?: ""
    private val repo = (app as DidamApp).repo
    val id: Long = idStr.toLongOrNull() ?: 0L

    val playlist: StateFlow<PlaylistEntity?> = repo.playlist(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val entries: StateFlow<List<PlaylistEntry>> =
        combine(repo.playlistItemIds(id), repo.titles, repo.allEpisodes) { items, titles, eps ->
            val byId = titles.associateBy { it.id }
            val epsBy = eps.groupBy { it.titleId }
            items.mapNotNull { pi ->
                byId[pi.titleId]?.let { t ->
                    PlaylistEntry(pi, HomeItem(t, if (t.isTv()) (epsBy[t.id] ?: emptyList()).progress() else null))
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTitles: StateFlow<List<TitleEntity>> = repo.titles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun move(titleId: String, up: Boolean) {
        viewModelScope.launch { repo.movePlaylistItem(entries.value.map { it.item }, titleId, up) }
    }

    fun remove(titleId: String) { viewModelScope.launch { repo.removeFromPlaylist(id, titleId) } }

    fun toggle(titleId: String, isMember: Boolean) {
        viewModelScope.launch {
            if (isMember) repo.removeFromPlaylist(id, titleId) else repo.addToPlaylist(id, titleId)
        }
    }

    fun edit(name: String, color: Int, icon: String) {
        val p = playlist.value ?: return
        viewModelScope.launch { repo.renamePlaylist(p, name, color, icon) }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deletePlaylist(id)
            onDone()
        }
    }
}

// ---------------------------------------------------------------- Stats

data class Stats(
    val moviesWatched: Int = 0,
    val episodesWatched: Int = 0,
    val seriesCompleted: Int = 0,
    val watching: Int = 0,
    val watchlist: Int = 0,
    val minutes: Long = 0,
    val topGenres: List<Pair<Int, Int>> = emptyList()
)

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as DidamApp).repo

    val stats: StateFlow<Stats> = combine(repo.titles, repo.allEpisodes) { titles, eps ->
        val items = buildItems(titles, eps)
        val byTitle = eps.groupBy { it.titleId }
        var minutes = 0L
        titles.forEach { t ->
            if (t.isTv()) {
                (byTitle[t.id] ?: emptyList()).filter { it.watched }.forEach { e ->
                    minutes += if (e.runtime > 0) e.runtime else if (t.runtime > 0) t.runtime else 40
                }
            } else if (t.movieWatched) {
                minutes += t.runtime
            }
        }
        val genreCount = HashMap<Int, Int>()
        items.filter { it.title.movieWatched || (it.progress?.watched ?: 0) > 0 }.forEach { item ->
            item.title.genreIds.split(",").mapNotNull { it.toIntOrNull() }.forEach { g ->
                genreCount[g] = (genreCount[g] ?: 0) + 1
            }
        }
        Stats(
            moviesWatched = titles.count { !it.isTv() && it.movieWatched },
            episodesWatched = eps.count { it.watched },
            seriesCompleted = items.count { it.title.isTv() && it.isCompleted },
            watching = items.count { it.tab == HomeTab.WATCHING },
            watchlist = items.count { it.tab == HomeTab.WATCHLIST },
            minutes = minutes,
            topGenres = genreCount.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Stats())
}

// ---------------------------------------------------------------- Settings

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val da = app as DidamApp
    private val settings = da.settings
    private val repo = da.repo

    val customKey = settings.customKey
    val customSubsKey = settings.customSubsKey
    val subtitleLangs = settings.subtitleLangs
    val accent = settings.accent
    val lang = settings.lang
    val theme = settings.theme
    val jalali = settings.jalali
    val notifications = settings.notifications
    val dismissedCount: StateFlow<Int> = settings.dismissed.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val message = MutableStateFlow<String?>(null)
    val busy = MutableStateFlow(false)
    val tmdbOk = MutableStateFlow<Boolean?>(null)
    val subsOk = MutableStateFlow<Boolean?>(null)

    fun saveKey(k: String) { settings.setCustomKey(k); message.value = "key_saved" }
    fun resetKey() { settings.setCustomKey(""); message.value = "key_saved" }
    fun saveSubsKey(k: String) { settings.setCustomSubsKey(k); message.value = "key_saved" }
    fun resetSubsKey() { settings.setCustomSubsKey(""); message.value = "key_saved" }
    fun setAccent(c: Int) = settings.setAccent(c)
    fun setLang(l: Lang) = settings.setLang(l)
    val titleMode = settings.titleMode
    val posterLang = settings.posterLang
    fun setTitleMode(m: String) = settings.setTitleMode(m)
    val titleParen = settings.titleParen
    fun setTitleParen(m: String) = settings.setTitleParen(m)
    fun setPosterLang(m: String) = settings.setPosterLang(m)
    val fontScale: StateFlow<Float> = settings.fontScale
    fun setFontScale(f: Float) = settings.setFontScale(f)
    fun setTheme(t: String) = settings.setTheme(t)
    fun setJalali(b: Boolean) = settings.setJalali(b)
    fun clearMessage() { message.value = null }
    fun clearDismissed() { settings.clearDismissed(); message.value = "dismissed_cleared" }

    fun toggleSubLang(code: String) {
        val cur = settings.subtitleLangs.value
        settings.setSubtitleLangs(if (code in cur) cur - code else cur + code)
    }

    fun setNotifications(on: Boolean) {
        settings.setNotifications(on)
        Platform.setNotifications(on)
    }

    fun testKey() {
        viewModelScope.launch {
            busy.value = true
            message.value = try {
                repo.browse("", "all", "", emptyList(), "relevance", 0, "", 1)
                tmdbOk.value = true
                "key_ok"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                tmdbOk.value = false
                errKind(e)
            }
            busy.value = false
        }
    }

    fun testSubsKey() {
        viewModelScope.launch {
            busy.value = true
            message.value = try {
                repo.searchSubtitles(603, false, null, null, setOf("en"))
                subsOk.value = true
                "key_ok"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                subsOk.value = false
                errKind(e)
            }
            busy.value = false
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            busy.value = true
            message.value = try {
                "refresh_done:${repo.refreshAll()}"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errKind(e)
            }
            busy.value = false
        }
    }

    fun exportBackup(target: Any) {
        viewModelScope.launch {
            message.value = try {
                val json = repo.exportJson()
                withContext(Dispatchers.IO) { Platform.writeBytes(target, json.toByteArray()) }
                "export_ok"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                "backup_failed"
            }
        }
    }

    fun importBackup(target: Any) {
        viewModelScope.launch {
            message.value = try {
                val text = withContext(Dispatchers.IO) { Platform.readText(target) }
                repo.importJson(text)
                "import_ok"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                "backup_failed"
            }
        }
    }
}


class GenreViewModel(app: Application, handle: SavedStateHandle) : AndroidViewModel(app) {
    private val repo = (app as DidamApp).repo
    val genreId: Int = (handle.get<String>("id") ?: "").toIntOrNull() ?: 0
    val type = MutableStateFlow("movie")
    val items = MutableStateFlow<List<SearchItem>>(emptyList())
    val loading = MutableStateFlow(true)
    private var page = 1
    val existing: StateFlow<Map<String, Boolean>> = repo.titles
        .map { l -> l.associate { it.id to it.inWatchlist } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init { reload() }
    fun setType(t: String) { type.value = t; reload() }
    private fun reload() {
        page = 1; items.value = emptyList()
        viewModelScope.launch {
            loading.value = true
            items.value = repo.genreItems(genreId, type.value, 1)
            loading.value = false
        }
    }
    fun more() {
        if (loading.value) return
        viewModelScope.launch {
            loading.value = true
            page++
            items.value = (items.value + repo.genreItems(genreId, type.value, page)).distinctBy { it.id }
            loading.value = false
        }
    }
}
