package com.encer.splitwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import com.encer.splitwise.ui.formatting.evaluateCalculatorExpression
import com.encer.splitwise.ui.formatting.formatCalculatorDisplayValue
import com.encer.splitwise.ui.formatting.normalizeAmountDigits
import com.encer.splitwise.ui.localization.appStrings

@Composable
fun CalculatorAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    colors: TextFieldColors,
    shape: Shape = RoundedCornerShape(18.dp),
    enabled: Boolean = true,
) {
    var showCalculator by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = amountVisualTransformation,
        colors = colors,
        shape = shape,
        trailingIcon = {
            IconButton(onClick = { showCalculator = true }, enabled = enabled) {
                Icon(Icons.Rounded.Calculate, contentDescription = null)
            }
        }
    )

    if (showCalculator) {
        CalculatorBottomSheet(
            initialValue = value,
            onDismiss = { showCalculator = false },
            onConfirm = {
                onValueChange(it)
                showCalculator = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorBottomSheet(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val strings = appStrings()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var expressionField by rememberSaveable(stateSaver = TextFieldValue.Saver, inputs = arrayOf(initialValue)) {
        mutableStateOf(
            TextFieldValue(
                text = normalizeAmountDigits(initialValue),
                selection = androidx.compose.ui.text.TextRange(normalizeAmountDigits(initialValue).length)
            )
        )
    }
    var error by remember { mutableStateOf<String?>(null) }
    val expression = expressionField.text

    val displayValue = remember(expression) {
        formatCalculatorDisplayValue(expression) ?: expression.ifBlank { "0" }
    }
    val showExpressionPreview = expression.isNotBlank() && displayValue != expression
    val buttons = remember {
        listOf(
            listOf("C", "(", ")", "⌫"),
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("0", "00", "=", "+"),
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.calculatorTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = strings.calculatorExpressionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textDirection = TextDirection.Ltr
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (showExpressionPreview) expression else "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDirection = TextDirection.Ltr
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                OutlinedTextField(
                    value = expressionField,
                    onValueChange = {
                        expressionField = normalizeExpressionFieldValue(it)
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(strings.calculatorExpressionLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = appFieldColors(),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        textDirection = TextDirection.Ltr,
                        textAlign = TextAlign.Start
                    )
                )
            }

            if (error != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { token ->
                        CalculatorKeyButton(
                            token = token,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (token) {
                                    "C" -> {
                                        expressionField = TextFieldValue("")
                                        error = null
                                    }

                                    "⌫" -> {
                                        expressionField = removeTokenAtSelection(expressionField)
                                        error = null
                                    }

                                    "=" -> {
                                        val result = resolveCalculatorSubmission(expression)
                                        if (result == null) {
                                            error = strings.calculatorInvalidExpression
                                        } else {
                                            expressionField = TextFieldValue(
                                                text = result,
                                                selection = androidx.compose.ui.text.TextRange(result.length)
                                            )
                                            error = null
                                        }
                                    }

                                    else -> {
                                        expressionField = insertTokenAtSelection(expressionField, token)
                                        error = null
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(strings.cancel, style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = {
                        val result = resolveCalculatorSubmission(expression)
                        if (result == null) {
                            error = strings.calculatorInvalidExpression
                        } else {
                            onConfirm(result)
                        }
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(strings.calculatorUseAction, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun CalculatorKeyButton(
    token: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isOperator = token in setOf("/", "*", "-", "+")
    val isAction = token in setOf("C", "⌫", "=")
    val containerColor = when {
        token == "=" -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
        isAction -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    }
    val contentColor = when {
        token == "=" -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.secondary
        isAction -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = token,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (token == "=") FontWeight.Bold else FontWeight.SemiBold
            ),
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

private fun resolveCalculatorSubmission(expression: String): String? {
    val normalized = normalizeAmountDigits(expression)
    if (normalized.isBlank()) return ""

    evaluateCalculatorExpression(normalized)?.let { return it.toString() }

    return normalized.toLongOrNull()
        ?.takeIf { it in 0..Int.MAX_VALUE.toLong() }
        ?.toString()
}

private fun normalizeExpressionFieldValue(value: TextFieldValue): TextFieldValue {
    val normalizedText = normalizeAmountDigits(value.text)
    val selectionEnd = value.selection.end.coerceIn(0, normalizedText.length)
    val selectionStart = value.selection.start.coerceIn(0, selectionEnd)
    return value.copy(
        text = normalizedText,
        selection = androidx.compose.ui.text.TextRange(selectionStart, selectionEnd)
    )
}

private fun insertTokenAtSelection(value: TextFieldValue, token: String): TextFieldValue {
    val start = value.selection.start.coerceIn(0, value.text.length)
    val end = value.selection.end.coerceIn(0, value.text.length)
    val newText = buildString {
        append(value.text.substring(0, start))
        append(token)
        append(value.text.substring(end))
    }
    val cursor = start + token.length
    return TextFieldValue(
        text = newText,
        selection = androidx.compose.ui.text.TextRange(cursor)
    )
}

private fun removeTokenAtSelection(value: TextFieldValue): TextFieldValue {
    val start = value.selection.start.coerceIn(0, value.text.length)
    val end = value.selection.end.coerceIn(0, value.text.length)
    if (start != end) {
        val newText = value.text.removeRange(start, end)
        return TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(start)
        )
    }
    if (start == 0) return value
    val newCursor = start - 1
    val newText = value.text.removeRange(newCursor, start)
    return TextFieldValue(
        text = newText,
        selection = androidx.compose.ui.text.TextRange(newCursor)
    )
}
