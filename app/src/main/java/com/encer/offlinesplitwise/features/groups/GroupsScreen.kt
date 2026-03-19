@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.encer.offlinesplitwise.features.groups

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.encer.offlinesplitwise.ui.components.EmptyStateCard
import com.encer.offlinesplitwise.ui.components.HeroCard
import com.encer.offlinesplitwise.ui.components.NameDialog
import com.encer.offlinesplitwise.ui.components.appCardColors
import com.encer.offlinesplitwise.ui.components.appTopBarColors
import com.encer.offlinesplitwise.ui.formatting.formatDate
import com.encer.offlinesplitwise.ui.localization.appStrings
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun GroupsScreen(onOpenGroup: (String) -> Unit) {
    val strings = appStrings()
    val viewModel: GroupsViewModel = com.encer.offlinesplitwise.ui.components.appHiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingGroupId by rememberSaveable { mutableStateOf<String?>(null) }

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
                .padding(horizontal = 16.dp, vertical = 18.dp),
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
}
