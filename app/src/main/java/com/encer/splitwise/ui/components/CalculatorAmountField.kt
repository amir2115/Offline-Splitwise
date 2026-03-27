package com.encer.splitwise.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.encer.splitwise.ui.formatting.evaluateCalculatorExpression
import com.encer.splitwise.ui.formatting.normalizeAmountDigits

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
        CalculatorDialog(
            initialValue = value,
            onDismiss = { showCalculator = false },
            onConfirm = {
                onValueChange(it)
                showCalculator = false
            }
        )
    }
}

@Composable
private fun CalculatorDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var expression by rememberSaveable(initialValue) {
        mutableStateOf(normalizeAmountDigits(initialValue).ifBlank { "" })
    }
    var error by remember { mutableStateOf<String?>(null) }
    val buttons = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("0", "(", ")", "+"),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ماشین‌حساب") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = expression,
                    onValueChange = { expression = normalizeAmountDigits(it); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("عبارت") },
                    singleLine = true
                )
                if (error != null) {
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { token ->
                            Button(
                                onClick = { expression += token; error = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(token)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { expression = ""; error = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("C")
                    }
                    Button(
                        onClick = {
                            expression = expression.dropLast(1)
                            error = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⌫")
                    }
                    Button(
                        onClick = {
                            val result = evaluateCalculatorExpression(expression)
                            if (result == null) {
                                error = "عبارت معتبر نیست"
                            } else {
                                expression = result.toString()
                                error = null
                            }
                        },
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("=")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val result = evaluateCalculatorExpression(expression)
                    if (result == null) {
                        error = "عبارت معتبر نیست"
                    } else {
                        onConfirm(result.toString())
                    }
                }
            ) {
                Text("استفاده")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
