package com.encer.offlinesplitwise.ui.formatting

private class CalculatorParser(private val input: String) {
    private var index = 0

    fun parse(): Long {
        val value = parseExpression()
        skipWhitespace()
        require(index == input.length) { "Unexpected token" }
        require(value in 0..Int.MAX_VALUE.toLong()) { "Out of range" }
        return value
    }

    private fun parseExpression(): Long {
        var value = parseTerm()
        while (true) {
            skipWhitespace()
            value = when {
                match('+') -> value + parseTerm()
                match('-') -> {
                    val result = value - parseTerm()
                    require(result >= 0) { "Negative result" }
                    result
                }
                else -> return value
            }
        }
    }

    private fun parseTerm(): Long {
        var value = parseFactor()
        while (true) {
            skipWhitespace()
            value = when {
                match('*') -> value * parseFactor()
                match('/') -> {
                    val divisor = parseFactor()
                    require(divisor != 0L) { "Division by zero" }
                    require(value % divisor == 0L) { "Fractional result" }
                    value / divisor
                }
                else -> return value
            }
        }
    }

    private fun parseFactor(): Long {
        skipWhitespace()
        return if (match('(')) {
            val value = parseExpression()
            require(match(')')) { "Missing closing parenthesis" }
            value
        } else {
            parseNumber()
        }
    }

    private fun parseNumber(): Long {
        skipWhitespace()
        val start = index
        while (index < input.length && input[index].isDigit()) index += 1
        require(index > start) { "Number expected" }
        return input.substring(start, index).toLong()
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
    return runCatching { CalculatorParser(normalized).parse().toInt() }.getOrNull()
}
