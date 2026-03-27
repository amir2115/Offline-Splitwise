package com.encer.splitwise.ui.formatting

import java.math.BigDecimal
import java.math.RoundingMode

private class CalculatorParser(private val input: String) {
    private var index = 0

    fun parse(): BigDecimal {
        val value = parseExpression()
        skipWhitespace()
        require(index == input.length) { "Unexpected token" }
        require(value >= BigDecimal.ZERO) { "Negative result" }
        require(value <= BigDecimal(Int.MAX_VALUE)) { "Out of range" }
        return value
    }

    private fun parseExpression(): BigDecimal {
        var value = parseTerm()
        while (true) {
            skipWhitespace()
            value = when {
                match('+') -> value + parseTerm()
                match('-') -> {
                    val result = value - parseTerm()
                    require(result >= BigDecimal.ZERO) { "Negative result" }
                    result
                }
                else -> return value
            }
        }
    }

    private fun parseTerm(): BigDecimal {
        var value = parseFactor()
        while (true) {
            skipWhitespace()
            value = when {
                match('*') -> value * parseFactor()
                match('/') -> {
                    val divisor = parseFactor()
                    require(divisor.compareTo(BigDecimal.ZERO) != 0) { "Division by zero" }
                    value.divide(divisor, 8, RoundingMode.HALF_UP)
                }
                else -> return value
            }
        }
    }

    private fun parseFactor(): BigDecimal {
        skipWhitespace()
        return if (match('(')) {
            val value = parseExpression()
            require(match(')')) { "Missing closing parenthesis" }
            value
        } else {
            parseNumber()
        }
    }

    private fun parseNumber(): BigDecimal {
        skipWhitespace()
        val start = index
        while (index < input.length && input[index].isDigit()) index += 1
        require(index > start) { "Number expected" }
        return input.substring(start, index).toBigDecimal()
    }

    private fun skipWhitespace() {
        while (index < input.length && input[index].isWhitespace()) index += 1
    }

    private fun match(char: Char): Boolean {
        if (index < input.length && input[index] == char) {
            index += 1
            return true
        }
        return false
    }
}

fun evaluateCalculatorExpression(input: String): Int? {
    val normalized = normalizeAmountDigits(input)
    if (normalized.isBlank()) return 0
    return runCatching {
        CalculatorParser(normalized)
            .parse()
            .setScale(0, RoundingMode.HALF_UP)
            .intValueExact()
    }.getOrNull()
}
