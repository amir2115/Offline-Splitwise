package com.encer.offlinesplitwise.features.group_dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.domain.model.Expense
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.domain.model.Settlement
import com.encer.offlinesplitwise.domain.model.memberName
import com.encer.offlinesplitwise.ui.components.AmountText
import com.encer.offlinesplitwise.ui.components.AppAnimatedSection
import com.encer.offlinesplitwise.ui.components.AppAnimatedVisibility
import com.encer.offlinesplitwise.ui.components.AppInlineMessageCard
import com.encer.offlinesplitwise.ui.components.DashboardHeroCard
import com.encer.offlinesplitwise.ui.components.EmptyStateCard
import com.encer.offlinesplitwise.ui.components.SectionHeader
import com.encer.offlinesplitwise.ui.components.appAssistChipColors
import com.encer.offlinesplitwise.ui.components.appCardColors
import com.encer.offlinesplitwise.ui.components.appHeroSectionEnter
import com.encer.offlinesplitwise.ui.components.appHiltViewModel
import com.encer.offlinesplitwise.ui.components.appOutlinedButtonColors
import com.encer.offlinesplitwise.ui.components.appSectionEnter
import com.encer.offlinesplitwise.ui.components.appTopBarColors
import com.encer.offlinesplitwise.ui.formatting.formatAmount
import com.encer.offlinesplitwise.ui.formatting.formatAmountCompact
import com.encer.offlinesplitwise.ui.localization.LocalAppLanguage
import com.encer.offlinesplitwise.ui.localization.appStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GroupDashboardScreen(
    groupId: String,
    onBack: () -> Unit,
    onOpenMembers: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSettlement: () -> Unit,
    onOpenBalances: () -> Unit,
    onOpenExpense: (String) -> Unit,
    onEditSettlement: (String) -> Unit,
) {
    val viewModel: GroupDashboardViewModel = appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GroupDashboardContent(
        uiState = uiState,
        groupId = groupId,
        onBack = onBack,
        onOpenMembers = onOpenMembers,
        onAddExpense = onAddExpense,
        onAddSettlement = onAddSettlement,
        onOpenBalances = onOpenBalances,
        onOpenExpense = onOpenExpense,
        onEditSettlement = onEditSettlement,
        onDeleteSettlement = { viewModel.deleteSettlement(it) },
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun GroupDashboardContent(
    uiState: GroupDashboardUiState,
    groupId: String,
    onBack: () -> Unit,
    onOpenMembers: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSettlement: () -> Unit,
    onOpenBalances: () -> Unit,
    onOpenExpense: (String) -> Unit,
    onEditSettlement: (String) -> Unit,
    onDeleteSettlement: (String) -> Unit,
) {
    val strings = appStrings()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var contentVisible by remember(groupId) { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        contentVisible = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: strings.groupFallbackTitle, style = MaterialTheme.typography.titleLarge) },
                colors = appTopBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter()) {
                    DashboardHeroCard(
                        title = strings.groupOverviewTitle,
                        subtitle = strings.groupOverviewSubtitle,
                        icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
                    )
                }
            }
            item {
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 70)) {
                    SummaryGrid(
                        items = listOf(
                            strings.totalExpenseLabel to formatAmount(uiState.summary.totalExpenses),
                            strings.membersLabel to formatAmountCompact(uiState.summary.membersCount),
                            strings.settlementsLabel to formatAmount(uiState.summary.totalSettlements),
                            strings.openBalancesLabel to formatAmountCompact(uiState.summary.openBalancesCount)
                        )
                    )
                }
            }
            item {
                AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter(delayMillis = 120)) {
                    QuickActionsGrid(
                        actions = listOf(
                            QuickActionItem(
                                label = strings.membersAction,
                                imageVector = Icons.Rounded.PersonAddAlt,
                                onClick = onOpenMembers
                            ),
                            QuickActionItem(
                                label = strings.newExpenseAction,
                                imageVector = Icons.Rounded.Add,
                                enabled = uiState.canCreateTransactions,
                                onClick = {
                                    if (uiState.canCreateTransactions) {
                                        onAddExpense()
                                    } else {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(strings.needSecondMemberMessage) }
                                    }
                                }
                            ),
                            QuickActionItem(
                                label = strings.addSettlementAction,
                                imageVector = Icons.Rounded.Payments,
                                enabled = uiState.canCreateTransactions,
                                onClick = {
                                    if (uiState.canCreateTransactions) {
                                        onAddSettlement()
                                    } else {
                                        coroutineScope.launch { snackbarHostState.showSnackbar(strings.needSecondMemberMessage) }
                                    }
                                }
                            ),
                            QuickActionItem(
                                label = strings.balancesAction,
                                imageVector = Icons.Rounded.SwapHoriz,
                                onClick = onOpenBalances
                            )
                        ),
                        visible = contentVisible
                    )
                }
            }
            item {
                AppAnimatedVisibility(visible = !uiState.canCreateTransactions) {
                    AppInlineMessageCard(
                        text = strings.needSecondMemberMessage,
                        isError = false
                    )
                }
            }
            item {
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 170)) {
                    SectionHeader(strings.recentExpensesTitle)
                }
            }
            items(uiState.expenses.take(8), key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    members = uiState.members,
                    onClick = { onOpenExpense(expense.id) }
                )
            }
            item {
                AppAnimatedVisibility(visible = uiState.expenses.isEmpty()) {
                    EmptyStateCard(strings.noExpensesTitle, strings.noExpensesSubtitle)
                }
            }
            item {
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 210)) {
                    SectionHeader(strings.recentSettlementsTitle)
                }
            }
            items(uiState.settlements.take(8), key = { it.id }) { settlement ->
                SettlementCard(
                    settlement = settlement,
                    members = uiState.members,
                    onEdit = { onEditSettlement(settlement.id) },
                    onDelete = { onDeleteSettlement(settlement.id) }
                )
            }
            item {
                AppAnimatedVisibility(visible = uiState.settlements.isEmpty()) {
                    EmptyStateCard(strings.noSettlementsTitle, strings.noSettlementsSubtitle)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, members: List<Member>, onClick: () -> Unit) {
    val strings = appStrings()
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = appCardColors(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(expense.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AmountText(
                    amount = expense.totalAmount,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(expense.note?.ifBlank { strings.noDescription } ?: strings.noDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AssistChip(
                onClick = {},
                colors = appAssistChipColors(),
                label = {
                    Text(
                        strings.payersSummary(expense.payers.joinToString(if (LocalAppLanguage.current == com.encer.offlinesplitwise.data.preferences.AppLanguage.FA) "، " else ", ") { memberName(members, it.memberId) }),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Composable
private fun SettlementCard(
    settlement: Settlement,
    members: List<Member>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = appStrings()
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.settlementSummary(memberName(members, settlement.fromMemberId), memberName(members, settlement.toMemberId)), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                AmountText(
                    amount = settlement.amount,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(settlement.note?.ifBlank { strings.noDescription } ?: strings.noDescription, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    colors = appOutlinedButtonColors()
                ) {
                    Text(strings.edit, style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onDelete,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private data class QuickActionItem(
    val label: String,
    val imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionsGrid(actions: List<QuickActionItem>, visible: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        actions.chunked(2).forEachIndexed { rowIndex, rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowActions.forEachIndexed { itemIndex, action ->
                    val index = rowIndex * 2 + itemIndex
                    Box(modifier = Modifier.weight(1f)) {
                        AppAnimatedSection(
                            visible = visible,
                            enter = appHeroSectionEnter(delayMillis = 160 + (index * 45))
                        ) {
                            QuickActionCard(
                                modifier = Modifier.fillMaxWidth(),
                                label = action.label,
                                imageVector = action.imageVector,
                                enabled = action.enabled,
                                onClick = action.onClick
                            )
                        }
                    }
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    label: String,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .height(94.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = appCardColors(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryGrid(items: List<Pair<String, String>>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { (label, value) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 0.82f else 0.9f
                            ),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                value,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
