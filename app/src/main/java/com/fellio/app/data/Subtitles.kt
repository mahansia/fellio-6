package com.fellio.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.ZipInputStream

enum class SubsError { NO_KEY, BAD_KEY, LIMIT, SERVER, NETWORK, NOT_FOUND }
class SubsException(val error: SubsError) : Exception(error.name)

data class SubtitleResult(
    val url: String,
    val language: String,
    val release: String,
    val hearingImpaired: Boolean,
    val author: String
)

/** SubDL REST API (api.subdl.com). Key comes from the app default or the user's own key in Settings. */
class SubtitlesApi(private val keyProvider: () -> String) {

    private fun fetchJson(key: String, query: Map<String, String>): JSONObject {
        val q = (mapOf("api_key" to key) + query).entries
            .joinToString("&") { (k, v) -> "$k=" + URLEncoder.encode(v, "UTF-8") }
        val conn = URL("https://api.subdl.com/api/v1/subtitles?$q").openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 20_000
            conn.setRequestProperty("Accept", "application/json")
            val code = conn.responseCode
            if (code == 401 || code == 403) throw SubsException(SubsError.BAD_KEY)
            if (code == 429) throw SubsException(SubsError.LIMIT)
            val stream = if (code in 200..299) conn.inputStream else (conn.errorStream ?: throw SubsException(SubsError.SERVER))
            val text = stream.bufferedReader().use { it.readText() }
            val json = try { JSONObject(text) } catch (e: Exception) { throw SubsException(SubsError.SERVER) }
            if (code !in 200..299 && !json.has("status")) throw SubsException(SubsError.SERVER)
            return json
        } catch (e: SubsException) {
            throw e
        } catch (e: IOException) {
            throw SubsException(SubsError.NETWORK)
        } finally {
            conn.disconnect()
        }
    }

    private fun validate(json: JSONObject) {
        if (json.optBoolean("status", true)) return
        val msg = (json.optString("error") + " " + json.optString("message")).lowercase()
        when {
            "limit" in msg || "quota" in msg || "too many" in msg -> throw SubsException(SubsError.LIMIT)
            "key" in msg || "unauthor" in msg || "api" in msg -> throw SubsException(SubsError.BAD_KEY)
            "not found" in msg || "no subtitle" in msg || "no result" in msg -> return
            else -> throw SubsException(SubsError.SERVER)
        }
    }

    suspend fun search(tmdbId: Int, isTv: Boolean, season: Int?, episode: Int?, langs: Set<String>): List<SubtitleResult> =
        withContext(Dispatchers.IO) {
            if (langs.isEmpty()) return@withContext emptyList()
            val q = mutableMapOf(
                "tmdb_id" to tmdbId.toString(),
                "type" to (if (isTv) "tv" else "movie"),
                "languages" to langs.joinToString(",") { it.uppercase() },
                "subs_per_page" to "30"
            )
            if (isTv) {
                season?.let { q["season_number"] = it.toString() }
                episode?.let { q["episode_number"] = it.toString() }
            }
            var key = keyProvider().trim()
            if (key.isEmpty()) throw SubsException(SubsError.NO_KEY)
            val json = try {
                fetchJson(key, q).also { validate(it) }
            } catch (e: SubsException) {
                // Some SubDL keys are shown with a "subdl_" label that is not part of the key itself.
                if (e.error == SubsError.BAD_KEY && key.startsWith("subdl_")) {
                    key = key.removePrefix("subdl_")
                    fetchJson(key, q).also { validate(it) }
                } else throw e
            }
            val arr = json.optJSONArray("subtitles") ?: return@withContext emptyList()
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val url = o.optString("url")
                if (url.isBlank()) return@mapNotNull null
                SubtitleResult(
                    url = url,
                    language = o.optString("language", o.optString("lang", "?")).uppercase(),
                    release = o.optString("release_name", o.optString("name", "")),
                    hearingImpaired = o.optBoolean("hi", false),
                    author = o.optString("author", "")
                )
            }
        }

    /** Returns (fileName, subtitle bytes). SubDL serves zip files; the best subtitle inside is extracted. */
    suspend fun download(url: String, season: Int?, episode: Int?): Pair<String, ByteArray> =
        withContext(Dispatchers.IO) {
            val full = if (url.startsWith("http")) url else "https://dl.subdl.com" + (if (url.startsWith("/")) url else "/$url")
            val conn = URL(full).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 10_000
                conn.readTimeout = 30_000
                val code = conn.responseCode
                if (code == 429) throw SubsException(SubsError.LIMIT)
                if (code !in 200..299) throw SubsException(SubsError.NOT_FOUND)
                unzip(conn.inputStream.use { it.readBytes() }, season, episode)
            } catch (e: SubsException) {
                throw e
            } catch (e: IOException) {
                throw SubsException(SubsError.NETWORK)
            } finally {
                conn.disconnect()
            }
        }

    private fun unzip(bytes: ByteArray, season: Int?, episode: Int?): Pair<String, ByteArray> {
        val isZip = bytes.size > 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()
        if (!isZip) return "subtitle.srt" to bytes
        val exts = setOf("srt", "ass", "ssa", "vtt", "sub")
        val entries = ArrayList<Pair<String, ByteArray>>()
        ZipInputStream(bytes.inputStream()).use { zin ->
            var e = zin.nextEntry
            while (e != null) {
                if (!e.isDirectory) {
                    val n = e.name.substringAfterLast('/')
                    if (n.substringAfterLast('.', "").lowercase() in exts) entries.add(n to zin.readBytes())
                }
                e = zin.nextEntry
            }
        }
        if (entries.isEmpty()) throw SubsException(SubsError.NOT_FOUND)
        val match = if (season != null && episode != null) entries.firstOrNull { matchesEpisode(it.first, season, episode) } else null
        return match ?: entries.firstOrNull { it.first.lowercase().endsWith(".srt") } ?: entries.first()
    }

    private fun matchesEpisode(name: String, s: Int, e: Int): Boolean {
        val n = name.lowercase()
        return Regex("s0*$s[ ._-]*e0*$e(?!\\d)").containsMatchIn(n) ||
            Regex("(?<!\\d)0*${s}x0*$e(?!\\d)").containsMatchIn(n)
    }
}

/** Subtitle languages offered in Settings: (code, native label). */
object SubtitleLangs {
    val options: List<Pair<String, String>> = listOf(
        "fa" to "فارسی", "en" to "English", "tr" to "Türkçe", "ar" to "العربية"
    )
}
