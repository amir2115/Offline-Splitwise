package com.encer.offlinesplitwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.encer.offlinesplitwise.data.AppContainer

@Composable
fun OfflineSplitwiseApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF9FBF2), Color(0xFFF0F6F6), Color(0xFFFFF8EF))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            NavHost(navController = navController, startDestination = "groups") {
                composable("groups") {
                    GroupsListScreen(
                        appContainer = appContainer,
                        onOpenGroup = { groupId -> navController.navigate("group/$groupId") }
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
                        onSaved = {
                            navController.popBackStack()
                        }
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
