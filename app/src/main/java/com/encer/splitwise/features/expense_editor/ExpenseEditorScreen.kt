package com.encer.splitwise.features.expense_editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Percent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.splitwise.core.common.MessageKey
import com.encer.splitwise.domain.model.ExpenseShare
import com.encer.splitwise.domain.model.SplitType
import com.encer.splitwise.ui.components.AppAnimatedSection
import com.encer.splitwise.ui.components.AppAnimatedVisibility
import com.encer.splitwise.ui.components.AppInlineMessageCard
import com.encer.splitwise.ui.components.CalculatorAmountField
import com.encer.splitwise.ui.components.DashboardHeroCard
import com.encer.splitwise.ui.components.DetailLine
import com.encer.splitwise.ui.components.SectionHeader
import com.encer.splitwise.ui.components.appAssistChipColors
import com.encer.splitwise.ui.components.appCardColors
import com.encer.splitwise.ui.components.appFieldColors
import com.encer.splitwise.ui.components.appFilterChipColors
import com.encer.splitwise.ui.components.appHeroSectionEnter
import com.encer.splitwise.ui.components.appHiltViewModel
import com.encer.splitwise.ui.components.appOutlinedButtonColors
import com.encer.splitwise.ui.components.appPlainCardColors
import com.encer.splitwise.ui.components.appPrimaryButtonColors
import com.encer.splitwise.ui.components.appSectionEnter
import com.encer.splitwise.ui.components.appSwitchColors
import com.encer.splitwise.ui.components.appTopBarColors
import com.encer.splitwise.ui.formatting.formatAmount
import com.encer.splitwise.ui.formatting.parseAmountInput
import com.encer.splitwise.ui.formatting.parseAmountInputOrNull
import com.encer.splitwise.ui.localization.appStrings

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorScreen(
    groupId: String,
    expenseId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val strings = appStrings()
    val viewModel: ExpenseEditorViewModel = appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val inlineErrorMessage = strings.message(uiState.message?.takeIf { it != MessageKey.EXPENSE_SAVED })
    var contentVisible by remember(expenseId, groupId) { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.takeIf { it == MessageKey.EXPENSE_SAVED }?.let {
            snackbarHostState.showSnackbar(strings.message(it).orEmpty())
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(groupId, expenseId) {
        contentVisible = true
    }
    LaunchedEffect(uiState.savedExpenseId) {
        if (uiState.savedExpenseId != null) {
            viewModel.markSavedConsumed()
            onSaved()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(strings.expenseFormTitle(expenseId != null), style = MaterialTheme.typography.titleLarge) },
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
            AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter()) {
                DashboardHeroCard(
                    title = strings.expenseHeroTitle,
                    subtitle = strings.expenseHeroSubtitle,
                    icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
                )
            }
            AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 60)) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = appPlainCardColors(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = viewModel::updateTitle,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.expenseTitleLabel, style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp)
                        )
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = viewModel::updateNote,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.expenseNoteLabel, style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp)
                        )
                        CalculatorAmountField(
                            value = uiState.totalAmountInput,
                            onValueChange = viewModel::updateTotal,
                            modifier = Modifier.fillMaxWidth(),
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp),
                            label = strings.totalAmountLabel,
                        )
                        TaxConfigurationSection(
                            taxEnabled = uiState.taxEnabled,
                            taxPercentInput = uiState.taxPercentInput,
                            baseAmountPreview = uiState.baseAmountPreview,
                            taxAmountPreview = uiState.taxAmountPreview,
                            onTaxEnabledChange = viewModel::updateTaxEnabled,
                            onTaxPercentChange = viewModel::updateTaxPercent,
                        )
                        ServiceChargesSection(
                            serviceCharges = uiState.serviceCharges,
                            members = uiState.members,
                            onAdd = viewModel::addServiceCharge,
                            onRemove = viewModel::removeServiceCharge,
                            onTitleChange = viewModel::updateServiceChargeTitle,
                            onAmountChange = viewModel::updateServiceChargeAmount,
                            onToggleMember = viewModel::toggleServiceChargeMember,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = uiState.splitType == SplitType.EQUAL,
                                onClick = { viewModel.updateSplitType(SplitType.EQUAL) },
                                label = { Text(strings.equalSplitLabel, style = MaterialTheme.typography.labelLarge) },
                                colors = appFilterChipColors()
                            )
                            FilterChip(
                                selected = uiState.splitType == SplitType.EXACT,
                                onClick = { viewModel.updateSplitType(SplitType.EXACT) },
                                label = { Text(strings.exactSplitLabel, style = MaterialTheme.typography.labelLarge) },
                                colors = appFilterChipColors()
                            )
                        }
                    }
                }
            }
            AppAnimatedVisibility(visible = inlineErrorMessage != null) {
                AppInlineMessageCard(
                    text = inlineErrorMessage.orEmpty(),
                    isError = true
                )
            }
            AppAnimatedVisibility(visible = !uiState.canCreateTransaction) {
                AppInlineMessageCard(
                    text = strings.needSecondMemberMessage,
                    isError = false
                )
            }
            AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 100)) {
                SectionHeader(strings.membersAndPayersTitle)
            }
            AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 115)) {
                ExpenseLiveSummaryCard(uiState = uiState)
            }
            uiState.members.forEach { member ->
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 130)) {
                    MemberEditorCard(
                        member = member,
                        uiState = uiState,
                        onToggleIncluded = { included -> viewModel.toggleIncluded(member.memberId, included) },
                        onPayerChange = { value -> viewModel.updatePayer(member.memberId, value) },
                        onExactShareChange = { value -> viewModel.updateExactShare(member.memberId, value) },
                        onAssignFullAmount = { viewModel.assignFullAmountToPayer(member.memberId) },
                        onClearPayerAmount = { viewModel.clearPayerAmount(member.memberId) },
                        onApplySuggestedPayer = { viewModel.applySuggestedPayer(member.memberId) },
                        onApplySuggestedShare = { viewModel.applySuggestedShare(member.memberId) },
                        onApplyEqualRemainingShare = { viewModel.applyEqualRemainingShare(member.memberId) }
                    )
                }
            }
            AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter(delayMillis = 170)) {
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = appPrimaryButtonColors(),
                    enabled = uiState.canCreateTransaction
                ) {
                    Text(
                        if (uiState.isAmountsReady && uiState.title.isNotBlank()) {
                            strings.expenseFormAction(expenseId != null)
                        } else {
                            strings.completeExpenseForm
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberEditorCard(
    member: MemberDraftUi,
    uiState: ExpenseEditorUiState,
    onToggleIncluded: (Boolean) -> Unit,
    onPayerChange: (String) -> Unit,
    onExactShareChange: (String) -> Unit,
    onAssignFullAmount: () -> Unit,
    onClearPayerAmount: () -> Unit,
    onApplySuggestedPayer: () -> Unit,
    onApplySuggestedShare: () -> Unit,
    onApplyEqualRemainingShare: () -> Unit,
) {
    val strings = appStrings()
    val hasPositiveTotal = (parseAmountInputOrNull(uiState.totalAmountInput) ?: 0) > 0
    val hasPayerAmount = member.payerAmountInput.isNotBlank()
    val receiptItems = remember(uiState.taxAmountPreview, uiState.serviceCharges, uiState.members, member.memberId) {
        buildMemberReceiptItems(
            uiState = uiState,
            member = member,
            taxLabel = strings.taxAmountLabel,
        ) { index ->
            strings.serviceChargeItemTitle(index)
        }
    }
    var receiptItemsExpanded by rememberSaveable(member.memberId) { mutableStateOf(false) }
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("@${member.username}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = member.includedInSplit,
                    onCheckedChange = onToggleIncluded,
                    colors = appSwitchColors()
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(
                    onClick = onAssignFullAmount,
                    enabled = hasPositiveTotal,
                    colors = appAssistChipColors(),
                    label = { Text(strings.payFullAmountLabel, style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null) }
                )
                AppAnimatedVisibility(visible = member.suggestedRemainingPayer != null) {
                    AssistChip(
                        onClick = onApplySuggestedPayer,
                        colors = appAssistChipColors(),
                        label = {
                            Text(
                                strings.applyRemainingPayer(formatAmount(member.suggestedRemainingPayer ?: 0)),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
                AppAnimatedVisibility(visible = hasPayerAmount) {
                    OutlinedButton(onClick = onClearPayerAmount, colors = appOutlinedButtonColors()) {
                        Text(strings.clearAmountLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            CalculatorAmountField(
                value = member.payerAmountInput,
                onValueChange = onPayerChange,
                modifier = Modifier.fillMaxWidth(),
                colors = appFieldColors(),
                shape = RoundedCornerShape(18.dp),
                label = strings.paidHowMuchLabel,
            )
            FieldHelperText(
                text = when {
                    uiState.isPayerOverflow -> strings.payerOverflowMessage(formatAmount(-uiState.remainingPayerAmount))
                    member.suggestedRemainingPayer != null -> strings.payerRemainingHint(formatAmount(member.suggestedRemainingPayer ?: 0))
                    else -> strings.paidHowMuchHint
                },
                isError = uiState.isPayerOverflow
            )
            if (member.includedInSplit) {
                if (uiState.splitType == SplitType.EXACT) {
                    CalculatorAmountField(
                        value = member.exactShareInput,
                        onValueChange = onExactShareChange,
                        modifier = Modifier.fillMaxWidth(),
                        colors = appFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        label = strings.baseShareLabel,
                    )
                    FieldHelperText(
                        text = when {
                            uiState.isShareOverflow -> strings.shareOverflowMessage(formatAmount(-uiState.remainingShareAmount))
                            member.suggestedRemainingShare != null -> strings.shareRemainingHint(formatAmount(member.suggestedRemainingShare ?: 0))
                            else -> strings.shareAmountHint
                        },
                        isError = uiState.isShareOverflow
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppAnimatedVisibility(visible = member.suggestedRemainingShare != null) {
                            AssistChip(
                                onClick = onApplySuggestedShare,
                                colors = appAssistChipColors(),
                                label = {
                                    Text(
                                        strings.applyRemainingShare(formatAmount(member.suggestedRemainingShare ?: 0)),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            )
                        }
                        AppAnimatedVisibility(visible = member.equalRemainingShare != null) {
                            AssistChip(
                                onClick = onApplyEqualRemainingShare,
                                colors = appAssistChipColors(),
                                label = {
                                    Text(
                                        strings.applyEqualRemainingShare(formatAmount(member.equalRemainingShare ?: 0)),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            )
                        }
                    }
                } else {
                    DetailLine(label = strings.baseShareLabel, value = formatAmount(member.baseSharePreview))
                }
                DetailLine(label = strings.finalShareLabel, value = formatAmount(member.finalSharePreview))
                AppAnimatedVisibility(visible = receiptItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { receiptItemsExpanded = !receiptItemsExpanded },
                            colors = appOutlinedButtonColors(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                if (receiptItemsExpanded) strings.hideDetailsLabel else strings.showDetailsLabel,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (receiptItemsExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AppAnimatedVisibility(visible = receiptItemsExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (member.taxSharePreview > 0) {
                                    DetailLine(label = strings.taxShareLabel, value = formatAmount(member.taxSharePreview))
                                }
                                if (member.serviceChargeSharePreview > 0) {
                                    DetailLine(label = strings.serviceChargeShareLabel, value = formatAmount(member.serviceChargeSharePreview))
                                }
                                receiptItems.forEach { (label, amount) ->
                                    DetailLine(label = label, value = formatAmount(amount))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxConfigurationSection(
    taxEnabled: Boolean,
    taxPercentInput: String,
    baseAmountPreview: Int,
    taxAmountPreview: Int,
    onTaxEnabledChange: (Boolean) -> Unit,
    onTaxPercentChange: (String) -> Unit,
) {
    val strings = appStrings()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(strings.taxToggleTitle, style = MaterialTheme.typography.titleMedium)
                Text(strings.taxToggleSubtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = taxEnabled,
                onCheckedChange = onTaxEnabledChange,
                colors = appSwitchColors()
            )
        }
        AppAnimatedVisibility(visible = taxEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CalculatorAmountField(
                    value = taxPercentInput,
                    onValueChange = onTaxPercentChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(18.dp),
                    label = strings.taxPercentLabel,
                )
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(strings.taxBreakdownTitle, style = MaterialTheme.typography.titleMedium)
                        }
                        DetailLine(strings.baseAmountLabel, formatAmount(baseAmountPreview))
                        DetailLine(strings.taxAmountLabel, formatAmount(taxAmountPreview))
                        DetailLine(strings.totalAmountStat, formatAmount(baseAmountPreview + taxAmountPreview))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceChargesSection(
    serviceCharges: List<ServiceChargeDraftUi>,
    members: List<MemberDraftUi>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onTitleChange: (String, String) -> Unit,
    onAmountChange: (String, String) -> Unit,
    onToggleMember: (String, String) -> Unit,
) {
    val strings = appStrings()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(strings.serviceChargesTitle, style = MaterialTheme.typography.titleMedium)
                Text(strings.serviceChargesSubtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = onAdd, colors = appOutlinedButtonColors()) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Text(strings.addServiceChargeLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
        serviceCharges.forEachIndexed { index, charge ->
            ServiceChargeEditorCard(
                title = charge.title,
                amountInput = charge.amountInput,
                members = members,
                selectedMemberIds = charge.selectedMemberIds,
                serviceIndex = index + 1,
                onRemove = { onRemove(charge.id) },
                onTitleChange = { onTitleChange(charge.id, it) },
                onAmountChange = { onAmountChange(charge.id, it) },
                onToggleMember = { memberId -> onToggleMember(charge.id, memberId) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceChargeEditorCard(
    title: String,
    amountInput: String,
    members: List<MemberDraftUi>,
    selectedMemberIds: Set<String>,
    serviceIndex: Int,
    onRemove: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onToggleMember: (String) -> Unit,
) {
    val strings = appStrings()
    val amount = parseAmountInput(amountInput)
    val selectedIds = members.map { it.memberId }.filter { it in selectedMemberIds }
    val perMember = if (amount > 0 && selectedIds.isNotEmpty()) splitAmountDeterministically(amount, selectedIds).values.firstOrNull() ?: 0 else 0
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = appPlainCardColors(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.serviceChargeItemTitle(serviceIndex), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.serviceChargeNameLabel, style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = appFieldColors(),
                shape = RoundedCornerShape(18.dp)
            )
            CalculatorAmountField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                colors = appFieldColors(),
                shape = RoundedCornerShape(18.dp),
                label = strings.serviceChargeAmountLabel,
            )
            Text(strings.serviceChargeMembersLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                members.filter { it.includedInSplit }.forEach { member ->
                    FilterChip(
                        selected = member.memberId in selectedMemberIds,
                        onClick = { onToggleMember(member.memberId) },
                        label = { Text("@${member.username}", style = MaterialTheme.typography.labelLarge) },
                        colors = appFilterChipColors()
                    )
                }
            }
            FieldHelperText(
                text = if (selectedIds.isNotEmpty() && amount > 0) {
                    strings.serviceChargePreview(selectedIds.size, formatAmount(perMember))
                } else {
                    strings.serviceChargeHint
                },
                isError = amountInput.isNotBlank() && selectedIds.isEmpty()
            )
        }
    }
}

@Composable
private fun ExpenseLiveSummaryCard(uiState: ExpenseEditorUiState) {
    val strings = appStrings()
    val receiptItems = remember(uiState.taxAmountPreview, uiState.serviceCharges) {
        buildReceiptSummaryItems(uiState = uiState, taxLabel = strings.taxAmountLabel) { index ->
            strings.serviceChargeItemTitle(index)
        }
    }
    var receiptItemsExpanded by rememberSaveable(
        uiState.taxAmountPreview,
        uiState.serviceChargeTotalPreview,
        uiState.serviceCharges.size
    ) { mutableStateOf(false) }
    val tone = when {
        uiState.isPayerOverflow || uiState.isShareOverflow || uiState.hasInvalidServiceCharges || uiState.hasInvalidTaxPercent -> MaterialTheme.colorScheme.error
        uiState.isAmountsReady -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = tone.copy(alpha = 0.08f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(strings.expenseSummaryTitle, style = MaterialTheme.typography.titleMedium, color = tone)
            DetailLine(strings.totalAmountStat, formatAmount(parseAmountInput(uiState.totalAmountInput)))
            DetailLine(strings.enteredPayersTitle, formatAmount(uiState.payerTotal))
            DetailLine(strings.remainingPayersTitle, formatAmount(uiState.remainingPayerAmount.coerceAtLeast(0)))
            DetailLine(strings.enteredSharesTitle, formatAmount(uiState.shareTotal))
            DetailLine(strings.remainingSharesTitle, formatAmount(uiState.remainingShareAmount.coerceAtLeast(0)))
            DetailLine(strings.finalSharesTitle, formatAmount(uiState.finalShareTotal))
            AppAnimatedVisibility(visible = receiptItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { receiptItemsExpanded = !receiptItemsExpanded },
                        colors = appOutlinedButtonColors(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (receiptItemsExpanded) strings.hideDetailsLabel else strings.showDetailsLabel,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f),
                            color = tone
                        )
                        Icon(
                            imageVector = if (receiptItemsExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = tone
                        )
                    }
                    AppAnimatedVisibility(visible = receiptItemsExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (uiState.taxAmountPreview > 0) {
                                DetailLine(strings.taxAmountLabel, formatAmount(uiState.taxAmountPreview))
                            }
                            if (uiState.serviceChargeTotalPreview > 0) {
                                DetailLine(strings.serviceChargesTotalLabel, formatAmount(uiState.serviceChargeTotalPreview))
                            }
                            receiptItems.forEach { (label, amount) ->
                                DetailLine(label, formatAmount(amount))
                            }
                        }
                    }
                }
            }
            AppAnimatedVisibility(
                visible = uiState.isPayerOverflow || uiState.isShareOverflow || uiState.hasInvalidServiceCharges || uiState.hasInvalidTaxPercent
            ) {
                Text(
                    text = when {
                        uiState.hasInvalidTaxPercent -> strings.message(MessageKey.EXPENSE_TAX_PERCENT_INVALID).orEmpty()
                        uiState.hasInvalidServiceCharges -> strings.message(MessageKey.EXPENSE_SERVICE_CHARGE_INVALID).orEmpty()
                        uiState.isPayerOverflow -> strings.payerOverflowMessage(formatAmount(-uiState.remainingPayerAmount))
                        else -> strings.shareOverflowMessage(formatAmount(-uiState.remainingShareAmount))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun FieldHelperText(text: String, isError: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun buildEqualPreview(uiState: ExpenseEditorUiState, memberId: String): String {
    if (uiState.splitType != SplitType.EQUAL) return "-"
    val member = uiState.members.firstOrNull { it.memberId == memberId } ?: return "-"
    return formatAmount(member.baseSharePreview)
}

private fun buildReceiptSummaryItems(
    uiState: ExpenseEditorUiState,
    taxLabel: String,
    fallbackServiceChargeTitle: (Int) -> String,
): List<Pair<String, Int>> {
    val items = mutableListOf<Pair<String, Int>>()
    if (uiState.taxAmountPreview > 0) {
        items += taxLabel to uiState.taxAmountPreview
    }
    uiState.serviceCharges.forEachIndexed { index, charge ->
        val amount = parseAmountInput(charge.amountInput)
        if (amount > 0) {
            val title = charge.title.ifBlank { fallbackServiceChargeTitle(index + 1) }
            items += title to amount
        }
    }
    return items
}

private fun buildMemberReceiptItems(
    uiState: ExpenseEditorUiState,
    member: MemberDraftUi,
    taxLabel: String,
    fallbackServiceChargeTitle: (Int) -> String,
): List<Pair<String, Int>> {
    val items = mutableListOf<Pair<String, Int>>()
    if (member.taxSharePreview > 0) {
        items += taxLabel to member.taxSharePreview
    }
    val includedMemberIds = uiState.members
        .filter { it.includedInSplit }
        .map { it.memberId }
    uiState.serviceCharges.forEachIndexed { index, charge ->
        val amount = parseAmountInput(charge.amountInput)
        if (amount <= 0) return@forEachIndexed
        val selectedIds = includedMemberIds.filter { it in charge.selectedMemberIds }
        if (member.memberId !in selectedIds || selectedIds.isEmpty()) return@forEachIndexed
        val memberAmount = splitAmountDeterministically(amount, selectedIds)[member.memberId] ?: 0
        if (memberAmount <= 0) return@forEachIndexed
        val title = charge.title.ifBlank { fallbackServiceChargeTitle(index + 1) }
        items += title to memberAmount
    }
    return items
}
