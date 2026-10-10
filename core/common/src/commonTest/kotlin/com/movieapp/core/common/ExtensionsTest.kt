package com.movieapp.core.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class ExtensionsTest {

    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    @Test
    fun `formatWithCommas - various sizes - groups thousands`() {
        assertEquals("0", 0.formatWithCommas())
        assertEquals("999", 999.formatWithCommas())
        assertEquals("1,000", 1000.formatWithCommas())
        assertEquals("1,234,567", 1234567.formatWithCommas())
        assertEquals("-1,234", (-1234).formatWithCommas())
    }

    @Test
    fun `formatOneDecimal - various values - rounds to one decimal`() {
        assertEquals("7.3", 7.25.formatOneDecimal())
        assertEquals("7.0", 7.0.formatOneDecimal())
        assertEquals("0.0", 0.04.formatOneDecimal())
        assertEquals("-1.2", (-1.25).formatOneDecimal())
    }

    @Test
    fun `formatRating - value - matches formatOneDecimal`() {
        assertEquals("8.4", 8.44.formatRating())
    }

    @Test
    fun `toLocalDateOrNull - valid date - parses`() {
        assertEquals(LocalDate(2026, 3, 5), "2026-03-05".toLocalDateOrNull())
    }

    @Test
    fun `toLocalDateOrNull - blank or malformed - returns null`() {
        assertNull("".toLocalDateOrNull())
        assertNull("2026-13-01".toLocalDateOrNull())
    }

    @Test
    fun `formatShort - date - uses provided month name`() {
        assertEquals("Mar 5, 2026", LocalDate(2026, 3, 5).formatShort(months))
        assertEquals("Dec 31, 1999", LocalDate(1999, 12, 31).formatShort(months))
    }
}
