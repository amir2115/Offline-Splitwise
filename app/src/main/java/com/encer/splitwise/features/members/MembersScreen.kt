@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.encer.splitwise.features.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.splitwise.domain.model.MembershipStatus
import com.encer.splitwise.ui.components.EmptyStateCard
import com.encer.splitwise.ui.components.NameDialog
import com.encer.splitwise.ui.components.appCardColors
import com.encer.splitwise.ui.components.appTopBarColors
import com.encer.splitwise.ui.formatting.formatDate
import com.encer.splitwise.ui.localization.appStrings

@Composable
fun MembersScreen(groupId: String, onBack: () -> Unit) {
    val strings = appStrings()
    val viewModel: MembersViewModel = com.encer.splitwise.ui.components.appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingMemberId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(strings.membersOfGroup(uiState.group?.name.orEmpty()), style = MaterialTheme.typography.titleLarge) },
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
            if (uiState.invalidUsernameMembers.isNotEmpty()) {
                item {
                    ElevatedCard(shape = RoundedCornerShape(24.dp), colors = appCardColors()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(strings.invalidUsernameSyncTitle, style = MaterialTheme.typography.titleMedium)
                            Text(strings.invalidUsernameSyncSubtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            uiState.invalidUsernameMembers.forEach { issue ->
                                Text("@${issue.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
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
                            Text(
                                member.username.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("@${member.username}", style = MaterialTheme.typography.titleMedium)
                            Text(strings.memberSince(formatDate(member.createdAt)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (member.membershipStatus == MembershipStatus.PENDING_INVITE) {
                                Text(strings.pendingInviteLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                            }
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
                initialValue = member.username,
                placeholder = strings.memberPlaceholder,
                confirmLabel = strings.save,
                onDismiss = { editingMemberId = null },
                onConfirm = { name ->
                    viewModel.updateMember(member.copy(username = name))
                    editingMemberId = null
                }
            )
        }
    }
}
