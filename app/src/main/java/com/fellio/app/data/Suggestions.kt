package com.fellio.app.data

/**
 * One row of suggestions. kind: similar | genre | trending | top | anime.
 * similar -> refName = the title it is based on; genre -> refId = TMDB genre id.
 */
data class SuggestionRow(val kind: String, val refId: Int, val refName: String, val items: List<SearchItem>)

/** Builds suggestion rows from the user's library. [full] = the Discover page (more rows). */
suspend fun buildSuggestionRows(
    api: TmdbApi,
    titles: List<TitleEntity>,
    watchedIds: Set<String>,
    dismissed: Set<String>,
    full: Boolean
): List<SuggestionRow> {
    val existing = titles.map { it.id }.toSet()
    fun clean(items: List<SearchItem>) =
        items.filter { it.id !in existing && it.id !in dismissed && it.posterPath != null }.distinctBy { it.id }

    val basis = titles.filter { it.id in watchedIds }.ifEmpty { titles }
    val genreCount = HashMap<Int, Int>()
    basis.forEach { t ->
        t.genreIds.split(",").mapNotNull { it.toIntOrNull() }
            .forEach { g -> genreCount[g] = (genreCount[g] ?: 0) + 1 }
    }
    val topGenres = genreCount.entries.sortedByDescending { it.value }.map { it.key }.take(if (full) 3 else 1)
    val recent = basis.sortedByDescending { it.addedAt }.take(if (full) 2 else 1)

    val rows = ArrayList<SuggestionRow>()
    recent.forEach { t ->
        val items = clean(runCatching { api.similar(t.mediaType, t.tmdbId, 1).items }.getOrDefault(emptyList()))
        if (items.isNotEmpty()) rows.add(SuggestionRow("similar", 0, t.name, items))
    }
    topGenres.forEach { g ->
        val movies = runCatching { api.discoverByGenreIds("movie", listOf(g), 1).items }.getOrDefault(emptyList())
        val tv = runCatching { api.discoverByGenreIds("tv", listOf(g), 1).items }.getOrDefault(emptyList())
        val items = clean((movies + tv).sortedByDescending { it.popularity })
        if (items.isNotEmpty()) rows.add(SuggestionRow("genre", g, "", items))
    }
    val trending = clean(runCatching { api.trending(1).items }.getOrDefault(emptyList()))
    if (trending.isNotEmpty()) rows.add(SuggestionRow("trending", 0, "", trending))
    if (full) {
        val top = clean(runCatching { api.discover("all", "", emptyList(), "rating", 0, "", 1).items }.getOrDefault(emptyList()))
        if (top.isNotEmpty()) rows.add(SuggestionRow("top", 0, "", top))
        val anime = clean(runCatching { api.discover("anime", "", emptyList(), "popularity", 0, "", 1).items }.getOrDefault(emptyList()))
        if (anime.isNotEmpty()) rows.add(SuggestionRow("anime", 0, "", anime))
    }
    return if (full) rows else rows.take(2)
}
