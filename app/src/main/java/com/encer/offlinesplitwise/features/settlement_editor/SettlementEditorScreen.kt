package com.encer.offlinesplitwise.features.settlement_editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.core.common.MessageKey
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.domain.model.memberName
import com.encer.offlinesplitwise.ui.components.AmountText
import com.encer.offlinesplitwise.ui.components.AppAnimatedSection
import com.encer.offlinesplitwise.ui.components.AppAnimatedVisibility
import com.encer.offlinesplitwise.ui.components.AppInlineMessageCard
import com.encer.offlinesplitwise.ui.components.CalculatorAmountField
import com.encer.offlinesplitwise.ui.components.DashboardHeroCard
import com.encer.offlinesplitwise.ui.components.appAssistChipColors
import com.encer.offlinesplitwise.ui.components.appCardColors
import com.encer.offlinesplitwise.ui.components.appFieldColors
import com.encer.offlinesplitwise.ui.components.appHeroSectionEnter
import com.encer.offlinesplitwise.ui.components.appHiltViewModel
import com.encer.offlinesplitwise.ui.components.appPlainCardColors
import com.encer.offlinesplitwise.ui.components.appPrimaryButtonColors
import com.encer.offlinesplitwise.ui.components.appSectionEnter
import com.encer.offlinesplitwise.ui.components.appTopBarColors
import com.encer.offlinesplitwise.ui.formatting.formatAmount
import com.encer.offlinesplitwise.ui.localization.appStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementEditorScreen(
    groupId: String,
    settlementId: String?,
    initialFromMemberId: String? = null,
    initialToMemberId: String? = null,
    initialAmountInput: String? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val strings = appStrings()
    val viewModel: SettlementEditorViewModel = appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val inlineErrorMessage = strings.message(uiState.message?.takeIf { it != MessageKey.SETTLEMENT_SAVED })
    var contentVisible by remember(settlementId, groupId) { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.takeIf { it == MessageKey.SETTLEMENT_SAVED }?.let {
            snackbarHostState.showSnackbar(strings.message(it).orEmpty())
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(groupId, settlementId) {
        contentVisible = true
    }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaved()
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(strings.settlementFormTitle(settlementId != null), style = MaterialTheme.typography.titleLarge) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter()) {
                DashboardHeroCard(
                    title = strings.settlementHeroTitle,
                    subtitle = strings.settlementHeroSubtitle,
                    icon = { Icon(Icons.Rounded.Payments, contentDescription = null) }
                )
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
            AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 60)) {
                ElevatedCard(shape = RoundedCornerShape(28.dp), colors = appCardColors()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(strings.settlementParticipantsTitle, style = MaterialTheme.typography.titleLarge)
                        AppAnimatedVisibility(
                            visible = uiState.fromMemberId != null && uiState.toMemberId != null
                        ) {
                            SettlementSelectionSummaryCard(
                                payerName = memberName(uiState.members, uiState.fromMemberId.orEmpty()),
                                receiverName = memberName(uiState.members, uiState.toMemberId.orEmpty()),
                            )
                        }
                        SettlementMemberPicker(
                            label = strings.payerLabel,
                            badge = strings.selectedPayerLabel,
                            members = uiState.members,
                            selectedId = uiState.fromMemberId,
                            onSelect = viewModel::setFromMember
                        )
                        SettlementMemberPicker(
                            label = strings.receiverLabel,
                            badge = strings.selectedReceiverLabel,
                            members = uiState.members,
                            selectedId = uiState.toMemberId,
                            onSelect = viewModel::setToMember
                        )
                    }
                }
            }
            AppAnimatedVisibility(visible = uiState.suggestedAmount != null) {
                SettlementSuggestedAmountCard(
                    label = strings.suggestedSettlementLabel,
                    recommendation = strings.settlementRecommendation(
                        from = memberName(uiState.members, uiState.fromMemberId.orEmpty()),
                        to = memberName(uiState.members, uiState.toMemberId.orEmpty()),
                        amount = formatAmount(uiState.suggestedAmount ?: 0)
                    ),
                    actionLabel = strings.fillSuggestedAmountLabel,
                    amount = uiState.suggestedAmount ?: 0,
                    onFillSuggestedAmount = viewModel::setSuggestedAmount
                )
            }
            AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 100)) {
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
                        CalculatorAmountField(
                            value = uiState.amountInput,
                            onValueChange = viewModel::setAmount,
                            modifier = Modifier.fillMaxWidth(),
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp),
                            label = strings.settlementAmountLabel,
                        )
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = viewModel::setNote,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(strings.noteLabel, style = MaterialTheme.typography.bodyMedium) },
                            colors = appFieldColors(),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
            AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter(delayMillis = 140)) {
                Button(
                    onClick = { viewModel.save() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = appPrimaryButtonColors(),
                    enabled = uiState.canCreateTransaction
                ) {
                    Text(strings.settlementFormAction(settlementId != null), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SettlementSelectionSummaryCard(
    payerName: String,
    receiverName: String,
) {
    val strings = appStrings()
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(payerName, style = MaterialTheme.typography.titleMedium)
                Text(strings.payerLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Rounded.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(receiverName, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.End)
                Text(strings.receiverLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettlementSuggestedAmountCard(
    label: String,
    recommendation: String,
    actionLabel: String,
    amount: Int,
    onFillSuggestedAmount: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(recommendation, style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AmountText(
                    amount = amount,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                AssistChip(
                    onClick = onFillSuggestedAmount,
                    colors = appAssistChipColors(),
                    label = { Text(actionLabel, style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun SettlementMemberPicker(
    label: String,
    badge: String,
    members: List<Member>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        members.chunked(2).forEach { rowMembers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowMembers.forEach { member ->
                    val isSelected = selectedId == member.id
                    Box(modifier = Modifier.weight(1f)) {
                        SettlementMemberOptionCard(
                            modifier = Modifier.fillMaxWidth(),
                            name = "@${member.username}",
                            badge = badge.takeIf { isSelected },
                            selected = isSelected,
                            enabled = true,
                            onClick = { onSelect(member.id) }
                        )
                    }
                }
                if (rowMembers.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SettlementMemberOptionCard(
    modifier: Modifier = Modifier,
    name: String,
    badge: String?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    }
    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    Card(
        modifier = modifier
            .heightIn(min = 88.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            AppAnimatedVisibility(visible = badge != null) {
                Text(
                    text = badge.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
