package com.encer.splitwise.ui.formatting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorDisplayFormattingTest {
    @Test
    fun `formats plain digits with grouping separators`() {
        assertEquals("1,000", formatCalculatorDisplayValue("1000"))
    }

    @Test
    fun `formats persian digits with grouping separators`() {
        assertEquals("1,234,567", formatCalculatorDisplayValue("۱۲۳۴۵۶۷"))
    }

    @Test
    fun `formats valid expressions using evaluated result`() {
        assertEquals("14", formatCalculatorDisplayValue("2+3*4"))
    }

    @Test
    fun `returns null for invalid expressions`() {
        assertNull(formatCalculatorDisplayValue("2+"))
    }
}
