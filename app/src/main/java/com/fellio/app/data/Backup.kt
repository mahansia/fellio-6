package com.fellio.app.data

import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val titles: List<TitleEntity>,
    val episodes: List<EpisodeEntity>,
    val playlists: List<PlaylistEntity>,
    val playlistItems: List<PlaylistItemEntity>
)

object Backup {
    fun toJson(d: BackupData): String {
        val root = JSONObject()
        root.put("app", "didam")
        root.put("version", 4)
        val ta = JSONArray()
        d.titles.forEach { t ->
            ta.put(JSONObject().apply {
                put("id", t.id); put("tmdbId", t.tmdbId); put("mediaType", t.mediaType)
                put("name", t.name); put("year", t.year); put("posterPath", t.posterPath ?: "")
                put("overview", t.overview); put("movieWatched", t.movieWatched); put("addedAt", t.addedAt)
                put("inWatchlist", t.inWatchlist); put("rating", t.rating); put("genreIds", t.genreIds)
                put("runtime", t.runtime); put("castNames", t.castNames); put("backdropPath", t.backdropPath)
                put("castJson", t.castJson); put("originalLang", t.originalLang)
            })
        }
        val ea = JSONArray()
        d.episodes.forEach { e ->
            ea.put(JSONObject().apply {
                put("titleId", e.titleId); put("season", e.season); put("episode", e.episode)
                put("name", e.name); put("airDate", e.airDate); put("watched", e.watched)
                put("runtime", e.runtime); put("notified", e.notified)
            })
        }
        val pa = JSONArray()
        d.playlists.forEach { p ->
            pa.put(JSONObject().apply {
                put("id", p.id); put("name", p.name); put("color", p.color); put("icon", p.icon)
                put("createdAt", p.createdAt); put("sortOrder", p.sortOrder)
            })
        }
        val ia = JSONArray()
        d.playlistItems.forEach { i ->
            ia.put(JSONObject().apply {
                put("playlistId", i.playlistId); put("titleId", i.titleId); put("position", i.position)
            })
        }
        root.put("titles", ta)
        root.put("episodes", ea)
        root.put("playlists", pa)
        root.put("playlistItems", ia)
        return root.toString()
    }

    fun fromJson(s: String): BackupData {
        val root = JSONObject(s)
        require(root.optString("app") == "didam") { "not a Didam backup" }
        val ta = root.getJSONArray("titles")
        val titles = (0 until ta.length()).map { i ->
            val o = ta.getJSONObject(i)
            TitleEntity(
                id = o.getString("id"),
                tmdbId = o.getInt("tmdbId"),
                mediaType = o.getString("mediaType"),
                name = o.optString("name"),
                year = o.optString("year"),
                posterPath = o.optString("posterPath").ifBlank { null },
                overview = o.optString("overview"),
                movieWatched = o.optBoolean("movieWatched", false),
                addedAt = o.optLong("addedAt", System.currentTimeMillis()),
                inWatchlist = o.optBoolean("inWatchlist", false),
                rating = o.optDouble("rating", 0.0),
                genreIds = o.optString("genreIds"),
                runtime = o.optInt("runtime", 0),
                castNames = o.optString("castNames"),
                backdropPath = o.optString("backdropPath"),
                castJson = o.optString("castJson"),
                originalLang = o.optString("originalLang")
            )
        }
        val ea = root.getJSONArray("episodes")
        val eps = (0 until ea.length()).map { i ->
            val o = ea.getJSONObject(i)
            EpisodeEntity(
                titleId = o.getString("titleId"),
                season = o.getInt("season"),
                episode = o.getInt("episode"),
                name = o.optString("name"),
                airDate = o.optString("airDate"),
                watched = o.optBoolean("watched", false),
                runtime = o.optInt("runtime", 0),
                notified = o.optBoolean("notified", true)
            )
        }
        val pa = root.optJSONArray("playlists")
        val playlists = (0 until (pa?.length() ?: 0)).map { i ->
            val o = pa!!.getJSONObject(i)
            PlaylistEntity(
                id = o.getLong("id"), name = o.optString("name"), color = o.optInt("color"),
                icon = o.optString("icon"), createdAt = o.optLong("createdAt"), sortOrder = o.optLong("sortOrder")
            )
        }
        val ia = root.optJSONArray("playlistItems")
        val items = (0 until (ia?.length() ?: 0)).map { i ->
            val o = ia!!.getJSONObject(i)
            PlaylistItemEntity(o.getLong("playlistId"), o.getString("titleId"), o.optLong("position"))
        }
        return BackupData(titles, eps, playlists, items)
    }
}
