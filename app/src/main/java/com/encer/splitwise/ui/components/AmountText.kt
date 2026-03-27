package com.encer.splitwise.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.encer.splitwise.ui.formatting.formatAmountParts

@Composable
fun AmountText(
    amount: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
) {
    val parts = remember(amount) { formatAmountParts(amount) }
    val suffixFontSize = if (style.fontSize.value.isNaN()) {
        MaterialTheme.typography.bodyMedium.fontSize
    } else {
        style.fontSize * 0.72f
    }
    Text(
        text = buildAnnotatedString {
            append(parts.number)
            append(" ")
            withStyle(SpanStyle(fontSize = suffixFontSize, fontWeight = FontWeight.Medium)) {
                append(parts.currency)
            }
        },
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}
