package com.fellio.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppSettings(ctx: Context, deviceLangCode: String) {
    private val sp = ctx.getSharedPreferences("didam_settings", Context.MODE_PRIVATE)

    private fun list(key: String): List<String> =
        (sp.getString(key, "") ?: "").split(",").filter { it.isNotBlank() }

    private val _customKey = MutableStateFlow(sp.getString("api_key", "") ?: "")
    val customKey: StateFlow<String> = _customKey
    val effectiveKey: String get() = _customKey.value.ifBlank { Config.DEFAULT_TMDB_KEY }

    private val _customSubsKey = MutableStateFlow(sp.getString("subs_key", "") ?: "")
    val customSubsKey: StateFlow<String> = _customSubsKey
    val effectiveSubsKey: String get() = _customSubsKey.value.ifBlank { Config.DEFAULT_SUBDL_KEY }

    private val _accent = MutableStateFlow(sp.getInt("accent", DEFAULT_ACCENT))
    val accent: StateFlow<Int> = _accent

    // First run: use the phone's language when supported, otherwise English.
    private val savedLang = sp.getString("lang", null)
    private val _lang = MutableStateFlow(Lang.fromCode(savedLang ?: Lang.fromDevice(deviceLangCode).code))
    val lang: StateFlow<Lang> = _lang

    private val allowedSubLangs = SubtitleLangs.options.map { it.first }.toSet()
    private val _subtitleLangs = MutableStateFlow(
        (sp.getString("subtitle_langs", null) ?: "fa,en").split(",")
            .filter { it in allowedSubLangs }.toSet().ifEmpty { setOf("fa", "en") }
    )
    val subtitleLangs: StateFlow<Set<String>> = _subtitleLangs

    private val _theme = MutableStateFlow(sp.getString("theme", "dark") ?: "dark")
    val theme: StateFlow<String> = _theme

    private val _jalali = MutableStateFlow(sp.getBoolean("jalali", true))
    val jalali: StateFlow<Boolean> = _jalali

    private val _grid = MutableStateFlow(sp.getBoolean("grid", false))
    val grid: StateFlow<Boolean> = _grid

    private val _notifications = MutableStateFlow(sp.getBoolean("notifications", false))
    val notifications: StateFlow<Boolean> = _notifications

    // Home chips: order + hidden ones (ids: all, movie, series, anime, pl:<playlistId>)
    private val _collectionOrder = MutableStateFlow(list("collections_order"))
    val collectionOrder: StateFlow<List<String>> = _collectionOrder
    private val _collectionHidden = MutableStateFlow(list("collections_hidden").toSet())
    val collectionHidden: StateFlow<Set<String>> = _collectionHidden

    // Suggestions the user pressed "not interested" on (title ids)
    private val _dismissed = MutableStateFlow(list("dismissed").toSet())
    val dismissed: StateFlow<Set<String>> = _dismissed

    // ---- Discover interests
    private fun ints(key: String) = list(key).mapNotNull { it.toIntOrNull() }
    /** importance per kind: 0 = off, 1 = normal, 2 = high */
    private fun readTypes(): Map<String, Int> {
        val m = mutableMapOf("movie" to 1, "tv" to 1, "anime" to 1)
        list("pref_types").forEach { e -> val p = e.split(":"); if (p.size == 2) p[1].toIntOrNull()?.let { m[p[0]] = it } }
        return m
    }
    private val _prefTypes = MutableStateFlow(readTypes())
    val prefTypes: StateFlow<Map<String, Int>> = _prefTypes
    private val _favGenres = MutableStateFlow(ints("pref_genres").toSet())
    val favGenres: StateFlow<Set<Int>> = _favGenres
    private val _interestsSet = MutableStateFlow(sp.getBoolean("interests_set", false))
    val interestsSet: StateFlow<Boolean> = _interestsSet
    fun setInterests(types: Map<String, Int>, genres: Set<Int>) {
        sp.edit().putString("pref_types", types.entries.joinToString(",") { "${it.key}:${it.value}" })
            .putString("pref_genres", genres.joinToString(",")).putBoolean("interests_set", true).apply()
        _prefTypes.value = types; _favGenres.value = genres; _interestsSet.value = true
    }
    /** Genres learned from what the user opens / adds: genreId -> weight */
    fun learnedGenres(): Map<Int, Int> = list("learned").mapNotNull { e ->
        val p = e.split(":"); if (p.size == 2) (p[0].toIntOrNull() ?: return@mapNotNull null) to (p[1].toIntOrNull() ?: 0) else null
    }.toMap()
    fun learn(genres: List<Int>, w: Int) {
        if (genres.isEmpty()) return
        val m = learnedGenres().toMutableMap()
        genres.forEach { m[it] = (m[it] ?: 0) + w }
        val top = m.entries.sortedByDescending { it.value }.take(30)
        sp.edit().putString("learned", top.joinToString(",") { "${it.key}:${it.value}" }).apply()
    }

    init {
        if (savedLang == null) sp.edit().putString("lang", _lang.value.code).apply()
    }

    fun setCustomKey(k: String) { val v = k.trim(); sp.edit().putString("api_key", v).apply(); _customKey.value = v }
    fun setCustomSubsKey(k: String) { val v = k.trim(); sp.edit().putString("subs_key", v).apply(); _customSubsKey.value = v }
    fun setAccent(c: Int) { sp.edit().putInt("accent", c).apply(); _accent.value = c }
    private val _titleMode = MutableStateFlow(sp.getString("title_mode", "local") ?: "local")
    val titleMode: StateFlow<String> = _titleMode
    fun setTitleMode(m: String) { sp.edit().putString("title_mode", m).apply(); _titleMode.value = m }
    private val _titleParen = MutableStateFlow(sp.getString("title_paren", "local") ?: "local")
    val titleParen: StateFlow<String> = _titleParen
    fun setTitleParen(m: String) { sp.edit().putString("title_paren", m).apply(); _titleParen.value = m }
    private val _posterLang = MutableStateFlow(sp.getString("poster_lang", "auto") ?: "auto")
    val posterLang: StateFlow<String> = _posterLang
    fun setPosterLang(m: String) { sp.edit().putString("poster_lang", m).apply(); _posterLang.value = m }
    fun setLang(l: Lang) { sp.edit().putString("lang", l.code).apply(); _lang.value = l }
    fun setSubtitleLangs(s: Set<String>) {
        sp.edit().putString("subtitle_langs", s.joinToString(",")).apply()
        _subtitleLangs.value = s
    }
    fun setTheme(t: String) { sp.edit().putString("theme", t).apply(); _theme.value = t }
    private val _fontScale = MutableStateFlow(sp.getFloat("font_scale", 1f))
    val fontScale: StateFlow<Float> = _fontScale
    fun setFontScale(f: Float) { sp.edit().putFloat("font_scale", f).apply(); _fontScale.value = f }
    fun setJalali(b: Boolean) { sp.edit().putBoolean("jalali", b).apply(); _jalali.value = b }
    fun setGrid(b: Boolean) { sp.edit().putBoolean("grid", b).apply(); _grid.value = b }
    fun setNotifications(b: Boolean) { sp.edit().putBoolean("notifications", b).apply(); _notifications.value = b }

    fun setCollectionOrder(l: List<String>) {
        sp.edit().putString("collections_order", l.joinToString(",")).apply()
        _collectionOrder.value = l
    }
    fun setCollectionHidden(s: Set<String>) {
        sp.edit().putString("collections_hidden", s.joinToString(",")).apply()
        _collectionHidden.value = s
    }
    fun addDismissed(id: String) {
        val l = (_dismissed.value + id).toList().takeLast(400)
        sp.edit().putString("dismissed", l.joinToString(",")).apply()
        _dismissed.value = l.toSet()
    }
    fun clearDismissed() {
        sp.edit().putString("dismissed", "").apply()
        _dismissed.value = emptySet()
    }

    companion object {
        val DEFAULT_ACCENT: Int = 0xFF5B7CFA.toInt()
    }
}
