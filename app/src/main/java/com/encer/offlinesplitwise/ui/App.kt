package com.encer.offlinesplitwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.data.AppContainer
import com.encer.offlinesplitwise.data.preferences.AppLanguage
import com.encer.offlinesplitwise.data.preferences.AppThemeMode
import com.encer.offlinesplitwise.ui.theme.OfflineSplitwiseTheme
import java.util.Locale

private enum class RootDestination(
    val route: String
) {
    HOME("groups"),
    SETTINGS("settings")
}

@Composable
fun OfflineSplitwiseApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val settings by appContainer.settingsRepository.observeSettings().collectAsStateWithLifecycle()
    val strings = stringsFor(settings.language)
    val layoutDirection = if (settings.language == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val topLevelRoutes = RootDestination.entries.map { it.route }.toSet()

    LaunchedEffect(settings.language) {
        Locale.setDefault(Locale.forLanguageTag(settings.language.tag))
    }

    CompositionLocalProvider(
        LocalAppLanguage provides settings.language,
        LocalAppStrings provides strings,
        LocalLayoutDirection provides layoutDirection
    ) {
        OfflineSplitwiseTheme(darkTheme = settings.themeMode == AppThemeMode.DARK) {
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
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
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
                                    label = { Text(if (destination == RootDestination.HOME) strings.homeTab else strings.settingsTab) },
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
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (settings.themeMode == AppThemeMode.DARK) {
                                    listOf(Color(0xFF0D1719), Color(0xFF112226), Color(0xFF142A2F))
                                } else {
                                    listOf(Color(0xFFF9FBF2), Color(0xFFF0F6F6), Color(0xFFFFF8EF))
                                }
                            )
                        )
                        .padding(bottom = if (currentDestination?.route in topLevelRoutes) 8.dp else 0.dp)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = RootDestination.HOME.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(RootDestination.HOME.route) {
                            GroupsListScreen(
                                appContainer = appContainer,
                                onOpenGroup = { groupId -> navController.navigate("group/$groupId") }
                            )
                        }
                        composable(RootDestination.SETTINGS.route) {
                            SettingsScreen(
                                currentLanguage = settings.language,
                                currentTheme = settings.themeMode,
                                onLanguageSelected = appContainer.settingsRepository::updateLanguage,
                                onThemeSelected = appContainer.settingsRepository::updateThemeMode
                            )
                        }
                        composable(
                            route = "group/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                            GroupDashboardScreen(
                                appContainer = appContainer,
                                groupId = groupId,
                                onBack = { navController.popBackStack() },
                                onOpenMembers = { navController.navigate("members/$groupId") },
                                onAddExpense = { navController.navigate("expense/$groupId?expenseId=-1") },
                                onAddSettlement = { navController.navigate("settlement/$groupId?settlementId=-1") },
                                onOpenBalances = { navController.navigate("balances/$groupId") },
                                onOpenExpense = { expenseId -> navController.navigate("expenseDetail/$groupId/$expenseId") },
                                onEditSettlement = { settlementId -> navController.navigate("settlement/$groupId?settlementId=$settlementId") }
                            )
                        }
                        composable(
                            route = "members/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            MembersScreen(
                                appContainer = appContainer,
                                groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "expense/{groupId}?expenseId={expenseId}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.LongType },
                                navArgument("expenseId") { type = NavType.LongType; defaultValue = -1L }
                            )
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                            val expenseId = backStackEntry.arguments?.getLong("expenseId")?.takeIf { it > 0 }
                            AddEditExpenseScreen(
                                appContainer = appContainer,
                                groupId = groupId,
                                expenseId = expenseId,
                                onBack = { navController.popBackStack() },
                                onSaved = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "settlement/{groupId}?settlementId={settlementId}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.LongType },
                                navArgument("settlementId") { type = NavType.LongType; defaultValue = -1L }
                            )
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                            val settlementId = backStackEntry.arguments?.getLong("settlementId")?.takeIf { it > 0 }
                            AddSettlementScreen(
                                appContainer = appContainer,
                                groupId = groupId,
                                settlementId = settlementId,
                                onBack = { navController.popBackStack() },
                                onSaved = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "balances/{groupId}",
                            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            BalancesScreen(
                                appContainer = appContainer,
                                groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "expenseDetail/{groupId}/{expenseId}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.LongType },
                                navArgument("expenseId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            ExpenseDetailScreen(
                                appContainer = appContainer,
                                groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L,
                                expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L,
                                onBack = { navController.popBackStack() },
                                onEdit = { groupIdValue, expenseIdValue ->
                                    navController.navigate("expense/$groupIdValue?expenseId=$expenseIdValue")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
