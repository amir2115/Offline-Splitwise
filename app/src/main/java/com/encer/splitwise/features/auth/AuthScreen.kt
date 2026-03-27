package com.encer.splitwise.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.encer.splitwise.data.remote.model.ApiError
import com.encer.splitwise.ui.components.AppAnimatedVisibility
import com.encer.splitwise.ui.components.HeroCard
import com.encer.splitwise.ui.components.appFieldColors
import com.encer.splitwise.ui.components.appOutlinedButtonColors
import com.encer.splitwise.ui.components.appPlainCardColors
import com.encer.splitwise.ui.components.appPrimaryButtonColors
import com.encer.splitwise.ui.localization.AppStrings
import com.encer.splitwise.ui.localization.appStrings
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLogin: suspend (String, String) -> Result<*>,
    onRegister: suspend (String, String, String) -> Result<*>,
    onContinueOffline: () -> Unit,
) {
    val strings = appStrings()
    val coroutineScope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight - 48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            HeroCard(
                title = strings.authHeroTitle,
                subtitle = strings.authHeroSubtitle,
                icon = { Icon(Icons.Rounded.Groups, contentDescription = null) }
            )
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = appPlainCardColors(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) strings.authRegisterTitle else strings.authLoginTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (isRegisterMode) strings.authRegisterSubtitle else strings.authLoginSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppAnimatedVisibility(visible = isRegisterMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; errorMessage = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.nameLabel, style = MaterialTheme.typography.bodyMedium) },
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.usernameLabel, style = MaterialTheme.typography.bodyMedium) },
                        colors = appFieldColors(),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.passwordLabel, style = MaterialTheme.typography.bodyMedium) },
                        colors = appFieldColors(),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    AppAnimatedVisibility(visible = errorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = errorMessage ?: strings.authFailed,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val validationError = when {
                                isRegisterMode && name.trim().isBlank() -> strings.authNameRequired
                                username.trim().isBlank() -> strings.authUsernameRequired
                                password.isBlank() -> strings.authPasswordRequired
                                else -> null
                            }
                            if (validationError != null) {
                                errorMessage = validationError
                                return@Button
                            }
                            coroutineScope.launch {
                                loading = true
                                errorMessage = null
                                val result = if (isRegisterMode) {
                                    onRegister(name.trim(), username.trim(), password)
                                } else {
                                    onLogin(username.trim(), password)
                                }
                                loading = false
                                errorMessage = resolveAuthError(result.exceptionOrNull(), isRegisterMode, strings)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = appPrimaryButtonColors(),
                        enabled = !loading
                    ) {
                        if (loading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.4.dp
                                )
                                Text(
                                    if (isRegisterMode) strings.authRegisterLoading else strings.authLoginLoading,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        } else {
                            Text(
                                if (isRegisterMode) strings.registerAction else strings.loginAction,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onContinueOffline,
                        modifier = Modifier.fillMaxWidth(),
                        colors = appOutlinedButtonColors(),
                        enabled = !loading
                    ) {
                        Text(strings.continueOfflineAction, style = MaterialTheme.typography.labelLarge)
                    }
                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            errorMessage = null
                        },
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            if (isRegisterMode) strings.hasAccountPrompt else strings.noAccountPrompt,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

private fun resolveAuthError(
    error: Throwable?,
    isRegisterMode: Boolean,
    strings: AppStrings
): String? {
    if (error == null) return null
    val message = error.message?.lowercase().orEmpty()
    if (
        "unable to resolve host" in message ||
        "failed to connect" in message ||
        "timeout" in message ||
        "timed out" in message ||
        "no address associated with hostname" in message
    ) {
        return strings.authNetworkError
    }
    if (error is ApiError) {
        if (!isRegisterMode && error.status == 401) return strings.authInvalidCredentials
        if (isRegisterMode && (error.status == 409 || "already" in message || "exists" in message || "taken" in message)) {
            return strings.authUsernameTaken
        }
    }
    return if (isRegisterMode) strings.authRegisterFailed else strings.authLoginFailed
}
