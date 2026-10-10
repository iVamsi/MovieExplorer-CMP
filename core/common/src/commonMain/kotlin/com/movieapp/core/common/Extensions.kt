package com.movieapp.core.common

import kotlin.math.abs
import kotlin.math.roundToLong
import kotlinx.datetime.LocalDate

fun Int.formatWithCommas(): String {
    val digits = abs(toLong()).toString().reversed().chunked(3).joinToString(",").reversed()
    return if (this < 0) "-$digits" else digits
}

fun Double.formatOneDecimal(): String {
    val tenths = (this * 10).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    return "$sign${abs(tenths) / 10}.${abs(tenths) % 10}"
}

fun Double.formatRating(): String = formatOneDecimal()

/** Parses TMDB's `yyyy-MM-dd`; null when blank or malformed. */
fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

/** `Mar 5, 2026` style. Month names come from the caller so they can be localized. */
fun LocalDate.formatShort(monthNames: List<String>): String = "${monthNames[month.ordinal]} $day, $year"
