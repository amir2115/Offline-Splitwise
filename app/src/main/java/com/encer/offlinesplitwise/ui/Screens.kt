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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupsListScreen(
    appContainer: AppContainer,
    onOpenGroup: (Long) -> Unit
) {
    val viewModel: GroupsViewModel = viewModel(factory = groupsViewModelFactory(appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingGroupId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("خرج‌یار آفلاین") },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "افزودن گروه")
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
                    title = "خرج‌ها را آفلاین جمع کن",
                    subtitle = "برای هر سفر یا جمع، گروه بساز، اعضا را اضافه کن و بدهی‌ها را با حالت simplify ببین.",
                    icon = { Icon(Icons.Rounded.Groups, contentDescription = null) }
                )
            }
            items(uiState.groups, key = { it.id }) { group ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                Icon(Icons.Rounded.Edit, contentDescription = "ویرایش")
                            }
                            IconButton(onClick = { viewModel.deleteGroup(group.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (uiState.groups.isEmpty()) {
                item {
                    EmptyStateCard("هنوز گروهی نداری", "از دکمه بالا یک گروه جدید بساز.")
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showCreateDialog) {
        NameDialog(
            title = "گروه جدید",
            initialValue = "",
            placeholder = "مثلا سفر شمال",
            confirmLabel = "ساخت گروه",
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
                title = "ویرایش گروه",
                initialValue = group.name,
                placeholder = "نام گروه",
                confirmLabel = "ذخیره",
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
    val viewModel: GroupDashboardViewModel = viewModel(factory = groupDashboardFactory(groupId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: "گروه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroCard(
                    title = "وضعیت کلی گروه",
                    subtitle = "خرج‌ها، اعضا، تسویه‌ها و مانده‌های باز را از همین‌جا کنترل کن.",
                    icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
                )
            }
            item {
                SummaryGrid(
                    items = listOf(
                        "کل خرج" to formatAmount(uiState.summary.totalExpenses),
                        "اعضا" to formatAmountCompact(uiState.summary.membersCount),
                        "تسویه‌ها" to formatAmount(uiState.summary.totalSettlements),
                        "مانده باز" to formatAmountCompact(uiState.summary.openBalancesCount)
                    )
                )
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionChip("اعضا", Icons.Rounded.PersonAddAlt, onOpenMembers)
                    ActionChip("خرج جدید", Icons.Rounded.Add, onAddExpense)
                    ActionChip("ثبت تسویه", Icons.Rounded.Payments, onAddSettlement)
                    ActionChip("مانده‌ها", Icons.Rounded.SwapHoriz, onOpenBalances)
                }
            }
            item {
                SectionHeader("آخرین خرج‌ها")
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
                    EmptyStateCard("خرجی ثبت نشده", "اولین خرج گروه را ثبت کن تا مانده‌ها محاسبه شوند.")
                }
            }
            item {
                SectionHeader("تسویه‌های اخیر")
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
                    EmptyStateCard("تسویه‌ای ثبت نشده", "وقتی کسی بدهی‌اش را داد، از اینجا ثبتش کن.")
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
    val viewModel: MembersViewModel = viewModel(factory = membersViewModelFactory(groupId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingMemberId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("اعضای ${uiState.group?.name.orEmpty()}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Rounded.PersonAddAlt, contentDescription = "افزودن عضو")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.members, key = { it.id }) { member ->
                ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface)) {
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
                            Text("عضو از ${formatDate(member.createdAt)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { editingMemberId = member.id }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "ویرایش")
                        }
                        IconButton(onClick = { viewModel.deleteMember(member.id) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            if (uiState.members.isEmpty()) {
                item { EmptyStateCard("هیچ عضوی ثبت نشده", "برای ثبت خرج حداقل یک عضو نیاز داری.") }
            }
        }
    }

    if (showDialog) {
        NameDialog(
            title = "افزودن عضو",
            initialValue = "",
            placeholder = "نام عضو",
            confirmLabel = "ثبت عضو",
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
                title = "ویرایش عضو",
                initialValue = member.name,
                placeholder = "نام عضو",
                confirmLabel = "ذخیره",
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
    val viewModel: ExpenseEditorViewModel = viewModel(factory = expenseEditorFactory(groupId, expenseId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
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
                title = { Text(if (expenseId == null) "خرج جدید" else "ویرایش خرج") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (expenseId != null) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
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
                title = "خرج را دقیق ثبت کن",
                subtitle = "می‌توانی مشخص کنی چه کسی پرداخت کرده و سهم هر نفر چقدر بوده.",
                icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
            )
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("عنوان خرج") },
                shape = RoundedCornerShape(20.dp)
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("توضیح") },
                shape = RoundedCornerShape(20.dp)
            )
            OutlinedTextField(
                value = uiState.totalAmountInput,
                onValueChange = viewModel::updateTotal,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ کل (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                shape = RoundedCornerShape(20.dp)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = uiState.splitType == SplitType.EQUAL,
                    onClick = { viewModel.updateSplitType(SplitType.EQUAL) },
                    label = { Text("تقسیم مساوی") }
                )
                FilterChip(
                    selected = uiState.splitType == SplitType.EXACT,
                    onClick = { viewModel.updateSplitType(SplitType.EXACT) },
                    label = { Text("مبلغ دقیق") }
                )
            }
            SectionHeader("اعضا و پرداخت‌کننده‌ها")
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
                Text(if (expenseId == null) "ثبت خرج" else "ذخیره تغییرات")
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
    val viewModel: SettlementEditorViewModel = viewModel(factory = settlementEditorFactory(groupId, settlementId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
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
                title = { Text(if (settlementId == null) "ثبت تسویه" else "ویرایش تسویه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (settlementId != null) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
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
                title = "تسویه واقعی را ثبت کن",
                subtitle = "وقتی یکی بدهی‌اش را به دیگری پرداخت کرد، اینجا ثبتش کن تا مانده‌ها اصلاح شوند.",
                icon = { Icon(Icons.Rounded.Payments, contentDescription = null) }
            )
            SimpleMemberPicker(
                label = "پرداخت‌کننده",
                members = uiState.members,
                selectedId = uiState.fromMemberId,
                onSelect = viewModel::setFromMember
            )
            SimpleMemberPicker(
                label = "دریافت‌کننده",
                members = uiState.members,
                selectedId = uiState.toMemberId,
                onSelect = viewModel::setToMember
            )
            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = viewModel::setAmount,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("مبلغ تسویه") },
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                shape = RoundedCornerShape(20.dp)
            )
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::setNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("یادداشت") },
                shape = RoundedCornerShape(20.dp)
            )
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(if (settlementId == null) "ثبت تسویه" else "ذخیره تغییرات")
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
    val viewModel: BalancesViewModel = viewModel(factory = balancesFactory(groupId, appContainer))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var simplify by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("مانده‌های ${uiState.group?.name.orEmpty()}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
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
                ElevatedCard(shape = RoundedCornerShape(28.dp), colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("حالت پرداخت بهینه", style = MaterialTheme.typography.titleMedium)
                            Text("کمترین تعداد پرداخت پیشنهادی را نشان می‌دهد.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = simplify, onCheckedChange = { simplify = it })
                    }
                }
            }
            item { SectionHeader("مانده هر نفر") }
            items(uiState.balances, key = { it.memberId }) { balance ->
                BalanceCard(balance)
            }
            if (simplify) {
                item { SectionHeader("پرداخت‌های پیشنهادی") }
                items(uiState.simplifiedTransfers, key = { "${it.fromMemberId}-${it.toMemberId}" }) { transfer ->
                    SimplifiedTransferCard(transfer)
                }
                if (uiState.simplifiedTransfers.isEmpty()) {
                    item { EmptyStateCard("همه‌چیز تسویه است", "در این گروه پرداخت باز باقی نمانده.") }
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
    val dashboardViewModel: GroupDashboardViewModel = viewModel(factory = groupDashboardFactory(groupId, appContainer))
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val expense = uiState.expenses.firstOrNull { it.id == expenseId }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(expense?.title ?: "جزئیات خرج") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (expense != null) {
                        IconButton(onClick = { onEdit(groupId, expenseId) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "ویرایش")
                        }
                        IconButton(onClick = {
                            dashboardViewModel.deleteExpense(expenseId)
                            onBack()
                        }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (expense == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("خرج پیدا نشد.")
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
                    subtitle = expense.note.ifBlank { "بدون توضیح" },
                    icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
                )
                SummaryGrid(
                    items = listOf(
                        "مبلغ کل" to formatAmount(expense.totalAmount),
                        "روش تقسیم" to if (expense.splitType == SplitType.EQUAL) "مساوی" else "دقیق",
                        "تاریخ" to formatDate(expense.createdAt),
                        "تعداد افراد" to formatAmountCompact(expense.shares.size)
                    )
                )
                SectionHeader("پرداخت‌کننده‌ها")
                expense.payers.forEach { payer ->
                    DetailLine(
                        label = memberName(uiState.members, payer.memberId),
                        value = formatAmount(payer.amount)
                    )
                }
                SectionHeader("سهم افراد")
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
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = member.includedInSplit, onCheckedChange = onToggleIncluded)
            }
            OutlinedTextField(
                value = member.payerAmountInput,
                onValueChange = onPayerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("چقدر پرداخت کرده؟") },
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                shape = RoundedCornerShape(18.dp)
            )
            if (member.includedInSplit) {
                if (splitType == SplitType.EXACT) {
                    OutlinedTextField(
                        value = member.exactShareInput,
                        onValueChange = onExactShareChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("سهم این نفر") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(18.dp)
                    )
                } else {
                    DetailLine(label = "سهم مساوی", value = equalSharePreview)
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: MemberBalance) {
    val tone = when {
        balance.netBalance > 0 -> Color(0xFF0F766E)
        balance.netBalance < 0 -> Color(0xFFB45309)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(balance.memberName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    text = when {
                        balance.netBalance > 0 -> "طلبکار"
                        balance.netBalance < 0 -> "بدهکار"
                        else -> "تسویه"
                    },
                    color = tone,
                    fontWeight = FontWeight.Bold
                )
            }
            SummaryGrid(
                items = listOf(
                    "پرداخت" to formatAmount(balance.paidTotal),
                    "سهم" to formatAmount(balance.owedTotal),
                    "خالص" to formatAmount(kotlin.math.abs(balance.netBalance))
                )
            )
        }
    }
}

@Composable
private fun SimplifiedTransferCard(transfer: SimplifiedTransfer) {
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${transfer.fromMemberName} باید به ${transfer.toMemberName} پرداخت کند", style = MaterialTheme.typography.titleMedium)
            Text(formatAmount(transfer.amount), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, members: List<Member>, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(expense.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(formatAmount(expense.totalAmount), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(expense.note.ifBlank { "بدون توضیح" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AssistChip(onClick = {}, label = { Text("پرداخت‌کننده‌ها: ${expense.payers.joinToString("، ") { memberName(members, it.memberId) }}") })
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
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${memberName(members, settlement.fromMemberId)} به ${memberName(members, settlement.toMemberId)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(formatAmount(settlement.amount), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            Text(settlement.note.ifBlank { "بدون توضیح" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("ویرایش") }
                OutlinedButton(onClick = onDelete, border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    ElevatedCard(shape = RoundedCornerShape(28.dp), colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.surface)) {
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
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF102A31))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                androidx.compose.material3.ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(color = Color.White)) {
                    icon()
                }
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.82f))
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

@Composable
private fun SimpleMemberPicker(
    label: String,
    members: List<Member>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
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
                Text("انصراف")
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

private fun memberName(members: List<Member>, memberId: Long): String {
    return members.firstOrNull { it.id == memberId }?.name ?: "کاربر $memberId"
}
