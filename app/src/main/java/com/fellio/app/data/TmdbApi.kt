package com.fellio.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

enum class TmdbError { NO_KEY, BAD_KEY, SERVER, NETWORK }

class TmdbException(val error: TmdbError, val code: Int = 0) : Exception(error.name)

data class SearchItem(
    val tmdbId: Int,
    val mediaType: String,
    val name: String,
    val year: String,
    val posterPath: String?,
    val overview: String,
    val rating: Double,
    val popularity: Double,
    val genreIds: List<Int>,
    val originalLanguage: String = ""
) {
    val id: String get() = "${mediaType}_$tmdbId"
}

data class Page(val items: List<SearchItem>, val totalPages: Int)

data class Details(
    val overview: String,
    val rating: Double,
    val genreIds: String,
    val runtime: Int,
    val castNames: String,
    val cast: List<CastMember>,
    val backdropPath: String,
    val originalLanguage: String = "",
    val name: String = "",
    val year: String = "",
    val posterPath: String? = null
)

data class PersonDetails(
    val name: String,
    val bio: String,
    val profilePath: String?,
    val birthday: String,
    val placeOfBirth: String,
    val knownFor: String,
    val filmography: List<SearchItem>
)

private fun JSONObject.str(k: String): String = if (isNull(k)) "" else optString(k)

/** [langProvider] returns a TMDB locale like "fa-IR" or "en-US". */
class TmdbApi(
    private val keyProvider: () -> String,
    private val langProvider: () -> String,
    private val titleModeProvider: () -> String = { "local" },
    private val posterLangProvider: () -> String = { "auto" },
    private val parenProvider: () -> String = { "local" }
) {
    /** local | original | both */
    /** paren: none | local (original + (local)) | original (local + (original)) */
    private fun pickTitle(local: String, original: String): String {
        if (local.isBlank() || original.isBlank() || original.equals(local, true)) return local.ifBlank { original }
        return when (parenProvider()) {
            "none" -> local
            "original" -> "$local ($original)"
            else -> "$original ($local)"
        }
    }


    private val lang: String get() =
        titleModeProvider().let { m -> com.fellio.app.data.Lang.values().firstOrNull { it.code == m }?.tmdb } ?: langProvider()

    private fun get(path: String, params: Map<String, String> = emptyMap()): JSONObject {
        val key = keyProvider().trim()
        if (key.isEmpty()) throw TmdbException(TmdbError.NO_KEY)
        val bearer = key.length > 40 // v4 read-access token
        val parts = ArrayList<String>()
        if (!bearer) parts.add("api_key=" + URLEncoder.encode(key, "UTF-8"))
        params.forEach { (k, v) -> parts.add("$k=" + URLEncoder.encode(v, "UTF-8")) }
        val url = URL("https://api.themoviedb.org/3$path?" + parts.joinToString("&"))
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("Accept", "application/json")
            if (bearer) conn.setRequestProperty("Authorization", "Bearer $key")
            val code = conn.responseCode
            if (code == 401) throw TmdbException(TmdbError.BAD_KEY, code)
            if (code !in 200..299) throw TmdbException(TmdbError.SERVER, code)
            return JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        } catch (e: IOException) {
            throw TmdbException(TmdbError.NETWORK)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseItem(o: JSONObject, forced: String?): SearchItem? {
        val type = forced ?: o.str("media_type")
        if (type != "movie" && type != "tv") return null
        val movie = type == "movie"
        val g = o.optJSONArray("genre_ids")
        return SearchItem(
            tmdbId = o.getInt("id"),
            mediaType = type,
            name = pickTitle(o.str(if (movie) "title" else "name"), o.str(if (movie) "original_title" else "original_name")),
            year = o.str(if (movie) "release_date" else "first_air_date").take(4),
            posterPath = o.str("poster_path").ifBlank { null },
            overview = o.str("overview"),
            rating = o.optDouble("vote_average", 0.0),
            popularity = o.optDouble("popularity", 0.0),
            genreIds = (0 until (g?.length() ?: 0)).map { g!!.getInt(it) },
            originalLanguage = o.str("original_language")
        )
    }

    private fun parsePage(json: JSONObject, forced: String?): Page {
        val arr = json.optJSONArray("results")
        val items = (0 until (arr?.length() ?: 0)).mapNotNull { parseItem(arr!!.getJSONObject(it), forced) }
        return Page(items, json.optInt("total_pages", 1))
    }

    suspend fun search(query: String, type: String, year: String, page: Int): Page = withContext(Dispatchers.IO) {
        val p = mutableMapOf(
            "query" to query, "language" to lang, "include_adult" to "false", "page" to page.toString()
        )
        val path: String
        val forced: String?
        when (type) {
            "movie" -> { if (year.isNotBlank()) p["year"] = year; path = "/search/movie"; forced = "movie" }
            "tv" -> { if (year.isNotBlank()) p["first_air_date_year"] = year; path = "/search/tv"; forced = "tv" }
            else -> { path = "/search/multi"; forced = null }
        }
        val main = parsePage(get(path, p), forced)
        if (page == 1 && lang != "en-US") {
            // Bilingual search: also search in English and merge the results.
            val en = runCatching { parsePage(get(path, p + ("language" to "en-US")), forced) }.getOrNull()
            if (en != null) {
                val seen = main.items.map { it.id }.toSet()
                return@withContext Page(main.items + en.items.filter { it.id !in seen }, maxOf(main.totalPages, en.totalPages))
            }
        }
        main
    }

    suspend fun trending(page: Int): Page = withContext(Dispatchers.IO) {
        parsePage(get("/trending/all/week", mapOf("language" to lang, "page" to page.toString())), null)
    }

    /**
     * Filtered browse without a text query.
     * [category]: all | movie | tv | anime (anime = Japanese animation, movies and series together).
     * Selected [genres] must all match (AND). [sort]: relevance/popularity | rating | newest.
     */
    suspend fun discover(
        category: String, year: String, genres: List<Genres.Option>,
        sort: String, minRating: Int, origLang: String, page: Int
    ): Page = withContext(Dispatchers.IO) {
        val today = java.time.LocalDate.now().toString()
        fun one(kind: String): Page {
            val idLists = genres.map { if (kind == "movie") it.movie else it.tv }
            if (idLists.any { it.isEmpty() }) return Page(emptyList(), 0)
            val ids = idLists.flatten().distinct().toMutableList()
            if (category == "anime" && 16 !in ids) ids.add(0, 16)
            val p = mutableMapOf("language" to lang, "include_adult" to "false", "page" to page.toString())
            p["sort_by"] = when (sort) {
                "rating" -> "vote_average.desc"
                "newest" -> if (kind == "movie") "primary_release_date.desc" else "first_air_date.desc"
                else -> "popularity.desc"
            }
            if (sort == "rating") p["vote_count.gte"] = "300"
            if (sort == "newest") {
                p[if (kind == "movie") "primary_release_date.lte" else "first_air_date.lte"] = today
                p["vote_count.gte"] = "10"
            }
            if (minRating > 0) {
                p["vote_average.gte"] = minRating.toString()
                if ("vote_count.gte" !in p) p["vote_count.gte"] = "100"
            }
            if (ids.isNotEmpty()) p["with_genres"] = ids.joinToString(",")
            val ol = if (category == "anime") "ja" else origLang
            if (ol.isNotBlank()) p["with_original_language"] = ol
            if (year.isNotBlank()) p[if (kind == "movie") "primary_release_year" else "first_air_date_year"] = year
            return parsePage(get("/discover/$kind", p), kind)
        }
        val kinds = when (category) {
            "movie" -> listOf("movie")
            "tv" -> listOf("tv")
            else -> listOf("movie", "tv")
        }
        val pages = kinds.map { one(it) }
        if (pages.size == 1) {
            pages[0]
        } else {
            val all = pages.flatMap { it.items }
            val merged = when (sort) {
                "rating" -> all.sortedByDescending { it.rating }
                "newest" -> all.sortedByDescending { it.year }
                else -> all.sortedByDescending { it.popularity }
            }
            Page(merged, pages.maxOf { it.totalPages })
        }
    }

    /** discover by a fixed set of genre ids (used for home "for you" suggestions), excluding nothing itself. */
    suspend fun discoverByGenreIds(type: String, genreIds: List<Int>, page: Int): Page = withContext(Dispatchers.IO) {
        if (genreIds.isEmpty()) return@withContext Page(emptyList(), 0)
        val p = mapOf(
            "language" to lang, "include_adult" to "false", "page" to page.toString(),
            "sort_by" to "popularity.desc", "with_genres" to genreIds.joinToString(",")
        )
        parsePage(get("/discover/$type", p), type)
    }

    suspend fun similar(type: String, id: Int, page: Int): Page = withContext(Dispatchers.IO) {
        val r = runCatching { get("/$type/$id/recommendations", mapOf("language" to lang, "page" to page.toString())) }
            .getOrElse { get("/$type/$id/similar", mapOf("language" to lang, "page" to page.toString())) }
        parsePage(r, type)
    }

    suspend fun listPath(path: String, forced: String?): List<SearchItem> = withContext(Dispatchers.IO) {
        parsePage(get(path, mapOf("language" to lang, "page" to "1")), forced).items
    }

    /** YouTube key of the best trailer, or null. */
    suspend fun trailerKey(type: String, id: Int): String? = withContext(Dispatchers.IO) {
        fun pick(j: JSONObject): String? {
            val a = j.optJSONArray("results") ?: return null
            val l = (0 until a.length()).map { a.getJSONObject(it) }.filter { it.str("site") == "YouTube" }
            return (l.firstOrNull { it.str("type") == "Trailer" } ?: l.firstOrNull())?.str("key")
        }
        pick(get("/$type/$id/videos", mapOf("language" to lang)))
            ?: pick(get("/$type/$id/videos", mapOf("language" to "en-US")))
    }

    suspend fun details(type: String, id: Int): Details = withContext(Dispatchers.IO) {
        val j = get("/$type/$id", mapOf("language" to lang, "append_to_response" to "credits"))
        var overview = j.str("overview")
        if (overview.isBlank() && lang != "en-US") {
            overview = runCatching { get("/$type/$id", mapOf("language" to "en-US")).str("overview") }.getOrDefault("")
        }
        val g = j.optJSONArray("genres")
        val genreIds = (0 until (g?.length() ?: 0)).joinToString(",") { g!!.getJSONObject(it).getInt("id").toString() }
        val runtime = if (type == "movie") j.optInt("runtime", 0)
        else (j.optJSONArray("episode_run_time")?.let { if (it.length() > 0) it.optInt(0, 0) else 0 } ?: 0)
        val castArr = j.optJSONObject("credits")?.optJSONArray("cast")
        val castList = (0 until minOf(castArr?.length() ?: 0, 12)).map { i ->
            val c = castArr!!.getJSONObject(i)
            CastMember(c.getInt("id"), c.str("name"), c.str("profile_path").ifBlank { null })
        }
        val names = castList.take(8).joinToString(", ") { it.name }
        val name = pickTitle(j.str(if (type == "movie") "title" else "name"), j.str(if (type == "movie") "original_title" else "original_name"))
        val date = j.str(if (type == "movie") "release_date" else "first_air_date")
        var poster = j.str("poster_path").ifBlank { null }
        val pl = langProvider().take(2)
        if (pl.length == 2) runCatching {
            val code = pl
            val posters = get("/$type/$id/images", mapOf("include_image_language" to code)).optJSONArray("posters")
            if (posters != null) {
                var best: JSONObject? = null
                for (i in 0 until posters.length()) {
                    val po = posters.getJSONObject(i)
                    if (po.str("iso_639_1") == code && (best == null || po.optDouble("vote_average") > best.optDouble("vote_average"))) best = po
                }
                best?.str("file_path")?.takeIf { it.isNotBlank() }?.let { poster = it }
            }
        }
        Details(overview, j.optDouble("vote_average", 0.0), genreIds, runtime, names, castList, j.str("backdrop_path"), j.str("original_language"), name, date.take(4), poster)
    }

    suspend fun person(id: Int): PersonDetails = withContext(Dispatchers.IO) {
        val p = get("/person/$id", mapOf("language" to lang))
        var bio = runCatching { get("/person/$id", mapOf("language" to "en-US")).str("biography") }.getOrDefault("")
        if (bio.isBlank()) bio = p.str("biography")
        val credits = runCatching { get("/person/$id/combined_credits", mapOf("language" to lang)) }.getOrNull()
        val cast = credits?.optJSONArray("cast")
        val filmography = (0 until (cast?.length() ?: 0)).mapNotNull { i -> parseItem(cast!!.getJSONObject(i), null) }
            .distinctBy { it.id }
            .sortedByDescending { it.popularity }
        PersonDetails(
            name = p.str("name"), bio = bio, profilePath = p.str("profile_path").ifBlank { null },
            birthday = p.str("birthday"), placeOfBirth = p.str("place_of_birth"),
            knownFor = p.str("known_for_department"), filmography = filmography
        )
    }

    /** Fetches every season (skipping specials / season 0) and returns all episodes. */
    suspend fun fetchEpisodes(tvId: Int, titleId: String, notified: Boolean): List<EpisodeEntity> =
        withContext(Dispatchers.IO) {
            val details = get("/tv/$tvId", mapOf("language" to "en-US"))
            val seasons = details.optJSONArray("seasons")
            val numbers = (0 until (seasons?.length() ?: 0))
                .map { seasons!!.getJSONObject(it).getInt("season_number") }
                .filter { it > 0 }
            val responses = coroutineScope {
                numbers.map { n ->
                    async { n to get("/tv/$tvId/season/$n", mapOf("language" to "en-US")) }
                }.awaitAll()
            }
            responses.flatMap { (n, json) ->
                val arr = json.optJSONArray("episodes")
                (0 until (arr?.length() ?: 0)).map { i ->
                    val e = arr!!.getJSONObject(i)
                    EpisodeEntity(
                        titleId = titleId,
                        season = n,
                        episode = e.getInt("episode_number"),
                        name = e.str("name"),
                        airDate = e.str("air_date"),
                        runtime = e.optInt("runtime", 0),
                        notified = notified
                    )
                }
            }.sortedWith(compareBy({ it.season }, { it.episode }))
        }
}
