package com.encer.splitwise.ui.components

import android.R.attr.singleLine
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.encer.splitwise.ui.localization.appStrings

@Composable
fun NameDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    confirmLabel: String,
    errorMessage: String? = null,
    confirmEnabled: Boolean = true,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val strings = appStrings()
    var value by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = confirmEnabled && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text(confirmLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = confirmEnabled && !isLoading) {
                Text(strings.cancel, style = MaterialTheme.typography.labelLarge)
            }
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(18.dp),
                    isError = errorMessage != null,
                    singleLine = true,
                    enabled = !isLoading,
                )
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    )
}
