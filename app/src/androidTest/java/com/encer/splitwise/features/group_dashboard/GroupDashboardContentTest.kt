package com.encer.splitwise.features.group_dashboard

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import com.encer.splitwise.data.preferences.AppLanguage
import com.encer.splitwise.domain.model.Group
import com.encer.splitwise.domain.model.GroupSummary
import com.encer.splitwise.ui.localization.LocalAppLanguage
import com.encer.splitwise.ui.localization.LocalAppStrings
import com.encer.splitwise.ui.localization.stringsFor
import com.encer.splitwise.ui.theme.SplitwiseTheme
import org.junit.Rule
import org.junit.Test

class GroupDashboardContentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun refreshingStateShowsPullToRefreshIndicator() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.FA,
                LocalAppStrings provides stringsFor(AppLanguage.FA),
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                SplitwiseTheme(darkTheme = false) {
                    GroupDashboardContent(
                        uiState = GroupDashboardUiState(
                            group = Group(
                                id = "group-trip",
                                name = "سفر شمال",
                                createdAt = 1_742_000_000_000,
                                updatedAt = 1_742_000_000_000,
                                userId = "user-1",
                            ),
                            summary = GroupSummary(320_000, 4, 80_000, 3),
                            isRefreshing = true,
                            canRefresh = true,
                        ),
                        groupId = "group-trip",
                        onBack = {},
                        onRefresh = {},
                        onOpenMembers = {},
                        onAddExpense = {},
                        onAddSettlement = {},
                        onOpenBalances = {},
                        onOpenExpense = {},
                        onEditSettlement = {},
                        onDeleteSettlement = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("سفر شمال").assertIsDisplayed()
        composeTestRule.onNodeWithTag("groupDashboardPullToRefreshIndicator").assertIsDisplayed()
    }
}
