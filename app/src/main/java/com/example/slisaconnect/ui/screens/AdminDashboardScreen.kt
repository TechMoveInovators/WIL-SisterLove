package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*
import za.org.slisa.connect.ui.theme.NightMuted

@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    onUsers: () -> Unit,
    onTeams: () -> Unit,
    onEvents: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser

    ScreenFrame(title = "Admin dashboard", route = "/admin", onBack = onBack) {
        NightPanel {
            Text(me.role.label, style = MaterialTheme.typography.bodySmall, color = NightMuted)
            Text("Welcome back, ${me.name.substringBefore(" ")}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Overview tiles: only the numbers this role is allowed to see.
        val tiles = buildList {
            if (state.can(Permission.USERS_VIEW)) add("${state.users.size} of 352" to "Registered users")
            if (state.can(Permission.TEAMS_REVIEW)) {
                add("${state.teams.size}" to "Teams registered")
                add("${state.pendingReviews}" to "Pending reviews")
            }
            if (state.can(Permission.EVENTS_MANAGE)) add("${state.events.count { it.published }}" to "Published events")
        }
        if (tiles.isNotEmpty()) {
            SectionTitle("Overview")
            tiles.chunked(2).forEach { rowTiles ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowTiles.forEach { (value, label) -> StatTile(value, label, Modifier.weight(1f)) }
                    if (rowTiles.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        SectionTitle("Management menu")
        MenuRow("User management", "View accounts and assign roles", Icons.Default.Person, state.can(Permission.USERS_VIEW), onUsers)
        MenuRow("Team approval", "Review and approve team registrations", Icons.Default.Star, state.can(Permission.TEAMS_REVIEW), onTeams, state.pendingReviews)
        MenuRow("Event management", "Create events, deadlines and fixtures", Icons.Default.DateRange, state.can(Permission.EVENTS_MANAGE), onEvents)

        // Support resources are invisible to every role without SUPPORT_VIEW.
        if (state.can(Permission.SUPPORT_VIEW)) {
            SectionTitle("Support resources", "Private · staff only")
            NoteBox("Contacts are hidden from every other role. A resource is shown in the app only after it is verified, then published by a platform admin.")
            state.resources.forEach { r -> ResourceCard(r, state, vm) }
        }

        if (state.can(Permission.AUDIT_VIEW)) {
            Panel {
                SectionTitle("Recent activity")
                state.audit.take(5).forEach { a ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(a.time, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${a.actor} · ${a.message}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, allowed: Boolean, onClick: () -> Unit, badge: Int = 0) {
    if (allowed) ListRow(title, subtitle, icon, onClick = onClick, badge = badge)
    else ListRow(title, "Not available for your role", locked = true)
}

@Composable
private fun ResourceCard(r: SupportResource, state: AppState, vm: AppViewModel) {
    Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(r.name, fontWeight = FontWeight.Bold)
                Text("${r.kind} · from ${r.source}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when (r.status) {
                ResourceStatus.SUBMITTED -> PlainChip("Submitted")
                ResourceStatus.VERIFIED -> WarnChip("Verified")
                ResourceStatus.PUBLISHED -> GoodChip("Published")
            }
        }
        Text("Contact on file: ${r.maskedContact}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (r.status == ResourceStatus.SUBMITTED && state.can(Permission.REFERRALS_VERIFY)) {
            LimeButton("Mark verified") { vm.verifyResource(r.id) }
        }
        if (r.status == ResourceStatus.VERIFIED) {
            if (state.can(Permission.SUPPORT_PUBLISH)) Button(onClick = { vm.publishResource(r.id) }) { Text("Publish") }
            else Text("Waiting for an admin to publish.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

