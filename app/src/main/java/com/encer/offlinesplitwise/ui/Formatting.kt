package com.encer.offlinesplitwise.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val persianLocale = Locale.forLanguageTag("fa")
private val persianNumberFormatter = NumberFormat.getIntegerInstance(persianLocale)

fun formatAmount(amount: Int): String {
    val number = persianNumberFormatter.format(amount)
    return "$number تومان"
}

fun formatAmountCompact(amount: Int): String {
    return persianNumberFormatter.format(amount)
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy/MM/dd", persianLocale)
    return formatter.format(Date(timestamp))
}

fun parseAmountInput(input: String): Int {
    val normalized = input
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
    return normalized.toIntOrNull() ?: 0
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

        val grouped = persianNumberFormatter.format(digitsOnly.toLongOrNull() ?: 0L)
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
