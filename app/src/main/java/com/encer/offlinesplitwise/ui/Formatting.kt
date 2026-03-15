package com.encer.offlinesplitwise.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val persianLocale = Locale("fa")

fun formatAmount(amount: Int): String {
    val number = NumberFormat.getIntegerInstance(persianLocale).format(amount)
    return "$number تومان"
}

fun formatAmountCompact(amount: Int): String {
    return NumberFormat.getIntegerInstance(persianLocale).format(amount)
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy/MM/dd", persianLocale)
    return formatter.format(Date(timestamp))
}

fun parseAmountInput(input: String): Int {
    val normalized = input
        .trim()
        .replace(",", "")
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
