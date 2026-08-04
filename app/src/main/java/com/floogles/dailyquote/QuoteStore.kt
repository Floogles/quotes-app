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

    // Rotation state ("shuffled deck" of upcoming quote ids, the ids already
    // shown in the current cycle, and the pick locked in for the current day).
    private const val KEY_DECK = "deck_json"
    private const val KEY_SHOWN = "shown_json"
    private const val KEY_TODAY_ID = "today_id"
    private const val KEY_TODAY_EPOCH = "today_epoch"

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
        return parseQuotes(prefs(context).getString(KEY_QUOTES, null))
    }

    private fun parseQuotes(raw: String?): MutableList<Quote> {
        val list = mutableListOf<Quote>()
        if (raw.isNullOrBlank()) return list
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

    private fun quotesToJson(quotes: List<Quote>): JSONArray {
        val arr = JSONArray()
        for (q in quotes) {
            val o = JSONObject()
            o.put("id", q.id)
            o.put("text", q.text)
            o.put("author", q.author)
            arr.put(o)
        }
        return arr
    }

    private fun saveAll(context: Context, quotes: List<Quote>) {
        prefs(context).edit().putString(KEY_QUOTES, quotesToJson(quotes).toString()).apply()
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

    // ---- Backup / restore -------------------------------------------------

    /** Serializes all quotes to a JSON string suitable for a backup file. */
    fun exportToJson(context: Context): String =
        quotesToJson(getAll(context)).toString(2)

    /**
     * Merges quotes from a backup JSON string into the current list. Existing
     * quotes are kept; a quote is skipped if the same text + author is already
     * present, so re-importing the same file does nothing. Returns how many new
     * quotes were added.
     */
    fun importFromJson(context: Context, json: String): Int {
        val incoming = parseQuotes(json)
        val current = getAll(context)
        val existingIds = current.map { it.id }.toMutableSet()
        val existingKeys = current.map { dedupeKey(it.text, it.author) }.toMutableSet()

        var added = 0
        for (q in incoming) {
            if (q.text.isBlank()) continue
            val key = dedupeKey(q.text, q.author)
            if (key in existingKeys) continue
            var id = q.id
            if (id.isBlank() || id in existingIds) id = UUID.randomUUID().toString()
            current.add(Quote(id, q.text.trim(), q.author.trim()))
            existingIds.add(id)
            existingKeys.add(key)
            added++
        }
        if (added > 0) saveAll(context, current)
        return added
    }

    private fun dedupeKey(text: String, author: String) =
        (text.trim() + "" + author.trim()).lowercase()

    // ---- Quote of the day -------------------------------------------------

    /**
     * Returns the quote for the given day, choosing it from a shuffled "deck"
     * so that every quote is shown once before any repeats, in a random order.
     *
     * The pick is locked in per calendar day (so the widget and notification
     * always agree), and the deck is advanced only on the first call of a new
     * day. Newly added quotes are mixed into the remaining deck so they appear
     * soon; deleted quotes are dropped from it.
     */
    fun quoteOfDay(context: Context, date: LocalDate = LocalDate.now()): Quote? {
        val list = getAll(context)
        if (list.isEmpty()) return null

        val p = prefs(context)
        val todayEpoch = date.toEpochDay()
        val storedEpoch = p.getLong(KEY_TODAY_EPOCH, Long.MIN_VALUE)
        val storedId = p.getString(KEY_TODAY_ID, null)

        // Already decided today's quote and it still exists → keep it stable.
        if (storedEpoch == todayEpoch && storedId != null) {
            val existing = list.firstOrNull { it.id == storedId }
            if (existing != null) return existing
        }

        val ids = list.map { it.id }.toSet()
        val remaining = readIds(p, KEY_DECK).filterTo(mutableListOf()) { it in ids } // drop deleted
        val shown = readIds(p, KEY_SHOWN).filterTo(mutableSetOf()) { it in ids }

        // Mix in quotes that have never been shown and aren't already queued
        // (e.g. ones the user just added) so they appear during this cycle.
        val known = remaining.toSet() + shown
        for (fresh in ids.filter { it !in known }.shuffled()) {
            remaining.add(Random.nextInt(remaining.size + 1), fresh)
        }

        // Whole deck has been shown → start a fresh shuffled cycle.
        if (remaining.isEmpty()) {
            shown.clear()
            remaining.addAll(ids.shuffled())
            // Avoid repeating the previous day's quote across the cycle boundary.
            if (remaining.size > 1 && remaining[0] == storedId) {
                val first = remaining.removeAt(0)
                remaining.add(1 + Random.nextInt(remaining.size), first)
            }
        }

        val nextId = remaining.removeAt(0)
        shown.add(nextId)
        p.edit()
            .putString(KEY_DECK, JSONArray(remaining).toString())
            .putString(KEY_SHOWN, JSONArray(shown.toList()).toString())
            .putLong(KEY_TODAY_EPOCH, todayEpoch)
            .putString(KEY_TODAY_ID, nextId)
            .commit()

        return list.firstOrNull { it.id == nextId } ?: list.first()
    }

    private fun readIds(p: android.content.SharedPreferences, key: String): List<String> {
        val raw = p.getString(key, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getString(it) }
    }
}
