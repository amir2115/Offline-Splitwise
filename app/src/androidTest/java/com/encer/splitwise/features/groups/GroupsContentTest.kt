package com.encer.splitwise.features.groups

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import com.encer.splitwise.data.preferences.AppLanguage
import com.encer.splitwise.ui.localization.LocalAppLanguage
import com.encer.splitwise.ui.localization.LocalAppStrings
import com.encer.splitwise.ui.localization.stringsFor
import com.encer.splitwise.ui.theme.SplitwiseTheme
import org.junit.Rule
import org.junit.Test

class GroupsContentTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyStateRendersSingleCardAndGroupsSectionTitle() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.FA,
                LocalAppStrings provides stringsFor(AppLanguage.FA),
                LocalLayoutDirection provides LayoutDirection.Rtl,
            ) {
                SplitwiseTheme(darkTheme = false) {
                    GroupsContent(
                        uiState = GroupsUiState(),
                        showCreateDialog = false,
                        editingGroupId = null,
                        pendingGroupActionId = null,
                        onOpenGroup = {},
                        onShowCreateDialogChange = {},
                        onEditingGroupChange = {},
                        onPendingGroupActionChange = {},
                        onCreateGroup = {},
                        onUpdateGroup = {},
                        onDeleteGroup = {},
                        onLeaveGroup = {},
                        onAcceptInvite = {},
                        onRejectInvite = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("گروه‌ها").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("هنوز گروهی نداری").assertCountEquals(1)
    }
}
