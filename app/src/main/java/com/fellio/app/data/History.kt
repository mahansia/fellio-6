package com.fellio.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Lightweight viewing history (most recent first), kept in SharedPreferences. */
class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)
    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<SearchItem>> = _items

    private fun load(): List<SearchItem> = runCatching {
        val arr = JSONArray(prefs.getString("h", "[]"))
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            SearchItem(
                o.getInt("t"), o.getString("m"), o.getString("n"), o.optString("y"),
                o.optString("p").ifBlank { null }, "", 0.0, 0.0, emptyList()
            )
        }
    }.getOrDefault(emptyList())

    private fun save(l: List<SearchItem>) {
        val arr = JSONArray()
        l.forEach {
            arr.put(JSONObject().put("t", it.tmdbId).put("m", it.mediaType).put("n", it.name)
                .put("y", it.year).put("p", it.posterPath ?: ""))
        }
        prefs.edit().putString("h", arr.toString()).apply()
        _items.value = l
    }

    fun record(item: SearchItem) {
        save((listOf(item) + _items.value.filter { it.id != item.id }).take(30))
    }

    fun clear() = save(emptyList())
}
