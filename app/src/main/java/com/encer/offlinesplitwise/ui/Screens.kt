@file:OptIn(ExperimentalMaterial3Api::class)

package com.encer.offlinesplitwise.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.encer.offlinesplitwise.data.AppContainer
import com.encer.offlinesplitwise.domain.Expense
import com.encer.offlinesplitwise.domain.ExpenseShare
import com.encer.offlinesplitwise.domain.Member
import com.encer.offlinesplitwise.domain.MemberBalance
import com.encer.offlinesplitwise.domain.Settlement
import com.encer.offlinesplitwise.domain.SimplifiedTransfer
import com.encer.offlinesplitwise.domain.SplitType

private val amountVisualTransformation = GroupedNumberVisualTransformation()

@Composable
private fun appTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun appCardColors() = CardDefaults.elevatedCardColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    contentColor = MaterialTheme.colorScheme.onSurface
)

@Composable
private fun appFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.06f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupsListScreen(
    appContainer: AppContainer,
    onOpenGroup: (Long) -> Unit
) {
    val strings = appStrings()
    val viewModel: GroupsViewModel = viewModel(factory = groupsViewModelFactory(appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingGroupId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(strings.appTitle) },
                colors = appTopBarColors(),
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.addGroup)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroCard(
                    title = strings.homeHeroTitle,
                    subtitle = strings.homeHeroSubtitle,
                    icon = { Icon(Icons.Rounded.Groups, contentDescription = null) }
                )
            }
            items(uiState.groups, key = { it.id }) { group ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = appCardColors(),
                    onClick = { onOpenGroup(group.id) }
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Rounded.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(group.name, style = MaterialTheme.typography.titleLarge)
                                Text(formatDate(group.createdAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { editingGroupId = group.id }) {
                                Icon(Icons.Rounded.Edit, contentDescription = strings.edit)
                            }
                            IconButton(onClick = { viewModel.deleteGroup(group.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (uiState.groups.isEmpty()) {
                item {
                    EmptyStateCard(strings.noGroupsTitle, strings.noGroupsSubtitle)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showCreateDialog) {
        NameDialog(
            title = strings.newGroupTitle,
            initialValue = "",
            placeholder = strings.groupPlaceholder,
            confirmLabel = strings.createGroup,
            onDismiss = { showCreateDialog = false },
            onConfirm = {
                viewModel.createGroup(it)
                showCreateDialog = false
            }
        )
    }

    editingGroupId?.let { groupId ->
        val group = uiState.groups.firstOrNull { it.id == groupId }
        if (group != null) {
            NameDialog(
                title = strings.editGroupTitle,
                initialValue = group.name,
                placeholder = strings.groupPlaceholder,
                confirmLabel = strings.save,
                onDismiss = { editingGroupId = null },
                onConfirm = { name ->
                    viewModel.updateGroup(group.copy(name = name))
                    editingGroupId = null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupDashboardScreen(
    appContainer: AppContainer,
    groupId: Long,
    onBack: () -> Unit,
    onOpenMembers: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSettlement: () -> Unit,
    onOpenBalances: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onEditSettlement: (Long) -> Unit,
) {
    val strings = appStrings()
    val viewModel: GroupDashboardViewModel = viewModel(factory = groupDashboardFactory(groupId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: strings.groupFallbackTitle) },
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
                HeroCard(
                    title = strings.groupOverviewTitle,
                    subtitle = strings.groupOverviewSubtitle,
                    icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
                )
            }
            item {
                SummaryGrid(
                    items = listOf(
                        strings.totalExpenseLabel to formatAmount(uiState.summary.totalExpenses),
                        strings.membersLabel to formatAmountCompact(uiState.summary.membersCount),
                        strings.settlementsLabel to formatAmount(uiState.summary.totalSettlements),
                        strings.openBalancesLabel to formatAmountCompact(uiState.summary.openBalancesCount)
                    )
                )
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionChip(strings.membersAction, Icons.Rounded.PersonAddAlt, onOpenMembers)
                    ActionChip(strings.newExpenseAction, Icons.Rounded.Add, onAddExpense)
                    ActionChip(strings.addSettlementAction, Icons.Rounded.Payments, onAddSettlement)
                    ActionChip(strings.balancesAction, Icons.Rounded.SwapHoriz, onOpenBalances)
                }
            }
            item {
                SectionHeader(strings.recentExpensesTitle)
            }
            items(uiState.expenses.take(8), key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    members = uiState.members,
                    onClick = { onOpenExpense(expense.id) }
                )
            }
            if (uiState.expenses.isEmpty()) {
                item {
                    EmptyStateCard(strings.noExpensesTitle, strings.noExpensesSubtitle)
                }
            }
            item {
                SectionHeader(strings.recentSettlementsTitle)
            }
            items(uiState.settlements.take(8), key = { it.id }) { settlement ->
                SettlementCard(
                    settlement = settlement,
                    members = uiState.members,
                    onEdit = { onEditSettlement(settlement.id) },
                    onDelete = { viewModel.deleteSettlement(settlement.id) }
                )
            }
            if (uiState.settlements.isEmpty()) {
                item {
                    EmptyStateCard(strings.noSettlementsTitle, strings.noSettlementsSubtitle)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun MembersScreen(
    appContainer: AppContainer,
    groupId: Long,
    onBack: () -> Unit
) {
    val strings = appStrings()
    val viewModel: MembersViewModel = viewModel(factory = membersViewModelFactory(groupId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingMemberId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(strings.membersOfGroup(uiState.group?.name.orEmpty())) },
                colors = appTopBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Rounded.PersonAddAlt, contentDescription = strings.addMember)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.members, key = { it.id }) { member ->
                ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f), CircleShape)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(member.name.take(1), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.titleMedium)
                            Text(strings.memberSince(formatDate(member.createdAt)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { editingMemberId = member.id }) {
                            Icon(Icons.Rounded.Edit, contentDescription = strings.edit)
                        }
                        IconButton(onClick = { viewModel.deleteMember(member.id) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            if (uiState.members.isEmpty()) {
                item { EmptyStateCard(strings.noMembersTitle, strings.noMembersSubtitle) }
            }
        }
    }

    if (showDialog) {
        NameDialog(
            title = strings.addMember,
            initialValue = "",
            placeholder = strings.memberPlaceholder,
            confirmLabel = strings.saveMember,
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.addMember(it)
                showDialog = false
            }
        )
    }

    editingMemberId?.let { memberId ->
        uiState.members.firstOrNull { it.id == memberId }?.let { member ->
            NameDialog(
                title = strings.editMember,
                initialValue = member.name,
                placeholder = strings.memberPlaceholder,
                confirmLabel = strings.save,
                onDismiss = { editingMemberId = null },
                onConfirm = { name ->
                    viewModel.updateMember(member.copy(name = name))
                    editingMemberId = null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseScreen(
    appContainer: AppContainer,
    groupId: Long,
    expenseId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val strings = appStrings()
    val viewModel: ExpenseEditorViewModel = viewModel(factory = expenseEditorFactory(groupId, expenseId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(strings.message(it).orEmpty())
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(uiState.savedExpenseId) {
        if (uiState.savedExpenseId != null) {
            viewModel.markSavedConsumed()
            onSaved()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(strings.expenseFormTitle(expenseId != null)) },
                colors = appTopBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    if (expenseId != null) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroCard(
                title = strings.expenseHeroTitle,
                subtitle = strings.expenseHeroSubtitle,
                icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
            )
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.expenseTitleLabel) },
                colors = appFieldColors(),
                shape = RoundedCornerShape(20.dp)
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.expenseNoteLabel) },
                colors = appFieldColors(),
                shape = RoundedCornerShape(20.dp)
            )
            OutlinedTextField(
                value = uiState.totalAmountInput,
                onValueChange = viewModel::updateTotal,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.totalAmountLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = amountVisualTransformation,
                colors = appFieldColors(),
                shape = RoundedCornerShape(20.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = uiState.splitType == SplitType.EQUAL,
                    onClick = { viewModel.updateSplitType(SplitType.EQUAL) },
                    label = { Text(strings.equalSplitLabel) }
                )
                FilterChip(
                    selected = uiState.splitType == SplitType.EXACT,
                    onClick = { viewModel.updateSplitType(SplitType.EXACT) },
                    label = { Text(strings.exactSplitLabel) }
                )
            }
            SectionHeader(strings.membersAndPayersTitle)
            uiState.members.forEach { member ->
                MemberEditorCard(
                    member = member,
                    splitType = uiState.splitType,
                    equalSharePreview = buildEqualPreview(uiState, member.memberId),
                    onToggleIncluded = { included -> viewModel.toggleIncluded(member.memberId, included) },
                    onPayerChange = { value -> viewModel.updatePayer(member.memberId, value) },
                    onExactShareChange = { value -> viewModel.updateExactShare(member.memberId, value) }
                )
            }
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(strings.expenseFormAction(expenseId != null))
            }
        }
    }
}

private fun buildEqualPreview(uiState: ExpenseEditorUiState, memberId: Long): String {
    if (uiState.splitType != SplitType.EQUAL) return ""
    val totalAmount = parseAmountInput(uiState.totalAmountInput)
    val selectedIds = uiState.members.filter { it.includedInSplit }.map { it.memberId }
    val shares: List<ExpenseShare> = if (selectedIds.isEmpty()) {
        emptyList()
    } else {
        selectedIds.sorted().mapIndexed { index, id ->
            val base = totalAmount / selectedIds.size
            val extra = if (index < totalAmount % selectedIds.size) 1 else 0
            ExpenseShare(id, base + extra)
        }
    }
    val amount = shares.firstOrNull { it.memberId == memberId }?.amount ?: 0
    return if (amount > 0) formatAmount(amount) else "-"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSettlementScreen(
    appContainer: AppContainer,
    groupId: Long,
    settlementId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val strings = appStrings()
    val viewModel: SettlementEditorViewModel = viewModel(factory = settlementEditorFactory(groupId, settlementId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(strings.message(it).orEmpty())
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(strings.settlementFormTitle(settlementId != null)) },
                colors = appTopBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    if (settlementId != null) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HeroCard(
                title = strings.settlementHeroTitle,
                subtitle = strings.settlementHeroSubtitle,
                icon = { Icon(Icons.Rounded.Payments, contentDescription = null) }
            )
            SimpleMemberPicker(
                label = strings.payerLabel,
                members = uiState.members,
                selectedId = uiState.fromMemberId,
                onSelect = viewModel::setFromMember
            )
            SimpleMemberPicker(
                label = strings.receiverLabel,
                members = uiState.members,
                selectedId = uiState.toMemberId,
                onSelect = viewModel::setToMember
            )
            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = viewModel::setAmount,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.settlementAmountLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = amountVisualTransformation,
                colors = appFieldColors(),
                shape = RoundedCornerShape(20.dp)
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::setNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.noteLabel) },
                colors = appFieldColors(),
                shape = RoundedCornerShape(20.dp)
            )
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(strings.settlementFormAction(settlementId != null))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BalancesScreen(
    appContainer: AppContainer,
    groupId: Long,
    onBack: () -> Unit
) {
    val strings = appStrings()
    val viewModel: BalancesViewModel = viewModel(factory = balancesFactory(groupId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var simplify by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(strings.balancesOfGroup(uiState.group?.name.orEmpty())) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ElevatedCard(shape = RoundedCornerShape(28.dp), colors = appCardColors()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.optimizePaymentsTitle, style = MaterialTheme.typography.titleMedium)
                            Text(strings.optimizePaymentsSubtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = simplify, onCheckedChange = { simplify = it })
                    }
                }
            }
            item { SectionHeader(strings.memberBalanceTitle) }
            items(uiState.balances, key = { it.memberId }) { balance ->
                BalanceCard(balance)
            }
            if (simplify) {
                item { SectionHeader(strings.suggestedPaymentsTitle) }
                items(uiState.simplifiedTransfers, key = { "${it.fromMemberId}-${it.toMemberId}" }) { transfer ->
                    SimplifiedTransferCard(transfer)
                }
                if (uiState.simplifiedTransfers.isEmpty()) {
                    item { EmptyStateCard(strings.allSettledTitle, strings.allSettledSubtitle) }
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailScreen(
    appContainer: AppContainer,
    groupId: Long,
    expenseId: Long,
    onBack: () -> Unit,
    onEdit: (Long, Long) -> Unit
) {
    val strings = appStrings()
    val dashboardViewModel: GroupDashboardViewModel = viewModel(factory = groupDashboardFactory(groupId, appContainer))
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val expense = uiState.expenses.firstOrNull { it.id == expenseId }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(expense?.title ?: strings.expenseDetailsFallback) },
                colors = appTopBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    if (expense != null) {
                        IconButton(onClick = { onEdit(groupId, expenseId) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = strings.edit)
                        }
                        IconButton(onClick = {
                            dashboardViewModel.deleteExpense(expenseId)
                            onBack()
                        }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (expense == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.expenseNotFound)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeroCard(
                    title = expense.title,
                    subtitle = expense.note.ifBlank { strings.noDescription },
                    icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
                )
                SummaryGrid(
                    items = listOf(
                        strings.totalAmountStat to formatAmount(expense.totalAmount),
                        strings.splitMethodStat to strings.splitTypeLabel(expense.splitType == SplitType.EQUAL),
                        strings.dateStat to formatDate(expense.createdAt),
                        strings.peopleCountStat to formatAmountCompact(expense.shares.size)
                    )
                )
                SectionHeader(strings.payersTitle)
                expense.payers.forEach { payer ->
                    DetailLine(
                        label = memberName(uiState.members, payer.memberId),
                        value = formatAmount(payer.amount)
                    )
                }
                SectionHeader(strings.sharesTitle)
                expense.shares.forEach { share ->
                    DetailLine(
                        label = memberName(uiState.members, share.memberId),
                        value = formatAmount(share.amount)
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberEditorCard(
    member: MemberDraftUi,
    splitType: SplitType,
    equalSharePreview: String,
    onToggleIncluded: (Boolean) -> Unit,
    onPayerChange: (String) -> Unit,
    onExactShareChange: (String) -> Unit,
) {
    val strings = appStrings()
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = member.includedInSplit, onCheckedChange = onToggleIncluded)
            }
            OutlinedTextField(
                value = member.payerAmountInput,
                onValueChange = onPayerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.paidHowMuchLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = amountVisualTransformation,
                colors = appFieldColors(),
                shape = RoundedCornerShape(18.dp)
            )
            if (member.includedInSplit) {
                if (splitType == SplitType.EXACT) {
                    OutlinedTextField(
                        value = member.exactShareInput,
                        onValueChange = onExactShareChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.shareAmountLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = amountVisualTransformation,
                        colors = appFieldColors(),
                        shape = RoundedCornerShape(18.dp)
                    )
                } else {
                    DetailLine(label = strings.equalShareLabel, value = equalSharePreview)
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: MemberBalance) {
    val strings = appStrings()
    val tone = when {
        balance.netBalance > 0 -> Color(0xFF0F766E)
        balance.netBalance < 0 -> Color(0xFFB45309)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(balance.memberName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    text = strings.balanceState(balance.netBalance),
                    color = tone,
                    fontWeight = FontWeight.Bold
                )
            }
            SummaryGrid(
                items = listOf(
                    strings.paidStat to formatAmount(balance.paidTotal),
                    strings.owedStat to formatAmount(balance.owedTotal),
                    strings.netStat to formatAmount(kotlin.math.abs(balance.netBalance))
                )
            )
        }
    }
}

@Composable
private fun SimplifiedTransferCard(transfer: SimplifiedTransfer) {
    val strings = appStrings()
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.paymentSuggestion(transfer.fromMemberName, transfer.toMemberName), style = MaterialTheme.typography.titleMedium)
            Text(formatAmount(transfer.amount), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
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
                Text(formatAmount(expense.totalAmount), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(expense.note.ifBlank { strings.noDescription }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AssistChip(
                onClick = {},
                label = { Text(strings.payersSummary(expense.payers.joinToString(if (LocalAppLanguage.current == com.encer.offlinesplitwise.data.preferences.AppLanguage.FA) "، " else ", ") { memberName(members, it.memberId) })) }
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
                Text(formatAmount(settlement.amount), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            Text(settlement.note.ifBlank { strings.noDescription }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text(strings.edit) }
                OutlinedButton(onClick = onDelete, border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryGrid(items: List<Pair<String, String>>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2
    ) {
        items.forEach { (label, value) ->
            Card(
                modifier = Modifier.width(160.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    )
}

@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    ElevatedCard(shape = RoundedCornerShape(28.dp), colors = appCardColors()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HeroCard(title: String, subtitle: String, icon: @Composable () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = if (MaterialTheme.colorScheme.background.red < 0.2f) 0.26f else 0.15f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                androidx.compose.material3.ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface)) {
                    icon()
                }
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionChip(label: String, imageVector: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(imageVector, contentDescription = null) }
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimpleMemberPicker(
    label: String,
    members: List<Member>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    val strings = appStrings()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            members.forEach { member ->
                FilterChip(
                    selected = selectedId == member.id,
                    onClick = { onSelect(member.id) },
                    label = { Text(member.name) }
                )
            }
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val strings = appStrings()
    var value by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(placeholder) },
                shape = RoundedCornerShape(18.dp)
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentLanguage: com.encer.offlinesplitwise.data.preferences.AppLanguage,
    currentTheme: com.encer.offlinesplitwise.data.preferences.AppThemeMode,
    onLanguageSelected: (com.encer.offlinesplitwise.data.preferences.AppLanguage) -> Unit,
    onThemeSelected: (com.encer.offlinesplitwise.data.preferences.AppThemeMode) -> Unit,
) {
    val strings = appStrings()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = strings.settingsHeroTitle,
            subtitle = strings.settingsHeroSubtitle,
            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) }
        )
        ElevatedCard(shape = RoundedCornerShape(28.dp), colors = appCardColors()) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(strings.languageTitle, style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = currentLanguage == com.encer.offlinesplitwise.data.preferences.AppLanguage.FA,
                        onClick = { onLanguageSelected(com.encer.offlinesplitwise.data.preferences.AppLanguage.FA) },
                        label = { Text(strings.persianLabel) }
                    )
                    FilterChip(
                        selected = currentLanguage == com.encer.offlinesplitwise.data.preferences.AppLanguage.EN,
                        onClick = { onLanguageSelected(com.encer.offlinesplitwise.data.preferences.AppLanguage.EN) },
                        label = { Text(strings.englishLabel) }
                    )
                }
            }
        }
        ElevatedCard(shape = RoundedCornerShape(28.dp), colors = appCardColors()) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(strings.themeTitle, style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = currentTheme == com.encer.offlinesplitwise.data.preferences.AppThemeMode.LIGHT,
                        onClick = { onThemeSelected(com.encer.offlinesplitwise.data.preferences.AppThemeMode.LIGHT) },
                        label = { Text(strings.lightLabel) }
                    )
                    FilterChip(
                        selected = currentTheme == com.encer.offlinesplitwise.data.preferences.AppThemeMode.DARK,
                        onClick = { onThemeSelected(com.encer.offlinesplitwise.data.preferences.AppThemeMode.DARK) },
                        label = { Text(strings.darkLabel) }
                    )
                }
            }
        }
    }
}

private fun memberName(members: List<Member>, memberId: Long): String {
    return members.firstOrNull { it.id == memberId }?.name ?: if (java.util.Locale.getDefault().language == "fa") "کاربر $memberId" else "Member $memberId"
}
