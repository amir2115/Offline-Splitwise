@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.encer.offlinesplitwise.features.groups

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.ui.components.EmptyStateCard
import com.encer.offlinesplitwise.ui.components.HeroCard
import com.encer.offlinesplitwise.ui.components.NameDialog
import com.encer.offlinesplitwise.ui.components.appCardColors
import com.encer.offlinesplitwise.ui.components.appOutlinedButtonColors
import com.encer.offlinesplitwise.ui.components.appTopBarColors
import com.encer.offlinesplitwise.ui.formatting.formatDate
import com.encer.offlinesplitwise.ui.localization.appStrings
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun GroupsScreen(onOpenGroup: (String) -> Unit) {
    val strings = appStrings()
    val viewModel: GroupsViewModel = com.encer.offlinesplitwise.ui.components.appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingGroupId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingGroupActionId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(strings.appTitle, style = MaterialTheme.typography.titleLarge) },
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
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroCard(
                    title = strings.homeHeroTitle,
                    subtitle = strings.homeHeroSubtitle,
                    icon = { Icon(Icons.Rounded.Groups, contentDescription = null) }
                )
            }
            item {
                Text(
                    strings.invitesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (uiState.isLoading && uiState.invites.isEmpty()) {
                items(2) {
                    InviteSkeletonCard(
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
            if (uiState.invites.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateCard(strings.noInvitesTitle, strings.noInvitesSubtitle)
                }
            }
            items(uiState.invites, key = { it.id }) { invite ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(28.dp),
                    colors = appCardColors(),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f), CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                                        shape = CircleShape
                                    )
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.MailOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    invite.groupName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "@${invite.inviterUsername}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.rejectInvite(invite.id) },
                                modifier = Modifier.weight(1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(strings.rejectInvite, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                            }
                            OutlinedButton(
                                onClick = { viewModel.acceptInvite(invite.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(strings.acceptInvite, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
            if (uiState.isLoading && uiState.groups.isEmpty()) {
                items(3) {
                    GroupSkeletonCard(
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
            items(uiState.groups, key = { it.id }) { group ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
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
                            IconButton(onClick = { pendingGroupActionId = group.id }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (uiState.groups.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateCard(strings.noGroupsTitle, strings.noGroupsSubtitle)
                }
            }
            item { Spacer(Modifier.padding(12.dp)) }
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

    pendingGroupActionId?.let { groupId ->
        val group = uiState.groups.firstOrNull { it.id == groupId }
        if (group != null) {
            val canDeleteForEveryone = group.userId == null || group.userId == uiState.currentUserId
            GroupActionDialog(
                groupName = group.name,
                canLeave = true,
                canDeleteForEveryone = canDeleteForEveryone,
                onDismiss = { pendingGroupActionId = null },
                onDeleteForEveryone = {
                    viewModel.deleteGroup(group.id)
                    pendingGroupActionId = null
                },
                onLeaveGroup = {
                    viewModel.leaveGroup(group.id)
                    pendingGroupActionId = null
                }
            )
        }
    }
}

@Composable
private fun GroupActionDialog(
    groupName: String,
    canLeave: Boolean,
    canDeleteForEveryone: Boolean,
    onDismiss: () -> Unit,
    onDeleteForEveryone: () -> Unit,
    onLeaveGroup: () -> Unit,
) {
    val strings = appStrings()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    strings.groupActionDialogTitle(groupName),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    strings.groupActionDialogSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (canLeave) {
                    OutlinedButton(
                        onClick = onLeaveGroup,
                        modifier = Modifier.fillMaxWidth(),
                        colors = appOutlinedButtonColors()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(strings.leaveGroup, style = MaterialTheme.typography.titleMedium)
                            Text(
                                strings.groupLeaveMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (canDeleteForEveryone) {
                    OutlinedButton(
                        onClick = onDeleteForEveryone,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(strings.deleteGroupForEveryone, style = MaterialTheme.typography.titleMedium)
                            Text(
                                strings.groupDeleteConfirmMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(strings.cancel, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun InviteSkeletonCard(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = appCardColors(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                ShimmerBlock(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    shape = CircleShape
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBlock(modifier = Modifier.fillMaxWidth(0.42f).height(20.dp))
                    ShimmerBlock(modifier = Modifier.fillMaxWidth(0.28f).height(14.dp))
                }
            }
            ShimmerBlock(modifier = Modifier.fillMaxWidth().height(1.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShimmerBlock(modifier = Modifier.weight(1f).height(42.dp))
                ShimmerBlock(modifier = Modifier.weight(1f).height(42.dp))
            }
        }
    }
}

@Composable
private fun GroupSkeletonCard(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = appCardColors(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBlock(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                shape = CircleShape
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBlock(modifier = Modifier.fillMaxWidth(0.46f).height(20.dp))
                ShimmerBlock(modifier = Modifier.fillMaxWidth(0.22f).height(14.dp))
            }
            ShimmerBlock(modifier = Modifier.width(22.dp).height(22.dp))
            ShimmerBlock(modifier = Modifier.width(22.dp).height(22.dp))
        }
    }
}

@Composable
private fun ShimmerBlock(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "groups_shimmer")
    val shift = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "groups_shimmer_shift"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset(600f * shift.value, 220f * shift.value)
                )
            )
    )
}
