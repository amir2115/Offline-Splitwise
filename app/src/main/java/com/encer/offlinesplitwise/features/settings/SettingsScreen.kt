package com.encer.offlinesplitwise.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.encer.offlinesplitwise.data.preferences.AppLanguage
import com.encer.offlinesplitwise.data.preferences.AppThemeMode
import com.encer.offlinesplitwise.ui.components.HeroCard
import com.encer.offlinesplitwise.ui.components.appFilterChipColors
import com.encer.offlinesplitwise.ui.components.appOutlinedButtonColors
import com.encer.offlinesplitwise.ui.components.appPlainCardColors
import com.encer.offlinesplitwise.ui.localization.appStrings
import com.encer.offlinesplitwise.ui.formatting.formatDate

@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    currentTheme: AppThemeMode,
    sessionName: String?,
    sessionUsername: String?,
    canSync: Boolean,
    isOnline: Boolean,
    lastSyncedAt: Long?,
    syncError: String?,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
    onSignIn: () -> Unit,
) {
    val strings = appStrings()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = strings.settingsHeroTitle,
            subtitle = strings.settingsHeroSubtitle,
            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) }
        )
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = appPlainCardColors(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(strings.accountTitle, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = if (sessionUsername == null) {
                        strings.accountSubtitleGuest
                    } else {
                        sessionName?.takeIf { it.isNotBlank() }?.let { "$it (@$sessionUsername)" } ?: "@$sessionUsername"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sessionUsername == null) {
                    OutlinedButton(onClick = onSignIn, colors = appOutlinedButtonColors()) {
                        Text(strings.signInLabel, style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    OutlinedButton(
                        onClick = onLogout,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(strings.logoutLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = appPlainCardColors(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(strings.syncTitle, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = when {
                        !canSync -> strings.syncLoginRequired
                        isOnline -> strings.syncOnline
                        else -> strings.syncOffline
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        !canSync -> MaterialTheme.colorScheme.onSurfaceVariant
                        isOnline -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = lastSyncedAt?.let { strings.lastSyncLabel(formatDate(it)) } ?: strings.notSyncedYet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (canSync && !syncError.isNullOrBlank()) {
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Rounded.CloudOff else Icons.Rounded.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (isOnline) strings.syncServerIssue else strings.syncConnectionIssue,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                OutlinedButton(onClick = onSyncNow, colors = appOutlinedButtonColors()) {
                    Text(strings.syncNow, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = appPlainCardColors(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(strings.languageTitle, style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = currentLanguage == AppLanguage.FA,
                        onClick = { onLanguageSelected(AppLanguage.FA) },
                        label = { Text(strings.persianLabel, style = MaterialTheme.typography.labelLarge) },
                        colors = appFilterChipColors()
                    )
                    FilterChip(
                        selected = currentLanguage == AppLanguage.EN,
                        onClick = { onLanguageSelected(AppLanguage.EN) },
                        label = { Text(strings.englishLabel, style = MaterialTheme.typography.labelLarge) },
                        colors = appFilterChipColors()
                    )
                }
            }
        }
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = appPlainCardColors(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(strings.themeTitle, style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = currentTheme == AppThemeMode.LIGHT,
                        onClick = { onThemeSelected(AppThemeMode.LIGHT) },
                        label = { Text(strings.lightLabel, style = MaterialTheme.typography.labelLarge) },
                        colors = appFilterChipColors()
                    )
                    FilterChip(
                        selected = currentTheme == AppThemeMode.DARK,
                        onClick = { onThemeSelected(AppThemeMode.DARK) },
                        label = { Text(strings.darkLabel, style = MaterialTheme.typography.labelLarge) },
                        colors = appFilterChipColors()
                    )
                }
            }
        }
    }
}
