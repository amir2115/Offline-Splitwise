package com.encer.splitwise.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.encer.splitwise.data.preferences.AppLanguage
import com.encer.splitwise.data.preferences.AppThemeMode
import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.ExpenseShare
import com.encer.splitwise.domain.model.Group
import com.encer.splitwise.domain.model.GroupInvite
import com.encer.splitwise.domain.model.GroupSummary
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.domain.model.Settlement
import com.encer.splitwise.domain.model.SplitType
import com.encer.splitwise.features.auth.AuthScreen
import com.encer.splitwise.features.group_dashboard.GroupDashboardContent
import com.encer.splitwise.features.group_dashboard.GroupDashboardUiState
import com.encer.splitwise.features.groups.GroupsContent
import com.encer.splitwise.features.groups.GroupsUiState
import com.encer.splitwise.features.settings.SettingsScreen
import com.encer.splitwise.ui.localization.LocalAppLanguage
import com.encer.splitwise.ui.localization.LocalAppStrings
import com.encer.splitwise.ui.localization.stringsFor
import com.encer.splitwise.ui.theme.SplitwiseTheme

@Composable
fun PreviewAppChrome(
    language: AppLanguage = AppLanguage.FA,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val layoutDirection = if (language == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalAppStrings provides stringsFor(language),
        LocalLayoutDirection provides layoutDirection,
    ) {
        SplitwiseTheme(darkTheme = darkTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    }
}

@Composable
fun AuthScreenPreviewScenario(
    language: AppLanguage = AppLanguage.FA,
    darkTheme: Boolean = false,
) {
    PreviewAppChrome(language = language, darkTheme = darkTheme) {
        AuthScreen(
            onLogin = { _, _ -> Result.success(Unit) },
            onRegister = { _, _, _ -> Result.success(Unit) },
            onContinueOffline = {},
        )
    }
}

@Composable
fun SettingsScreenPreviewScenario(
    language: AppLanguage = AppLanguage.FA,
    darkTheme: Boolean = false,
) {
    PreviewAppChrome(language = language, darkTheme = darkTheme) {
        SettingsScreen(
            currentLanguage = language,
            currentTheme = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
            sessionName = if (language == AppLanguage.FA) "امیر" else "Amir",
            sessionUsername = "amir_dev",
            canSync = true,
            hasInternet = true,
            isApiReachable = true,
            lastSyncedAt = 1_742_000_000_000,
            syncError = null,
            onLanguageSelected = {},
            onThemeSelected = {},
            onSyncNow = {},
            onLogout = {},
            onSignIn = {},
        )
    }
}

@Composable
fun GroupsScreenPreviewScenario(
    language: AppLanguage = AppLanguage.FA,
    darkTheme: Boolean = false,
) {
    PreviewAppChrome(language = language, darkTheme = darkTheme) {
        GroupsContent(
            uiState = previewGroupsUiState(),
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

@Composable
fun GroupDashboardPreviewScenario(
    language: AppLanguage = AppLanguage.FA,
    darkTheme: Boolean = false,
) {
    PreviewAppChrome(language = language, darkTheme = darkTheme) {
        GroupDashboardContent(
            uiState = previewGroupDashboardUiState(),
            groupId = "group-trip",
            onBack = {},
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

private fun previewGroupsUiState(): GroupsUiState {
    val now = 1_742_000_000_000
    return GroupsUiState(
        groups = listOf(
            Group(
                id = "group-trip",
                name = "سفر شمال",
                createdAt = now,
                updatedAt = now,
                userId = "user-1",
            ),
            Group(
                id = "group-home",
                name = "خانه",
                createdAt = now - 86_400_000,
                updatedAt = now - 86_400_000,
                userId = "user-1",
            ),
        ),
        invites = listOf(
            GroupInvite(
                id = "invite-1",
                groupId = "group-dinner",
                memberId = "member-guest",
                username = "sara",
                inviterUserId = "user-2",
                inviteeUserId = "user-1",
                status = "pending",
                groupName = "شام دوستانه",
                inviterUsername = "sara",
                inviteeUsername = "amir_dev",
                respondedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        ),
        isLoading = false,
        canLeaveGroups = true,
        currentUserId = "user-1",
    )
}

private fun previewGroupDashboardUiState(): GroupDashboardUiState {
    val now = 1_742_000_000_000
    val members = listOf(
        Member(id = "member-1", groupId = "group-trip", username = "amir_dev", createdAt = now, updatedAt = now, userId = "user-1"),
        Member(id = "member-2", groupId = "group-trip", username = "sara", createdAt = now, updatedAt = now),
        Member(id = "member-3", groupId = "group-trip", username = "ali", createdAt = now, updatedAt = now),
    )
    return GroupDashboardUiState(
        group = Group(
            id = "group-trip",
            name = "سفر شمال",
            createdAt = now - 86_400_000,
            updatedAt = now,
            userId = "user-1",
        ),
        summary = GroupSummary(
            totalExpenses = 2_450_000,
            totalSettlements = 500_000,
            membersCount = members.size,
            openBalancesCount = 2,
        ),
        members = members,
        expenses = listOf(
            Expense(
                id = "expense-1",
                groupId = "group-trip",
                title = "ویلا",
                note = "بیعانه شب اول",
                totalAmount = 1_800_000,
                splitType = SplitType.EQUAL,
                createdAt = now,
                updatedAt = now,
                userId = "user-1",
                payers = listOf(ExpenseShare(memberId = "member-1", amount = 1_800_000)),
                shares = listOf(
                    ExpenseShare(memberId = "member-1", amount = 600_000),
                    ExpenseShare(memberId = "member-2", amount = 600_000),
                    ExpenseShare(memberId = "member-3", amount = 600_000),
                ),
            ),
            Expense(
                id = "expense-2",
                groupId = "group-trip",
                title = "نهار",
                note = "رستوران کنار دریا",
                totalAmount = 650_000,
                splitType = SplitType.EXACT,
                createdAt = now,
                updatedAt = now,
                userId = "user-2",
                payers = listOf(ExpenseShare(memberId = "member-2", amount = 650_000)),
                shares = listOf(
                    ExpenseShare(memberId = "member-1", amount = 200_000),
                    ExpenseShare(memberId = "member-2", amount = 250_000),
                    ExpenseShare(memberId = "member-3", amount = 200_000),
                ),
            ),
        ),
        settlements = listOf(
            Settlement(
                id = "settlement-1",
                groupId = "group-trip",
                fromMemberId = "member-3",
                toMemberId = "member-1",
                amount = 500_000,
                note = "تسویه بخشی از ویلا",
                createdAt = now,
                updatedAt = now,
                userId = "user-3",
            )
        ),
        canCreateTransactions = true,
    )
}
