package com.encer.offlinesplitwise.ui.preview

import androidx.compose.runtime.Composable
import com.encer.offlinesplitwise.data.preferences.AppLanguage

@ResponsiveThemePreview
@Composable
fun AuthScreenResponsivePreview() {
    AuthScreenPreviewScenario()
}

@ResponsiveThemePreview
@Composable
fun SettingsScreenResponsivePreview() {
    SettingsScreenPreviewScenario()
}

@ResponsiveThemePreview
@Composable
fun GroupsScreenResponsivePreview() {
    GroupsScreenPreviewScenario()
}

@ResponsiveThemePreview
@Composable
fun GroupDashboardResponsivePreview() {
    GroupDashboardPreviewScenario(language = AppLanguage.EN)
}
