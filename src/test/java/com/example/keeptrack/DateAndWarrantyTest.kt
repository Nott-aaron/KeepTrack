package com.example.keeptrack

import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateAndWarrantyTest {

    private fun parseDate(value: String): Long? {
        val formats = listOf("dd/MM/yyyy", "yyyy-MM-dd", "MM/dd/yyyy")
        for (format in formats) {
            val result = runCatching {
                SimpleDateFormat(format, Locale.US).apply { isLenient = false }.parse(value)?.time
            }.getOrNull()
            if (result != null) return result
        }
        return null
    }

    private fun parseAmount(value: String): Double? {
        return value.replace("₹", "").replace("$", "").replace(",", "").trim().toDoubleOrNull()
    }

    @Test
    fun testParseDate_ddMMyyyy() {
        val timestamp = parseDate("15/05/2024")
        assertNotNull(timestamp)
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp!! }
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testParseDate_yyyyMMdd() {
        val timestamp = parseDate("2024-05-15")
        assertNotNull(timestamp)
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp!! }
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testParseDate_invalid() {
        assertNull(parseDate("invalid-date"))
        assertNull(parseDate(""))
    }

    @Test
    fun testParseAmount() {
        assertEquals(1299.99, parseAmount("₹1,299.99")!!, 0.001)
        assertEquals(500.0, parseAmount("$500")!!, 0.001)
        assertEquals(49.5, parseAmount("49.50")!!, 0.001)
        assertNull(parseAmount("N/A"))
    }

    @Test
    fun testWarrantyCalculation() {
        val purchase = parseDate("15/05/2024")!!
        val months = 12
        val expiry = Calendar.getInstance().apply {
            timeInMillis = purchase
            add(Calendar.MONTH, months)
        }.timeInMillis

        val cal = Calendar.getInstance().apply { timeInMillis = expiry }
        assertEquals(2025, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
    }
}
