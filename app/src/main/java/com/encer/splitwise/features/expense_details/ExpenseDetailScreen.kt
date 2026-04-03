@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.encer.splitwise.features.expense_details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.splitwise.domain.model.Expense
import com.encer.splitwise.domain.model.ExpenseShare
import com.encer.splitwise.domain.model.Member
import com.encer.splitwise.domain.model.SplitType
import com.encer.splitwise.ui.components.HeroCard
import com.encer.splitwise.ui.components.SectionHeader
import com.encer.splitwise.ui.components.appAssistChipColors
import com.encer.splitwise.ui.components.appBlendOverBackground
import com.encer.splitwise.ui.components.appCardColors
import com.encer.splitwise.ui.components.appPlainCardColors
import com.encer.splitwise.ui.components.appTopBarColors
import com.encer.splitwise.ui.formatting.formatAmount
import com.encer.splitwise.ui.formatting.formatDate
import com.encer.splitwise.ui.localization.appStrings

@Composable
fun ExpenseDetailScreen(
    groupId: String,
    expenseId: String,
    onBack: () -> Unit,
    onEdit: (String, String) -> Unit,
) {
    val strings = appStrings()
    val viewModel: ExpenseDetailsViewModel = com.encer.splitwise.ui.components.appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val expense = uiState.expense

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = expense?.title ?: strings.expenseDetailsFallback,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
                            viewModel.deleteExpense(expenseId)
                            onBack()
                        }) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = strings.delete,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (expense == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.expenseNotFound,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            val participantRows = rememberParticipantRows(uiState.members, expense)

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
                    subtitle = expense.note?.ifBlank { strings.noDescription } ?: strings.noDescription,
                    icon = { Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null) }
                )
                ExpenseOverviewCard(expense = expense)
                if (participantRows.isNotEmpty()) {
                    ParticipantsOverviewCard(rows = participantRows)
                }
                BreakdownCard(
                    title = strings.payersTitle,
                    items = expense.payers,
                    members = uiState.members,
                    accentColor = MaterialTheme.colorScheme.primary
                )
                BreakdownCard(
                    title = strings.sharesTitle,
                    items = expense.shares,
                    members = uiState.members,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun ExpenseOverviewCard(expense: Expense) {
    val strings = appStrings()
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = appCardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = strings.expenseSummaryTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatAmount(expense.totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            SummaryGrid(
                items = listOf(
                    strings.splitMethodStat to strings.splitTypeLabel(expense.splitType == SplitType.EQUAL),
                    strings.dateStat to formatDate(expense.createdAt),
                )
            )
        }
    }
}

@Composable
private fun ParticipantsOverviewCard(rows: List<ParticipantRow>) {
    val strings = appStrings()
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = appCardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(strings.membersAndPayersTitle)
            rows.forEachIndexed { index, row ->
                ParticipantRowCard(row = row)
                if (index != rows.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
private fun ParticipantRowCard(row: ParticipantRow) {
    val strings = appStrings()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = appBlendOverBackground(MaterialTheme.colorScheme.primary, alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = row.initial,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = strings.balanceState(row.net),
                    style = MaterialTheme.typography.bodyMedium,
                    color = netTone(row.net)
                )
            }
            AssistChip(
                onClick = {},
                enabled = false,
                colors = appAssistChipColors(),
                label = {
                    Text(
                        text = formatAmount(row.netAbsolute),
                        style = MaterialTheme.typography.labelLarge,
                        color = netTone(row.net)
                    )
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ParticipantStatCard(
                label = strings.paidStat,
                value = formatAmount(row.paid),
                modifier = Modifier.weight(1f)
            )
            ParticipantStatCard(
                label = strings.owedStat,
                value = formatAmount(row.owed),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ParticipantStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = appBlendOverBackground(MaterialTheme.colorScheme.surface, alpha = 0.75f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor
            )
        }
    }
}

@Composable
private fun BreakdownCard(
    title: String,
    items: List<ExpenseShare>,
    members: List<Member>,
    accentColor: Color,
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = appPlainCardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            items.forEachIndexed { index, item ->
                BreakdownRow(
                    name = memberName(members, item.memberId),
                    amount = formatAmount(item.amount),
                    accentColor = accentColor
                )
                if (index != items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    name: String,
    amount: String,
    accentColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = CircleShape,
            color = accentColor
        ) {}
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor
        )
    }
}

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
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = appBlendOverBackground(MaterialTheme.colorScheme.surface, alpha = 0.78f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.titleLarge,
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

private data class ParticipantRow(
    val name: String,
    val paid: Int,
    val owed: Int,
) {
    val net: Int get() = paid - owed
    val netAbsolute: Int get() = kotlin.math.abs(net)
    val initial: String get() = name.trim().take(1).uppercase()
}

@Composable
private fun netTone(net: Int): Color = when {
    net > 0 -> MaterialTheme.colorScheme.primary
    net < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun rememberParticipantRows(
    members: List<Member>,
    expense: Expense,
): List<ParticipantRow> {
    val orderedIds = buildList {
        expense.shares.forEach { share ->
            if (!contains(share.memberId)) add(share.memberId)
        }
        expense.payers.forEach { payer ->
            if (!contains(payer.memberId)) add(payer.memberId)
        }
    }
    return orderedIds.map { memberId ->
        ParticipantRow(
            name = memberName(members, memberId),
            paid = expense.payers.firstOrNull { it.memberId == memberId }?.amount ?: 0,
            owed = expense.shares.firstOrNull { it.memberId == memberId }?.amount ?: 0
        )
    }
}

private fun memberName(members: List<Member>, memberId: String): String {
    return members.firstOrNull { it.id == memberId }?.username
        ?: if (java.util.Locale.getDefault().language == "fa") "کاربر $memberId" else "Member $memberId"
}
