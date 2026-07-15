package com.floogles.dailyquote

/**
 * A single quote entry. [author] is optional and may be blank.
 */
data class Quote(
    val id: String,
    val text: String,
    val author: String
)
