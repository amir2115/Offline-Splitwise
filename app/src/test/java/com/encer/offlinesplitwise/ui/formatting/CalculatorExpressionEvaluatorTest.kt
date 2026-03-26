package com.encer.offlinesplitwise.ui.formatting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorExpressionEvaluatorTest {
    @Test
    fun `evaluates basic arithmetic with precedence`() {
        assertEquals(14, evaluateCalculatorExpression("2+3*4"))
    }

    @Test
    fun `evaluates expressions with parentheses and persian digits`() {
        assertEquals(21, evaluateCalculatorExpression("(۲+۵)*۳"))
    }

    @Test
    fun `rejects fractional results`() {
        assertNull(evaluateCalculatorExpression("10/3"))
    }

    @Test
    fun `rejects negative results`() {
        assertNull(evaluateCalculatorExpression("4-9"))
    }
}
