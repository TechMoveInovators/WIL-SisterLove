package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.BuildConfig
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*

/** The admin side's home. Every row is shown, but rows outside the signed-in role are locked. */
@Composable
fun AdminDashboardScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser
    if (me == null) {
        LaunchedEffect(Unit) { nav.to(Routes.WELCOME) }
        return
    }
    val today = vm.today()

    ScreenFrame(
        title = "Admin Dashboard", subtitle = me.role.label,
        actions = { BellAction(state.unreadCount) { nav.tab(Routes.ADMIN_ALERTS) } },
    ) {
        val tiles = buildList<@Composable (Modifier) -> Unit> {
            if (state.can(Permission.USERS_VIEW)) add { m -> StatTile("${state.users.size}", "People", m) }
            if (state.can(Permission.TEAMS_REVIEW)) {
                add { m -> StatTile("${state.pendingReviews}", "Teams to review", m) }
                add { m -> StatTile("${state.teams.size}", "Teams registered", m) }
            }
            if (state.can(Permission.EVENTS_MANAGE)) {
                add { m -> StatTile("${state.events.count { it.published && it.isUpcoming(today) }}", "Live events", m) }
            }
            if (state.can(Permission.SUPPORT_VIEW)) {
                add { m -> StatTile("${state.resources.count { it.status != ResourceStatus.PUBLISHED }}", "Contacts to check", m) }
            }
            if (state.can(Permission.LEARNING_MANAGE)) {
                add { m -> StatTile("${state.articles.count { it.published }}", "Articles live", m) }
            }
        }
        TileGrid(tiles)

        SectionTitle("Manage")
        ListRow("User Management", "Roles and access", Icons.Default.Person, locked = !state.canOpen(AdminArea.USERS), onClick = { nav.to(Routes.ADMIN_USERS) })
        ListRow("Sports & Team Registration", "Review, approve, entries", Icons.Default.Star, locked = !state.canOpen(AdminArea.TEAMS),
            badge = if (state.canOpen(AdminArea.TEAMS)) state.pendingReviews else 0, onClick = { nav.to(Routes.ADMIN_TEAMS) })
        ListRow("Events & Competitions", "Publish to the user dashboard", Icons.Default.DateRange, locked = !state.canOpen(AdminArea.EVENTS), onClick = { nav.to(Routes.ADMIN_EVENTS) })
        ListRow("Women's Hub & Support", "Verify and publish contacts", Icons.Default.Favorite, locked = !state.canOpen(AdminArea.SUPPORT), onClick = { nav.to(Routes.ADMIN_SUPPORT) })
        ListRow("Learning Content", "Articles for the Learn tab", Icons.Default.Info, locked = !state.canOpen(AdminArea.LEARNING), onClick = { nav.to(Routes.ADMIN_LEARNING) })
        ListRow("Reports & Impact", "Numbers across the platform", Icons.Default.List, onClick = { nav.to(Routes.ADMIN_REPORTS) })

        OutlinedButton(onClick = { nav.tab(Routes.HOME) }, Modifier.fillMaxWidth()) { Text("View as user") }

        if (BuildConfig.DEBUG && state.can(Permission.USERS_MANAGE)) {
            Panel {
                Text("Test build only", fontWeight = FontWeight.Bold)
                Muted("Adds clearly marked sample events, articles and helplines so you can see the user side filled in. Not in the release app.")
                OutlinedButton(onClick = { vm.addSampleContent() }) { Text("Load sample content") }
            }
        }
    }
}

@Composable
fun AdminAccountScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser ?: return
    var confirmOut by remember { mutableStateOf(false) }
    ScreenFrame(title = "Account") {
        Panel {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Avatar(me.name, 56)
                Column {
                    Text(me.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Muted(state.profiles[me.id]?.email ?: "")
                    PlainChip(me.role.label)
                }
            }
            Muted(me.role.description)
        }
        SectionTitle("What your role can do")
        Panel { Permission.values().forEach { p -> FactRow(p.label, if (state.can(p)) "Yes" else "No") } }
        ListRow("Edit profile", null, Icons.Default.Edit, onClick = { nav.to(Routes.EDIT_PROFILE) })
        ListRow("Privacy & security", null, Icons.Default.Lock, onClick = { nav.to(Routes.SECURITY) })
        OutlinedButton(onClick = { nav.tab(Routes.HOME) }, Modifier.fillMaxWidth()) { Text("View as user") }
        OutlinedButton(onClick = { confirmOut = true }, Modifier.fillMaxWidth()) { Text("Log out") }
    }
    if (confirmOut) SignOutDialog(onDismiss = { confirmOut = false }) { vm.signOut() }
}
