package com.encer.splitwise.ui.formatting

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FormattedAmount(
    val number: String,
    val currency: String,
)

fun formatAmount(amount: Int): String {
    val locale = Locale.getDefault()
    val parts = formatAmountParts(amount, locale)
    return "${parts.number} ${parts.currency}"
}

fun formatAmountParts(amount: Int, locale: Locale = Locale.getDefault()): FormattedAmount {
    val number = NumberFormat.getIntegerInstance(locale).format(amount)
    val suffix = if (locale.language == "fa") "تومان" else "Toman"
    return FormattedAmount(number = number, currency = suffix)
}

fun formatAmountCompact(amount: Int): String {
    return NumberFormat.getIntegerInstance(Locale.getDefault()).format(amount)
}

fun formatCalculatorDisplayValue(input: String): String? {
    val normalized = normalizeAmountDigits(input)
    if (normalized.isBlank()) return null

    evaluateCalculatorExpression(normalized)?.let { result ->
        return formatAmountCompact(result)
    }

    return normalized.toLongOrNull()
        ?.takeIf { it in 0..Int.MAX_VALUE.toLong() }
        ?.toInt()
        ?.let(::formatAmountCompact)
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun parseAmountInput(input: String): Int {
    return parseAmountInputOrNull(input) ?: 0
}

fun parseAmountInputOrNull(input: String): Int? {
    val normalized = normalizeAmountDigits(input)
    if (normalized.isBlank()) return 0
    return normalized.toLongOrNull()?.takeIf { it in 0..Int.MAX_VALUE.toLong() }?.toInt()
}

fun normalizeAmountDigits(input: String): String {
    return input
        .trim()
        .replace(",", "")
        .replace("٬", "")
        .replace("،", "")
        .replace(" ", "")
        .map { char ->
            when (char) {
                '۰' -> '0'
                '۱' -> '1'
                '۲' -> '2'
                '۳' -> '3'
                '۴' -> '4'
                '۵' -> '5'
                '۶' -> '6'
                '۷' -> '7'
                '۸' -> '8'
                '۹' -> '9'
                else -> char
            }
        }
        .joinToString("")
}

class GroupedNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isBlank()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val digitsOnly = raw.filter { it.isDigit() }
        if (digitsOnly.isBlank()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val grouped = groupDigitsForDisplay(digitsOnly)
        val transformed = AnnotatedString(grouped)

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, digitsOnly.length)
                val commasInserted = grouped.takeWhileIndexedDigits(safeOffset).count { !it.isDigit() }
                return (safeOffset + commasInserted).coerceAtMost(grouped.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, grouped.length)
                return grouped.take(safeOffset).count { it.isDigit() }.coerceAtMost(digitsOnly.length)
            }
        }

        return TransformedText(transformed, mapping)
    }
}

private fun groupDigitsForDisplay(digitsOnly: String): String {
    val separator = DecimalFormatSymbols.getInstance(Locale.getDefault()).groupingSeparator
    return digitsOnly.reversed()
        .chunked(3)
        .joinToString(separator.toString())
        .reversed()
}

private fun String.takeWhileIndexedDigits(originalDigitCount: Int): String {
    if (originalDigitCount <= 0) return ""
    var digitCount = 0
    val builder = StringBuilder()
    for (char in this) {
        if (digitCount >= originalDigitCount) break
        builder.append(char)
        if (char.isDigit()) {
            digitCount += 1
        }
    }
    return builder.toString()
}
