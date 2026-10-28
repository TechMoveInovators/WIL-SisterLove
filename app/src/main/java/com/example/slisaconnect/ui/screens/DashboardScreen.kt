package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.R
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*
import za.org.slisa.connect.ui.theme.*

/** The Dashboard in the design: "Who are you?" tiles, plus what admins have published for users. */
@Composable
fun DashboardScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val me = state.currentUser ?: return
    val first = state.profiles[me.id]?.first?.ifBlank { null } ?: me.name.substringBefore(" ")
    val today = vm.today()
    val upcoming = state.upcomingEvents(today).take(3)
    val latest = state.visibleArticles().filter { it.published }.maxByOrNull { it.date }
    val unread = state.myNotices.filter { !it.read }

    ScreenFrame(
        title = "Welcome $first",
        actions = { BellAction(state.unreadCount) { nav.to(Routes.NOTIFICATIONS) } },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.slisa_logo), "SLiSA Connect logo", Modifier.height(56.dp))
            Column {
                Text("Who are you?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Muted("Choose how you'll use SLiSA Connect")
            }
        }

        val tiles = buildList<@Composable (Modifier) -> Unit> {
            add { m -> ColourTile("Youth", "Sports, events, learning", Icons.Default.Star, TileBlue, m) { nav.to(Routes.YOUTH) } }
            add { m -> ColourTile("Women", "Safety, health, support", Icons.Default.Favorite, TilePink, m) { nav.to(Routes.WOMEN) } }
            add { m -> ColourTile("Volunteer", "Tasks, events, hours", Icons.Default.Person, TileLime, m) { nav.tab(Routes.VOLUNTEER) } }
            // The Admin tile only exists for staff. Members get a plain Alerts tile in its place.
            if (me.role.isStaff) {
                add { m -> ColourTile("Admin", "Users, programmes, reports", Icons.Default.Settings, TileViolet, m) { nav.to(Routes.ADMIN) } }
            } else {
                add { m -> ColourTile("Alerts", if (state.unreadCount > 0) "${state.unreadCount} unread" else "Nothing new", Icons.Default.Notifications, TileViolet, m) { nav.to(Routes.NOTIFICATIONS) } }
            }
        }
        TileGrid(tiles)

        if (unread.isNotEmpty()) {
            val n = unread.first()
            Panel(onClick = { nav.to(Routes.NOTIFICATIONS) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PillChip("New", Magenta, androidx.compose.ui.graphics.Color.White)
                    Text(n.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
                Muted(n.body)
                if (unread.size > 1) Muted("+ ${unread.size - 1} more")
            }
        }

        SectionTitle("Coming up", if (upcoming.isEmpty()) null else "From SLiSA")
        if (upcoming.isEmpty()) {
            EmptyState("No events yet", "When SLiSA publishes an event, tournament or workshop it will show up here.")
        } else {
            upcoming.forEach { EventCard(it, state, nav, vm) }
            TextButton(onClick = { nav.tab(Routes.EVENTS) }) { Text("See all events") }
        }

        if (latest != null) {
            SectionTitle("Latest from Learn")
            ListRow(latest.title, "${latest.topic.label} · ${latest.date.pretty()}", onClick = { nav.to(Routes.article(latest.id)) })
        }
    }
}
