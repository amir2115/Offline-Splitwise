@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.encer.offlinesplitwise.features.expense_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.domain.model.Member
import com.encer.offlinesplitwise.domain.model.SplitType
import com.encer.offlinesplitwise.ui.components.DetailLine
import com.encer.offlinesplitwise.ui.components.HeroCard
import com.encer.offlinesplitwise.ui.components.SectionHeader
import com.encer.offlinesplitwise.ui.components.appTopBarColors
import com.encer.offlinesplitwise.ui.formatting.formatAmount
import com.encer.offlinesplitwise.ui.formatting.formatAmountCompact
import com.encer.offlinesplitwise.ui.formatting.formatDate
import com.encer.offlinesplitwise.ui.localization.appStrings

@Composable
fun ExpenseDetailScreen(
    groupId: String,
    expenseId: String,
    onBack: () -> Unit,
    onEdit: (String, String) -> Unit,
) {
    val strings = appStrings()
    val viewModel: ExpenseDetailsViewModel = com.encer.offlinesplitwise.ui.components.appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val expense = uiState.expense

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(expense?.title ?: strings.expenseDetailsFallback, style = MaterialTheme.typography.titleLarge) },
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
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (expense == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.expenseNotFound, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
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
                    subtitle = expense.note?.ifBlank { strings.noDescription } ?: strings.noDescription,
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
                    DetailLine(memberName(uiState.members, payer.memberId), formatAmount(payer.amount))
                }
                SectionHeader(strings.sharesTitle)
                expense.shares.forEach { share ->
                    DetailLine(memberName(uiState.members, share.memberId), formatAmount(share.amount))
                }
            }
        }
    }
}

@Composable
private fun SummaryGrid(items: List<Pair<String, String>>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { (label, value) ->
                    androidx.compose.material3.Card(
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                if (rowItems.size == 1) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun memberName(members: List<Member>, memberId: String): String {
    return members.firstOrNull { it.id == memberId }?.username ?: if (java.util.Locale.getDefault().language == "fa") "کاربر $memberId" else "Member $memberId"
}
