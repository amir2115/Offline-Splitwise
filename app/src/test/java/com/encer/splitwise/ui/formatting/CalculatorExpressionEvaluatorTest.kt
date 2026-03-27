package com.encer.splitwise.ui.formatting

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
    fun `rounds fractional results instead of rejecting them`() {
        assertEquals(3, evaluateCalculatorExpression("10/3"))
    }

    @Test
    fun `evaluates mixed multiply divide expressions with rounding`() {
        assertEquals(378, evaluateCalculatorExpression("345*34/31"))
    }

    @Test
    fun `rejects negative results`() {
        assertNull(evaluateCalculatorExpression("4-9"))
    }
}
