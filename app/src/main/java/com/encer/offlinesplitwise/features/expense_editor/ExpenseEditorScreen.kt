package com.encer.offlinesplitwise.features.expense_editor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.core.common.MessageKey
import com.encer.offlinesplitwise.domain.model.ExpenseShare
import com.encer.offlinesplitwise.domain.model.SplitType
import com.encer.offlinesplitwise.ui.components.AppAnimatedSection
import com.encer.offlinesplitwise.ui.components.AppAnimatedVisibility
import com.encer.offlinesplitwise.ui.components.AppInlineMessageCard
import com.encer.offlinesplitwise.ui.components.DashboardHeroCard
import com.encer.offlinesplitwise.ui.components.DetailLine
import com.encer.offlinesplitwise.ui.components.SectionHeader
import com.encer.offlinesplitwise.ui.components.amountVisualTransformation
import com.encer.offlinesplitwise.ui.components.appAssistChipColors
import com.encer.offlinesplitwise.ui.components.appCardColors
import com.encer.offlinesplitwise.ui.components.appFieldColors
import com.encer.offlinesplitwise.ui.components.appFilterChipColors
import com.encer.offlinesplitwise.ui.components.appHeroSectionEnter
import com.encer.offlinesplitwise.ui.components.appHiltViewModel
import com.encer.offlinesplitwise.ui.components.appOutlinedButtonColors
import com.encer.offlinesplitwise.ui.components.appPlainCardColors
import com.encer.offlinesplitwise.ui.components.appPrimaryButtonColors
import com.encer.offlinesplitwise.ui.components.appSectionEnter
import com.encer.offlinesplitwise.ui.components.appSwitchColors
import com.encer.offlinesplitwise.ui.components.appTopBarColors
import com.encer.offlinesplitwise.ui.formatting.formatAmount
import com.encer.offlinesplitwise.ui.formatting.parseAmountInput
import com.encer.offlinesplitwise.ui.formatting.parseAmountInputOrNull
import com.encer.offlinesplitwise.ui.localization.appStrings

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
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp)
                        )
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = viewModel::updateNote,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.expenseNoteLabel, style = MaterialTheme.typography.bodyMedium) },
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp)
                        )
                        OutlinedTextField(
                            value = uiState.totalAmountInput,
                            onValueChange = viewModel::updateTotal,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.totalAmountLabel, style = MaterialTheme.typography.bodyMedium) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = amountVisualTransformation,
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp)
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
            AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 100)) {
                SectionHeader(strings.membersAndPayersTitle)
            }
            uiState.members.forEach { member ->
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 130)) {
                    MemberEditorCard(
                        member = member,
                        splitType = uiState.splitType,
                        equalSharePreview = buildEqualPreview(uiState, member.memberId),
                        totalAmountInput = uiState.totalAmountInput,
                        onToggleIncluded = { included -> viewModel.toggleIncluded(member.memberId, included) },
                        onPayerChange = { value -> viewModel.updatePayer(member.memberId, value) },
                        onExactShareChange = { value -> viewModel.updateExactShare(member.memberId, value) },
                        onAssignFullAmount = { viewModel.assignFullAmountToPayer(member.memberId) },
                        onClearPayerAmount = { viewModel.clearPayerAmount(member.memberId) }
                    )
                }
            }
            AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter(delayMillis = 170)) {
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = appPrimaryButtonColors()
                ) {
                    Text(strings.expenseFormAction(expenseId != null), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberEditorCard(
    member: MemberDraftUi,
    splitType: SplitType,
    equalSharePreview: String,
    totalAmountInput: String,
    onToggleIncluded: (Boolean) -> Unit,
    onPayerChange: (String) -> Unit,
    onExactShareChange: (String) -> Unit,
    onAssignFullAmount: () -> Unit,
    onClearPayerAmount: () -> Unit,
) {
    val strings = appStrings()
    val hasPositiveTotal = (parseAmountInputOrNull(totalAmountInput) ?: 0) > 0
    val hasPayerAmount = member.payerAmountInput.isNotBlank()
    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("@${member.username}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = member.includedInSplit,
                    onCheckedChange = onToggleIncluded,
                    colors = appSwitchColors()
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssistChip(
                    onClick = onAssignFullAmount,
                    enabled = hasPositiveTotal,
                    colors = appAssistChipColors(),
                    label = { Text(strings.payFullAmountLabel, style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null) }
                )
                AppAnimatedVisibility(visible = hasPayerAmount) {
                    OutlinedButton(onClick = onClearPayerAmount, colors = appOutlinedButtonColors()) {
                        Text(strings.clearAmountLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            OutlinedTextField(
                value = member.payerAmountInput,
                onValueChange = onPayerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(strings.paidHowMuchLabel, style = MaterialTheme.typography.bodyMedium) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = amountVisualTransformation,
                colors = appFieldColors(),
                shape = RoundedCornerShape(18.dp)
            )
            AppAnimatedVisibility(visible = member.includedInSplit) {
                if (splitType == SplitType.EXACT) {
                    OutlinedTextField(
                        value = member.exactShareInput,
                        onValueChange = onExactShareChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.shareAmountLabel, style = MaterialTheme.typography.bodyMedium) },
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

private fun buildEqualPreview(uiState: ExpenseEditorUiState, memberId: String): String {
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
