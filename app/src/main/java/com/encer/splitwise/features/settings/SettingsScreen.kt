package com.encer.splitwise.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.encer.splitwise.data.preferences.AppLanguage
import com.encer.splitwise.data.preferences.AppThemeMode
import com.encer.splitwise.ui.components.HeroCard
import com.encer.splitwise.ui.components.appFilterChipColors
import com.encer.splitwise.ui.components.appOutlinedButtonColors
import com.encer.splitwise.ui.components.appPlainCardColors
import com.encer.splitwise.ui.localization.appStrings
import com.encer.splitwise.ui.formatting.formatDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    currentTheme: AppThemeMode,
    sessionName: String?,
    sessionUsername: String?,
    canSync: Boolean,
    hasInternet: Boolean,
    isApiReachable: Boolean,
    lastHealthWasSuccessful: Boolean,
    lastSyncedAt: Long?,
    isSyncing: Boolean,
    syncError: String?,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
    onSignIn: () -> Unit,
) {
    val strings = appStrings()
    val effectiveOnline = lastHealthWasSuccessful || isApiReachable || hasInternet
    val syncSupportingText = when {
        !canSync -> strings.syncLoginRequired
        isSyncing -> strings.syncInProgress
        !syncError.isNullOrBlank() -> syncError
        !effectiveOnline -> strings.syncConnectionIssue
        !isApiReachable && !lastHealthWasSuccessful -> strings.syncServerIssue
        else -> strings.syncSubtitle
    }
    val syncSupportingColor = if (canSync && (isSyncing || !effectiveOnline || !syncError.isNullOrBlank() || (!isApiReachable && !lastHealthWasSuccessful))) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = strings.settingsHeroTitle,
            subtitle = strings.settingsHeroSubtitle,
            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) }
        )
        SettingsSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSectionHeader(
                    title = strings.accountTitle,
                    subtitle = if (sessionUsername == null) strings.accountSubtitleGuest else strings.accountSubtitleSignedIn,
                    icon = Icons.Rounded.PersonOutline,
                )
                if (sessionUsername == null) {
                    SettingsInfoCard(
                        headline = strings.accountTitle,
                        supporting = strings.accountSubtitleGuest,
                    )
                    OutlinedButton(
                        onClick = onSignIn,
                        modifier = Modifier.fillMaxWidth(),
                        colors = appOutlinedButtonColors()
                    ) {
                        Text(strings.signInLabel, style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    SettingsInfoCard(
                        headline = sessionName?.takeIf { it.isNotBlank() } ?: "@$sessionUsername",
                        supporting = "@$sessionUsername",
                    )
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(strings.logoutLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        SettingsSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSectionHeader(
                    title = strings.syncTitle,
                    subtitle = strings.syncSubtitle,
                    icon = Icons.Rounded.Sync,
                    trailing = {
                        SettingsStatusBadge(
                            text = when {
                                !canSync -> strings.signInLabel
                                effectiveOnline -> strings.syncOnline
                                else -> strings.syncOffline
                            },
                            tone = when {
                                !canSync -> MaterialTheme.colorScheme.outline
                                effectiveOnline -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                )
                SettingsInfoCard(
                    headline = lastSyncedAt?.let { strings.lastSyncLabel(formatDate(it)) } ?: strings.notSyncedYet,
                    supporting = syncSupportingText,
                    supportingColor = syncSupportingColor
                )
                OutlinedButton(
                    onClick = if (canSync) onSyncNow else onSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = appOutlinedButtonColors(),
                    enabled = !isSyncing
                ) {
                    if (canSync && isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.size(8.dp))
                    }
                    Text(
                        if (!canSync) strings.signInLabel else if (isSyncing) strings.syncInProgress else strings.syncNow,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        SettingsSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SettingsSectionHeader(
                    title = strings.languageTitle,
                    subtitle = strings.languageSubtitle,
                    icon = Icons.Rounded.Language,
                )
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
        SettingsSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SettingsSectionHeader(
                    title = strings.themeTitle,
                    subtitle = strings.themeSubtitle,
                    icon = Icons.Rounded.Palette,
                )
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

@Composable
private fun SettingsSectionCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = appPlainCardColors(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape)
                .padding(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingsStatusBadge(
    text: String,
    tone: Color,
) {
    Box(
        modifier = Modifier
            .background(tone.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, tone.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = tone)
    }
}

@Composable
private fun SettingsInfoCard(
    headline: String,
    supporting: String,
    supportingColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.42f), RoundedCornerShape(22.dp))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(headline, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(supporting, style = MaterialTheme.typography.bodyMedium, color = supportingColor)
    }
}
