package com.example.slisaconnect.ui.screens

@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*

@Composable
fun UserManagementScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var roleFilter by rememberSaveable { mutableStateOf<Role?>(null) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = state.users.firstOrNull { it.id == selectedId }

    val users = state.users.filter { u ->
        (roleFilter == null || u.role == roleFilter) &&
                (query.isBlank() || u.name.contains(query, ignoreCase = true) || u.role.label.contains(query, ignoreCase = true))
    }

    ScreenFrame(title = "User management", route = "/admin/users", onBack = onBack) {
        NoteBox("Only a name, role and province are shown. Email and phone stay private to each user.")
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            placeholder = { Text("Search name or role") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = roleFilter == null, onClick = { roleFilter = null }, label = { Text("All") })
            Role.values().forEach { r -> FilterChip(selected = roleFilter == r, onClick = { roleFilter = r }, label = { Text(r.label) }) }
        }
        if (users.isEmpty()) Panel { Text("No matching users. Try a different name or role.") }
        users.forEach { u ->
            ListRow(
                title = u.name + if (u.id == state.currentUserId) " (you)" else "",
                subtitle = "${u.role.label} · ${u.province} · ${u.lastActive}",
                onClick = { selectedId = u.id },
                leading = { Avatar(u.name, 36) },
                trailing = { if (!u.active) PillChip("Suspended", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer) },
            )
        }
        Text("Demo sample of 352 registered users", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedId = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) { UserSheet(selected, state, vm, onDone = { selectedId = null }) }
    }
}

@Composable
private fun UserSheet(user: AppUser, state: AppState, vm: AppViewModel, onDone: () -> Unit) {
    val self = user.id == state.currentUserId
    val canManage = state.can(Permission.USERS_MANAGE) && !self
    var chosen by remember(user.id) { mutableStateOf(user.role) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${user.province} · last active ${user.lastActive}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!state.can(Permission.USERS_MANAGE)) NoteBox("View only. Only platform admins can change roles.")
        if (self) NoteBox("You cannot change your own role or suspend yourself.", NoteKind.WARN)

        Role.values().forEach { r ->
            Panel(modifier = Modifier.selectable(selected = chosen == r, enabled = canManage, role = SemanticsRole.RadioButton) { chosen = r }) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioButton(selected = chosen == r, onClick = null, enabled = canManage)
                    Column {
                        Text(r.label, fontWeight = FontWeight.SemiBold)
                        Text(r.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (canManage) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { if (vm.setRole(user.id, chosen) is Outcome.Done) onDone() }) { Text("Save role") }
                OutlinedButton(onClick = { if (vm.setActive(user.id, !user.active) is Outcome.Done) onDone() }) {
                    Text(if (user.active) "Suspend account" else "Reactivate account")
                }
            }
        }
    }
}
