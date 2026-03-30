package com.encer.splitwise.features.group_dashboard

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
import androidx.compose.material3.HorizontalDivider
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
import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.domain.model.Settlement
import com.encer.splitwise.domain.model.memberName
import com.encer.splitwise.ui.components.AmountText
import com.encer.splitwise.ui.components.AppAnimatedSection
import com.encer.splitwise.ui.components.AppAnimatedVisibility
import com.encer.splitwise.ui.components.AppInlineMessageCard
import com.encer.splitwise.ui.components.DashboardHeroCard
import com.encer.splitwise.ui.components.DetailLine
import com.encer.splitwise.ui.components.EmptyStateCard
import com.encer.splitwise.ui.components.SectionHeader
import com.encer.splitwise.ui.components.appBackgroundBrush
import com.encer.splitwise.ui.components.appBlendOverBackground
import com.encer.splitwise.ui.components.appAssistChipColors
import com.encer.splitwise.ui.components.appCardColors
import com.encer.splitwise.ui.components.appHeroSectionEnter
import com.encer.splitwise.ui.components.appHiltViewModel
import com.encer.splitwise.ui.components.appOutlinedButtonColors
import com.encer.splitwise.ui.components.appSectionEnter
import com.encer.splitwise.ui.components.appTopBarColors
import com.encer.splitwise.ui.formatting.formatAmount
import com.encer.splitwise.ui.formatting.formatAmountCompact
import com.encer.splitwise.ui.formatting.formatDate
import com.encer.splitwise.ui.localization.LocalAppLanguage
import com.encer.splitwise.ui.localization.appStrings
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
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundBrush(isDark = MaterialTheme.colorScheme.background.luminance() <= 0.5f))
        ) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appBlendOverBackground(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            alpha = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 0.78f else 0.88f
                        ),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(strings.dateStat, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatDate(expense.createdAt), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appBlendOverBackground(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            alpha = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) 0.78f else 0.88f
                        ),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(strings.splitMethodStat, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(strings.splitTypeLabel(expense.splitType == com.encer.splitwise.domain.model.SplitType.EQUAL), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            DetailLine(strings.peopleCountStat, formatAmountCompact(expense.shares.size))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            Text(strings.payersTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            expense.payers.forEach { payer ->
                DetailLine(memberName(members, payer.memberId), formatAmount(payer.amount))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            Text(strings.sharesTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            expense.shares.forEach { share ->
                DetailLine(memberName(members, share.memberId), formatAmount(share.amount))
            }
            AssistChip(
                onClick = {},
                colors = appAssistChipColors(),
                label = {
                    Text(
                        strings.payersSummary(expense.payers.joinToString(if (LocalAppLanguage.current == com.encer.splitwise.data.preferences.AppLanguage.FA) "، " else ", ") { memberName(members, it.memberId) }),
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
                            containerColor = appBlendOverBackground(
                                color = MaterialTheme.colorScheme.surfaceVariant,
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
