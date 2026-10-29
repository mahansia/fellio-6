package com.fellio.app.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: DidamDb, private val api: TmdbApi, private val subs: SubtitlesApi) {
    private val dao = db.dao()

    val titles: Flow<List<TitleEntity>> = dao.titles()
    val allEpisodes: Flow<List<EpisodeEntity>> = dao.allEpisodes()
    suspend fun trendingItems(): List<SearchItem> = api.trending(1).items.filter { it.posterPath != null }
    fun title(id: String): Flow<TitleEntity?> = dao.title(id)
    fun episodes(id: String): Flow<List<EpisodeEntity>> = dao.episodes(id)

    /**
     * Search (when [q] is not empty) or filtered browse. [category]: all | movie | tv | anime.
     * Without text and without any filter, this-week trending is returned.
     */
    suspend fun browse(
        q: String, category: String, year: String, genres: List<Genres.Option>,
        sort: String, minRating: Int, origLang: String, page: Int
    ): Page {
        val query = q.trim()
        val y = year.trim().takeIf { it.length == 4 && it.all(Char::isDigit) } ?: ""
        if (query.isNotEmpty()) {
            val searchType = when (category) {
                "movie" -> "movie"
                "tv" -> "tv"
                else -> "all"
            }
            val p = api.search(query, searchType, y, page)
            var items = p.items
            if (y.isNotEmpty() && searchType == "all") items = items.filter { it.year == y }
            if (category == "anime") items = items.filter { it.originalLanguage == "ja" && 16 in it.genreIds }
            if (genres.isNotEmpty()) {
                items = items.filter { item ->
                    genres.all { opt ->
                        val ids = if (item.mediaType == "movie") opt.movie else opt.tv
                        item.genreIds.any { it in ids }
                    }
                }
            }
            if (origLang.isNotBlank() && category != "anime") items = items.filter { it.originalLanguage == origLang }
            if (minRating > 0) items = items.filter { it.rating >= minRating }
            items = when (sort) {
                "rating" -> items.sortedByDescending { it.rating }
                "popularity" -> items.sortedByDescending { it.popularity }
                "newest" -> items.sortedByDescending { it.year }
                else -> items
            }
            return Page(items, p.totalPages)
        }
        val hasFilters = category != "all" || y.isNotEmpty() || genres.isNotEmpty() ||
            sort != "relevance" || minRating > 0 || origLang.isNotBlank()
        return if (!hasFilters) api.trending(page) else api.discover(category, y, genres, sort, minRating, origLang, page)
    }

    suspend fun genreItems(genre: Int, type: String, page: Int): List<SearchItem> =
        runCatching { api.discoverByGenreIds(type, listOf(genre), page).items.filter { it.posterPath != null } }.getOrDefault(emptyList())

    suspend fun trailerKey(type: String, id: Int): String? = runCatching { api.trailerKey(type, id) }.getOrNull()

    suspend fun extraRows(): List<SuggestionRow> {
        val defs = listOf(
            Triple("/movie/now_playing", "movie", "nowPlaying"), Triple("/movie/upcoming", "movie", "upcoming"),
            Triple("/movie/popular", "movie", "popularMovies"), Triple("/tv/popular", "tv", "popularTv"),
            Triple("/tv/top_rated", "tv", "topTv"), Triple("/tv/on_the_air", "tv", "onAir")
        )
        return defs.mapNotNull { (p, k, n) ->
            val l = runCatching { api.listPath(p, k) }.getOrDefault(emptyList()).filter { it.posterPath != null }
            if (l.isEmpty()) null else SuggestionRow("list", 0, n, l)
        }
    }

    suspend fun interestRows(
        types: Map<String, Int>, fav: Set<Int>, learned: Map<Int, Int>, dismissed: Set<String>
    ): List<SuggestionRow> {
        val have = dao.titlesOnce().map { it.id }.toSet()
        fun ok(i: SearchItem): Boolean {
            if (i.posterPath == null || i.id in dismissed || i.id in have) return false
            val anime = 16 in i.genreIds && i.originalLanguage == "ja"
            return if (anime) (types["anime"] ?: 1) > 0 else (types[i.mediaType] ?: 1) > 0
        }
        fun score(i: SearchItem): Double {
            val anime = 16 in i.genreIds && i.originalLanguage == "ja"
            val w = if (anime) types["anime"] ?: 1 else types[i.mediaType] ?: 1
            val g = i.genreIds.count { it in fav } * 2 + i.genreIds.sumOf { (learned[it] ?: 0).coerceAtMost(10) } / 5.0
            return i.popularity / 100.0 + g + w
        }
        val out = ArrayList<SuggestionRow>()
        val genres = (fav.toList() + learned.entries.sortedByDescending { it.value }.map { it.key }).distinct().take(4)
        genres.forEach { g ->
            val m = if ((types["movie"] ?: 1) > 0) runCatching { api.discoverByGenreIds("movie", listOf(g), 1).items }.getOrDefault(emptyList()) else emptyList()
            val tv = if ((types["tv"] ?: 1) > 0) runCatching { api.discoverByGenreIds("tv", listOf(g), 1).items }.getOrDefault(emptyList()) else emptyList()
            val l = (m + tv).filter(::ok).distinctBy { it.id }.sortedByDescending(::score)
            if (l.isNotEmpty()) out.add(SuggestionRow("genre", g, "", l))
        }
        if ((types["anime"] ?: 1) > 0) {
            val a = runCatching { api.discover("anime", "", emptyList(), "popularity", 0, "", 1).items }.getOrDefault(emptyList())
                .filter(::ok).distinctBy { it.id }
            if (a.isNotEmpty()) out.add(SuggestionRow("anime", 0, "", a))
        }
        // News: new / upcoming releases that match the interests
        val news = extraRows().filter { it.refName in setOf("nowPlaying", "upcoming", "onAir") }
            .flatMap { it.items }.filter(::ok).distinctBy { it.id }
        val picked = if (fav.isEmpty() && learned.isEmpty()) news else news.filter { i ->
            i.genreIds.any { it in fav || (learned[it] ?: 0) >= 3 }
        }.ifEmpty { news }
        val sorted = picked.sortedByDescending(::score)
        if (sorted.isNotEmpty()) out.add(0, SuggestionRow("list", 0, "newsForYou", sorted))
        if ((types["anime"] ?: 1) == 2) out.sortBy { if (it.kind == "anime") 0 else 1 }
        return out
    }

    suspend fun similar(type: String, tmdbId: Int): List<SearchItem> =
        runCatching { api.similar(type, tmdbId, 1).items.filter { it.posterPath != null } }.getOrDefault(emptyList())

    suspend fun suggestionRows(dismissed: Set<String>, full: Boolean): List<SuggestionRow> {
        val all = dao.titlesOnce()
        val watchedTv = dao.allEpisodesOnce().filter { it.watched }.map { it.titleId }.toSet()
        val watched = all.filter { it.movieWatched || it.id in watchedTv }.map { it.id }.toSet()
        return buildSuggestionRows(api, all, watched, dismissed, full)
    }

    suspend fun personDetails(id: Int): PersonDetails = api.person(id)

    /** Saves the title (and all episodes for a series). Returns its id. */
    suspend fun add(item: SearchItem, watchlist: Boolean): String {
        val id = item.id
        if (dao.count(id) > 0) return id
        val d = api.details(item.mediaType, item.tmdbId)
        val base = TitleEntity(
            id = id, tmdbId = item.tmdbId, mediaType = item.mediaType, name = item.name,
            year = item.year, posterPath = item.posterPath,
            overview = d.overview.ifBlank { item.overview },
            inWatchlist = watchlist,
            rating = d.rating.takeIf { it > 0 } ?: item.rating,
            genreIds = d.genreIds.ifBlank { item.genreIds.joinToString(",") },
            runtime = d.runtime, castNames = d.castNames, castJson = d.cast.toJson(),
            backdropPath = d.backdropPath,
            originalLang = d.originalLanguage.ifBlank { item.originalLanguage }
        )
        if (item.mediaType == "movie") {
            dao.insertTitle(base)
        } else {
            val eps = api.fetchEpisodes(item.tmdbId, id, notified = true)
            dao.addTitleWithEpisodes(base, eps)
        }
        return id
    }

    /** Opens a title from search: if it is not saved yet, fetch it from TMDB and save it (not in watchlist). */
    suspend fun fetchAndSave(id: String): TitleEntity? {
        dao.titleOnce(id)?.let { return it }
        val m = Regex("^(movie|tv)_(\\d+)$").find(id) ?: return null
        val type = m.groupValues[1]
        val tmdbId = m.groupValues[2].toInt()
        val d = api.details(type, tmdbId)
        val name = d.name.ifBlank { return null }
        val base = TitleEntity(
            id = id, tmdbId = tmdbId, mediaType = type, name = name,
            year = d.year, posterPath = d.posterPath, overview = d.overview,
            rating = d.rating, genreIds = d.genreIds, runtime = d.runtime,
            castNames = d.castNames, castJson = d.cast.toJson(),
            backdropPath = d.backdropPath, originalLang = d.originalLanguage
        )
        if (type == "movie") dao.insertTitle(base)
        else dao.addTitleWithEpisodes(base, api.fetchEpisodes(tmdbId, id, notified = false))
        return base
    }

    /** Titles saved by older versions lack cast / original language; fill them in when first opened. */
    suspend fun backfillDetails(id: String) {
        val t = dao.titleOnce(id) ?: return
        if (t.castJson.isNotBlank() && t.originalLang.isNotBlank()) return
        val d = api.details(t.mediaType, t.tmdbId)
        dao.updateTitle(
            t.copy(
                castNames = d.castNames.ifBlank { t.castNames },
                castJson = if (t.castJson.isNotBlank()) t.castJson else d.cast.toJson(),
                backdropPath = t.backdropPath.ifBlank { d.backdropPath },
                runtime = if (t.runtime > 0) t.runtime else d.runtime,
                genreIds = t.genreIds.ifBlank { d.genreIds },
                originalLang = t.originalLang.ifBlank { d.originalLanguage }
            )
        )
    }

    suspend fun delete(id: String) = dao.deleteTitle(id)

    suspend fun setMovieWatched(id: String, w: Boolean) {
        dao.setMovieWatched(id, w)
        if (w) dao.setWatchlist(id, false)
    }

    suspend fun setEpisodeWatched(ep: EpisodeEntity, w: Boolean) {
        dao.setEpisodeWatched(ep.id, w)
        if (w) dao.setWatchlist(ep.titleId, false)
    }

    suspend fun setSeasonWatched(id: String, season: Int, w: Boolean) {
        dao.setSeasonWatched(id, season, w)
        if (w) dao.setWatchlist(id, false)
    }

    suspend fun markUpTo(id: String, season: Int, ep: Int) {
        dao.markUpTo(id, season, ep)
        dao.setWatchlist(id, false)
    }

    suspend fun setWatchlist(id: String, w: Boolean) = dao.setWatchlist(id, w)

    /** Adds new episodes / refreshes names and dates, keeping watched marks. Returns number of new episodes. */
    suspend fun refreshSeries(id: String): Int {
        val t = dao.titleOnce(id) ?: return 0
        if (t.mediaType != "tv") return 0
        val fresh = api.fetchEpisodes(t.tmdbId, id, notified = false)
        val old = dao.episodesOnce(id).associateBy { it.season to it.episode }
        val toInsert = fresh.filter { (it.season to it.episode) !in old }
        val toUpdate = fresh.mapNotNull { f ->
            old[f.season to f.episode]?.let { o ->
                if (o.name != f.name || o.airDate != f.airDate || o.runtime != f.runtime)
                    o.copy(name = f.name, airDate = f.airDate, runtime = f.runtime) else null
            }
        }
        if (toInsert.isNotEmpty()) dao.insertEpisodes(toInsert)
        if (toUpdate.isNotEmpty()) dao.updateEpisodes(toUpdate)
        return toInsert.size
    }

    suspend fun refreshAll(): Int {
        var n = 0
        for (t in dao.tvTitlesOnce()) n += refreshSeries(t.id)
        return n
    }

    suspend fun pendingNotifications(today: String): List<Pair<String, List<EpisodeEntity>>> {
        val pending = dao.pendingNotifications(today)
        if (pending.isEmpty()) return emptyList()
        val names = dao.titlesOnce().associate { it.id to it.name }
        return pending.groupBy { it.titleId }.map { (id, list) -> (names[id] ?: "") to list }
    }

    suspend fun markNotified(list: List<EpisodeEntity>) {
        list.map { it.id }.chunked(500).forEach { dao.markNotified(it) }
    }

    // ---- playlists (custom ones only; the home chips All/Movie/Series/Anime are built in)

    val playlists: Flow<List<PlaylistEntity>> = dao.playlists()
    val allPlaylistItems: Flow<List<PlaylistItemEntity>> = dao.allPlaylistItems()
    fun playlist(id: Long): Flow<PlaylistEntity?> = dao.playlist(id)
    fun playlistItemIds(playlistId: Long): Flow<List<PlaylistItemEntity>> = dao.playlistItems(playlistId)
    fun playlistIdsFor(titleId: String): Flow<List<PlaylistItemEntity>> = dao.playlistsForTitle(titleId)

    suspend fun createPlaylist(name: String, color: Int, icon: String): Long =
        dao.insertPlaylist(PlaylistEntity(name = name, color = color, icon = icon))

    suspend fun renamePlaylist(p: PlaylistEntity, name: String, color: Int, icon: String) =
        dao.updatePlaylist(p.copy(name = name, color = color, icon = icon))

    suspend fun deletePlaylist(id: Long) = dao.deletePlaylist(id)

    suspend fun addToPlaylist(playlistId: Long, titleId: String) =
        dao.addToPlaylist(PlaylistItemEntity(playlistId, titleId, System.currentTimeMillis()))

    suspend fun removeFromPlaylist(playlistId: Long, titleId: String) = dao.removeFromPlaylist(playlistId, titleId)

    /** [items] must be in display order. Swaps the position values of two neighbours. */
    suspend fun movePlaylistItem(items: List<PlaylistItemEntity>, titleId: String, up: Boolean) {
        val idx = items.indexOfFirst { it.titleId == titleId }
        val swap = if (up) idx - 1 else idx + 1
        if (idx < 0 || swap < 0 || swap >= items.size) return
        val a = items[idx]
        val b = items[swap]
        dao.updatePlaylistItems(listOf(a.copy(position = b.position), b.copy(position = a.position)))
    }

    // ---- subtitles

    suspend fun searchSubtitles(tmdbId: Int, isTv: Boolean, season: Int?, episode: Int?, langs: Set<String>) =
        subs.search(tmdbId, isTv, season, episode, langs)

    suspend fun downloadSubtitle(url: String, season: Int?, episode: Int?) = subs.download(url, season, episode)

    // ---- backup

    suspend fun exportJson(): String = Backup.toJson(
        BackupData(dao.titlesOnce(), dao.allEpisodesOnce(), dao.playlistsOnce(), dao.allPlaylistItemsOnce())
    )

    suspend fun importJson(json: String) {
        val d = Backup.fromJson(json)
        val titleIds = d.titles.map { it.id }.toSet()
        val playlistIds = d.playlists.map { it.id }.toSet()
        dao.replaceAllWithPlaylists(
            d.titles,
            d.episodes.filter { it.titleId in titleIds },
            d.playlists,
            d.playlistItems.filter { it.titleId in titleIds && it.playlistId in playlistIds }
        )
    }
}
