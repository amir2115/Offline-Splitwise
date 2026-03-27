package com.encer.splitwise.core.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.encer.splitwise.ui.localization.LocalAppLanguage
import com.encer.splitwise.ui.localization.LocalAppStrings
import com.encer.splitwise.ui.components.appHiltViewModel
import com.encer.splitwise.ui.localization.stringsFor
import com.encer.splitwise.ui.theme.SplitwiseTheme
import com.encer.splitwise.data.preferences.AppLanguage
import com.encer.splitwise.features.auth.AuthScreen
import com.encer.splitwise.features.balances.BalancesScreen
import com.encer.splitwise.features.expense_details.ExpenseDetailScreen
import com.encer.splitwise.features.expense_editor.ExpenseEditorScreen
import com.encer.splitwise.features.group_dashboard.GroupDashboardScreen
import com.encer.splitwise.features.groups.GroupsScreen
import com.encer.splitwise.features.members.MembersScreen
import com.encer.splitwise.features.settings.SettingsScreen
import com.encer.splitwise.features.settlement_editor.SettlementEditorScreen
import com.encer.splitwise.data.update.AppUpdateMode
import java.util.Locale

private enum class RootDestination(val route: String) {
    HOME(AppRoutes.GROUPS),
    SETTINGS(AppRoutes.SETTINGS)
}

@Composable
fun SplitwiseApp() {
    val navController = rememberNavController()
    val appShellViewModel: AppShellViewModel = appHiltViewModel()
    val settings by appShellViewModel.settingsRepository.observeSettings().collectAsStateWithLifecycle()
    val session by appShellViewModel.sessionRepository.observeSession().collectAsStateWithLifecycle()
    val syncStatus by appShellViewModel.syncCoordinator.observeSyncStatus().collectAsStateWithLifecycle()
    val networkStatus by appShellViewModel.networkMonitor.observeStatus().collectAsStateWithLifecycle()
    val updateState by appShellViewModel.appUpdateChecker.observeUpdateState().collectAsStateWithLifecycle()
    val context = LocalContext.current
    val strings = stringsFor(settings.language)
    val layoutDirection = if (settings.language == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val topLevelRoutes = RootDestination.entries.map { it.route }.toSet()
    var guestMode by rememberSaveable { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(settings.language) {
        Locale.setDefault(Locale.forLanguageTag(settings.language.tag))
    }

    LaunchedEffect(session?.userId) {
        if (session != null) {
            appShellViewModel.syncCoordinator.requestSync()
        }
    }

    LaunchedEffect(currentDestination?.route) {
        if (currentDestination?.route == RootDestination.SETTINGS.route) {
            appShellViewModel.refreshConnectionStatus()
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides settings.language,
        LocalAppStrings provides strings,
        LocalLayoutDirection provides layoutDirection
    ) {
        SplitwiseTheme(darkTheme = settings.themeMode == com.encer.splitwise.data.preferences.AppThemeMode.DARK) {
            AppSystemBars(darkTheme = settings.themeMode == com.encer.splitwise.data.preferences.AppThemeMode.DARK)
            if (updateState.isVisible) {
                AlertDialog(
                    onDismissRequest = {
                        if (updateState.mode == AppUpdateMode.SOFT) {
                            appShellViewModel.appUpdateChecker.dismissSoftUpdate()
                        }
                    },
                    title = {
                        Text(
                            updateState.title
                                ?: if (updateState.mode == AppUpdateMode.HARD) strings.updateRequiredTitle else strings.updateAvailableTitle
                        )
                    },
                    text = {
                        Text(updateState.message ?: strings.updateDefaultMessage)
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                updateState.storeUrl?.let { url ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            }
                        ) {
                            Text(strings.updateNow)
                        }
                    },
                    dismissButton = {
                        if (updateState.mode == AppUpdateMode.SOFT) {
                            androidx.compose.material3.TextButton(
                                onClick = appShellViewModel.appUpdateChecker::dismissSoftUpdate
                            ) {
                                Text(strings.updateLater)
                            }
                        }
                    }
                )
            }
            if (session == null && !guestMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = appBackgroundBrush(settings.themeMode == com.encer.splitwise.data.preferences.AppThemeMode.DARK))
                ) {
                    AuthScreen(
                        onLogin = { username, password -> appShellViewModel.syncCoordinator.login(username, password) },
                        onRegister = { name, username, password -> appShellViewModel.syncCoordinator.register(name, username, password) },
                        onContinueOffline = { guestMode = true }
                    )
                }
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (currentDestination?.route in topLevelRoutes) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                tonalElevation = 0.dp
                            ) {
                                RootDestination.entries.forEach { destination ->
                                    NavigationBarItem(
                                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                                        onClick = {
                                            navController.navigate(destination.route) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (destination == RootDestination.HOME) Icons.Rounded.Home else Icons.Rounded.Settings,
                                                contentDescription = null
                                            )
                                        },
                                        label = {
                                            Text(
                                                if (destination == RootDestination.HOME) strings.homeTab else strings.settingsTab,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(appBackgroundBrush(settings.themeMode == com.encer.splitwise.data.preferences.AppThemeMode.DARK))
                            .padding(bottom = if (currentDestination?.route in topLevelRoutes) 8.dp else 0.dp)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = RootDestination.HOME.route,
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        ) {
                            composable(AppRoutes.GROUPS) {
                                GroupsScreen(onOpenGroup = { groupId -> navController.navigate(AppRoutes.group(groupId)) })
                            }
                            composable(AppRoutes.SETTINGS) {
                                SettingsScreen(
                                    currentLanguage = settings.language,
                                    currentTheme = settings.themeMode,
                                    sessionName = session?.name,
                                    sessionUsername = session?.username,
                                    canSync = session != null,
                                    hasInternet = session != null && networkStatus.hasInternet,
                                    isApiReachable = session != null && networkStatus.isApiReachable,
                                    lastSyncedAt = syncStatus.lastSyncedAt,
                                    syncError = syncStatus.lastError,
                                    onLanguageSelected = appShellViewModel.settingsRepository::updateLanguage,
                                    onThemeSelected = appShellViewModel.settingsRepository::updateThemeMode,
                                    onSyncNow = {
                                        if (session == null) guestMode = false else appShellViewModel.syncCoordinator.requestSync()
                                    },
                                    onLogout = {
                                        guestMode = false
                                        appShellViewModel.syncCoordinator.logout()
                                    },
                                    onSignIn = { guestMode = false }
                                )
                            }
                            composable(route = AppRoutes.GROUP_PATTERN, arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { entry ->
                                GroupDashboardScreen(
                                    groupId = entry.arguments?.getString("groupId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onOpenMembers = { navController.navigate(AppRoutes.members(entry.arguments?.getString("groupId").orEmpty())) },
                                    onAddExpense = { navController.navigate(AppRoutes.expense(entry.arguments?.getString("groupId").orEmpty())) },
                                    onAddSettlement = { navController.navigate(AppRoutes.settlement(entry.arguments?.getString("groupId").orEmpty())) },
                                    onOpenBalances = { navController.navigate(AppRoutes.balances(entry.arguments?.getString("groupId").orEmpty())) },
                                    onOpenExpense = { expenseId -> navController.navigate(AppRoutes.expenseDetails(entry.arguments?.getString("groupId").orEmpty(), expenseId)) },
                                    onEditSettlement = { settlementId -> navController.navigate(AppRoutes.settlement(entry.arguments?.getString("groupId").orEmpty(), settlementId = settlementId)) }
                                )
                            }
                            composable(route = AppRoutes.MEMBERS_PATTERN, arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { entry ->
                                MembersScreen(groupId = entry.arguments?.getString("groupId").orEmpty(), onBack = { navController.popBackStack() })
                            }
                            composable(
                                route = AppRoutes.EXPENSE_PATTERN,
                                arguments = listOf(
                                    navArgument("groupId") { type = NavType.StringType },
                                    navArgument("expenseId") { type = NavType.StringType; nullable = true; defaultValue = null }
                                )
                            ) { entry ->
                                ExpenseEditorScreen(
                                    groupId = entry.arguments?.getString("groupId").orEmpty(),
                                    expenseId = entry.arguments?.getString("expenseId")?.takeIf { it.isNotBlank() },
                                    onBack = { navController.popBackStack() },
                                    onSaved = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = AppRoutes.SETTLEMENT_PATTERN,
                                arguments = listOf(
                                    navArgument("groupId") { type = NavType.StringType },
                                    navArgument("settlementId") { type = NavType.StringType; nullable = true; defaultValue = null },
                                    navArgument("fromMemberId") { type = NavType.StringType; nullable = true; defaultValue = null },
                                    navArgument("toMemberId") { type = NavType.StringType; nullable = true; defaultValue = null },
                                    navArgument("amount") { type = NavType.StringType; nullable = true; defaultValue = null }
                                )
                            ) { entry ->
                                SettlementEditorScreen(
                                    groupId = entry.arguments?.getString("groupId").orEmpty(),
                                    settlementId = entry.arguments?.getString("settlementId")?.takeIf { it.isNotBlank() },
                                    initialFromMemberId = entry.arguments?.getString("fromMemberId")?.takeIf { it.isNotBlank() },
                                    initialToMemberId = entry.arguments?.getString("toMemberId")?.takeIf { it.isNotBlank() },
                                    initialAmountInput = entry.arguments?.getString("amount")?.takeIf { it.isNotBlank() },
                                    onBack = { navController.popBackStack() },
                                    onSaved = { navController.popBackStack() }
                                )
                            }
                            composable(route = AppRoutes.BALANCES_PATTERN, arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { entry ->
                                val groupId = entry.arguments?.getString("groupId").orEmpty()
                                BalancesScreen(
                                    groupId = groupId,
                                    onSuggestedPaymentClick = { transfer ->
                                        navController.navigate(
                                            AppRoutes.settlement(
                                                groupId = groupId,
                                                fromMemberId = transfer.fromMemberId,
                                                toMemberId = transfer.toMemberId,
                                                amount = transfer.amount.toString()
                                            )
                                        )
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = AppRoutes.EXPENSE_DETAILS_PATTERN,
                                arguments = listOf(
                                    navArgument("groupId") { type = NavType.StringType },
                                    navArgument("expenseId") { type = NavType.StringType }
                                )
                            ) { entry ->
                                ExpenseDetailScreen(
                                    groupId = entry.arguments?.getString("groupId").orEmpty(),
                                    expenseId = entry.arguments?.getString("expenseId").orEmpty(),
                                    onBack = { navController.popBackStack() },
                                    onEdit = { targetGroupId, expenseId ->
                                        navController.navigate(AppRoutes.expense(targetGroupId, expenseId))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSystemBars(darkTheme: Boolean) {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surface.copy(alpha = 0.98f).toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

@Composable
private fun appBackgroundBrush(isDark: Boolean): Brush =
    Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color(0xFF0D1719), Color(0xFF112226), Color(0xFF142A2F))
        } else {
            listOf(Color(0xFFF9FBF2), Color(0xFFF0F6F6), Color(0xFFFFF8EF))
        }
    )
