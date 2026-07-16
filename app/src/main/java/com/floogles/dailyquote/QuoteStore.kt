package com.floogles.dailyquote

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

/**
 * Simple SharedPreferences-backed store for quotes, serialized as a JSON array.
 * No database needed for a small personal list.
 */
object QuoteStore {

    private const val PREFS = "quotes_prefs"
    private const val KEY_QUOTES = "quotes_json"
    private const val KEY_SEEDED = "seeded"

    /**
     * Quotes added the very first time the app runs, so it isn't empty on day
     * one. They're ordinary quotes — you can edit or delete any of them, and
     * they are never re-added once the initial seed has happened.
     */
    private val STARTER_QUOTES = listOf(
        "The only way to do great work is to love what you do." to "Steve Jobs",
        "In the middle of difficulty lies opportunity." to "Albert Einstein",
        "It always seems impossible until it's done." to "Nelson Mandela",
        "Whether you think you can or you think you can't, you're right." to "Henry Ford",
        "The best way to predict the future is to create it." to "Peter Drucker",
        "Do what you can, with what you have, where you are." to "Theodore Roosevelt",
        "Happiness is not something ready made. It comes from your own actions." to "Dalai Lama",
        "Fall seven times, stand up eight." to "Japanese proverb"
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun ensureSeeded(context: Context) {
        val p = prefs(context)
        if (p.getBoolean(KEY_SEEDED, false)) return
        if (p.getString(KEY_QUOTES, null) == null) {
            val seeded = STARTER_QUOTES.map { (text, author) ->
                Quote(UUID.randomUUID().toString(), text, author)
            }
            saveAll(context, seeded)
        }
        p.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    fun getAll(context: Context): MutableList<Quote> {
        ensureSeeded(context)
        val raw = prefs(context).getString(KEY_QUOTES, null) ?: return mutableListOf()
        val list = mutableListOf<Quote>()
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Quote(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    text = o.optString("text", ""),
                    author = o.optString("author", "")
                )
            )
        }
        return list
    }

    private fun saveAll(context: Context, quotes: List<Quote>) {
        val arr = JSONArray()
        for (q in quotes) {
            val o = JSONObject()
            o.put("id", q.id)
            o.put("text", q.text)
            o.put("author", q.author)
            arr.put(o)
        }
        prefs(context).edit().putString(KEY_QUOTES, arr.toString()).apply()
    }

    fun get(context: Context, id: String): Quote? =
        getAll(context).firstOrNull { it.id == id }

    /** Adds a new quote and returns its generated id. */
    fun add(context: Context, text: String, author: String): String {
        val list = getAll(context)
        val id = UUID.randomUUID().toString()
        list.add(Quote(id, text.trim(), author.trim()))
        saveAll(context, list)
        return id
    }

    fun update(context: Context, id: String, text: String, author: String) {
        val list = getAll(context)
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            list[idx] = Quote(id, text.trim(), author.trim())
            saveAll(context, list)
        }
    }

    fun delete(context: Context, id: String) {
        val list = getAll(context)
        list.removeAll { it.id == id }
        saveAll(context, list)
    }

    /**
     * Deterministically picks one quote for the given day. The result is stable
     * for the whole day (so the widget and notification agree) and is guaranteed
     * to differ from the previous day's pick when more than one quote exists.
     */
    fun quoteOfDay(context: Context, date: LocalDate = LocalDate.now()): Quote? {
        val list = getAll(context)
        if (list.isEmpty()) return null
        if (list.size == 1) return list[0]

        val epochDay = date.toEpochDay()
        var todayIdx = Random(epochDay).nextInt(list.size)
        val yesterdayIdx = Random(epochDay - 1).nextInt(list.size)
        if (todayIdx == yesterdayIdx) {
            todayIdx = (todayIdx + 1) % list.size
        }
        return list[todayIdx]
    }
}
