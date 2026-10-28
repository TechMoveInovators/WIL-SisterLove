

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*

@Composable
fun UserManagementScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val canManage = state.can(Permission.USERS_MANAGE)
    val users = state.users.filter { query.isBlank() || it.name.contains(query, true) }.sortedBy { it.name }
    ScreenFrame(title = "User Management", onBack = { nav.back() }) {
        if (!canManage) NoteBox("You can view people but not change roles.")
        Field(query, { query = it }, "Search by name")
        users.forEach { u ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar(u.name, 40)
                    Column(Modifier.weight(1f)) {
                        Text(u.name, fontWeight = FontWeight.Bold)
                        Muted("${u.role.label} · ${u.province.ifBlank { "No province" }} · joined ${u.joined.pretty()}")
                    }
                    if (!u.active) BadChip("Suspended")
                }
                if (canManage && u.id != state.currentUserId) {
                    RoleMenu(u.role) { vm.setRole(u.id, it) }
                    SwitchRow(if (u.active) "Active" else "Suspended", u.active, "Suspended people can't sign in.") { vm.setActive(u.id, it) }
                }
            }
        }
    }
}

@Composable
private fun RoleMenu(current: Role, onPick: (Role) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) { Text("Role: ${current.label}") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Role.values().forEach { r ->
                DropdownMenuItem(
                    text = { Column { Text(r.label, fontWeight = FontWeight.SemiBold); Muted(r.description) } },
                    onClick = { open = false; if (r != current) onPick(r) },
                )
            }
        }
    }
}
