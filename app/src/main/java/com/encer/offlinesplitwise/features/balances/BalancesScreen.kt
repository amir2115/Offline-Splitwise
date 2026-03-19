package com.encer.offlinesplitwise.features.balances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.domain.model.MemberBalance
import com.encer.offlinesplitwise.domain.model.SimplifiedTransfer
import com.encer.offlinesplitwise.ui.components.AmountText
import com.encer.offlinesplitwise.ui.components.AppAnimatedSection
import com.encer.offlinesplitwise.ui.components.AppAnimatedVisibility
import com.encer.offlinesplitwise.ui.components.DashboardHeroCard
import com.encer.offlinesplitwise.ui.components.EmptyStateCard
import com.encer.offlinesplitwise.ui.components.SectionHeader
import com.encer.offlinesplitwise.ui.components.appHeroSectionEnter
import com.encer.offlinesplitwise.ui.components.appHiltViewModel
import com.encer.offlinesplitwise.ui.components.appPlainCardColors
import com.encer.offlinesplitwise.ui.components.appSectionEnter
import com.encer.offlinesplitwise.ui.components.appSwitchColors
import com.encer.offlinesplitwise.ui.components.appTopBarColors
import com.encer.offlinesplitwise.ui.localization.appStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalancesScreen(
    groupId: String,
    onSuggestedPaymentClick: (SimplifiedTransfer) -> Unit,
    onBack: () -> Unit,
) {
    val strings = appStrings()
    val viewModel: BalancesViewModel = appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var simplify by rememberSaveable { mutableStateOf(true) }
    var contentVisible by remember(groupId) { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        contentVisible = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(strings.balancesOfGroup(uiState.group?.name.orEmpty()), style = MaterialTheme.typography.titleLarge) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter()) {
                    DashboardHeroCard(
                        title = strings.balancesOfGroup(uiState.group?.name.orEmpty()),
                        subtitle = strings.optimizePaymentsSubtitle,
                        icon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null) }
                    )
                }
            }
            item {
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 60)) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = appPlainCardColors(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.optimizePaymentsTitle, style = MaterialTheme.typography.titleLarge)
                                Text(strings.optimizePaymentsSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = simplify,
                                onCheckedChange = { simplify = it },
                                colors = appSwitchColors()
                            )
                        }
                    }
                }
            }
            item {
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 90)) {
                    SectionHeader(strings.memberBalanceTitle)
                }
            }
            items(uiState.balances, key = { it.memberId }) { balance ->
                AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 120)) {
                    BalanceCard(balance)
                }
            }
            if (simplify) {
                item {
                    AppAnimatedSection(visible = contentVisible, enter = appSectionEnter(delayMillis = 150)) {
                        SectionHeader(strings.suggestedPaymentsTitle)
                    }
                }
                items(uiState.simplifiedTransfers, key = { "${it.fromMemberId}-${it.toMemberId}" }) { transfer ->
                    AppAnimatedSection(visible = contentVisible, enter = appHeroSectionEnter(delayMillis = 180)) {
                        SimplifiedTransferCard(
                            transfer = transfer,
                            onClick = { onSuggestedPaymentClick(transfer) }
                        )
                    }
                }
                if (uiState.simplifiedTransfers.isEmpty()) {
                    item {
                        AppAnimatedVisibility(visible = true) {
                            EmptyStateCard(strings.allSettledTitle, strings.allSettledSubtitle)
                        }
                    }
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
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = appPlainCardColors(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(balance.memberName, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                Text(
                    text = strings.balanceState(balance.netBalance),
                    style = MaterialTheme.typography.labelLarge,
                    color = tone,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BalanceStatCard(
                        modifier = Modifier.weight(1f),
                        label = strings.paidStat,
                        amount = balance.paidTotal
                    )
                    BalanceStatCard(
                        modifier = Modifier.weight(1f),
                        label = strings.owedStat,
                        amount = balance.owedTotal
                    )
                }
                BalanceStatCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = strings.netStat,
                    amount = kotlin.math.abs(balance.netBalance),
                    accent = tone
                )
            }
        }
    }
}

@Composable
private fun SimplifiedTransferCard(
    transfer: SimplifiedTransfer,
    onClick: () -> Unit,
) {
    val strings = appStrings()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Icon(Icons.Rounded.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(strings.paymentSuggestion(transfer.fromMemberName, transfer.toMemberName), style = MaterialTheme.typography.titleMedium)
                AmountText(
                    amount = transfer.amount,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BalanceStatCard(
    modifier: Modifier = Modifier,
    label: String,
    amount: Int,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier.heightIn(min = 86.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AmountText(
                amount = amount,
                style = MaterialTheme.typography.titleMedium,
                color = accent
            )
        }
    }
}