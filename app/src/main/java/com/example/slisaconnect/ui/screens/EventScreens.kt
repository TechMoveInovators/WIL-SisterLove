package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*

/** A published event as users see it. Used on the dashboard, the Events tab and Volunteer. */
@Composable
fun EventCard(event: SportsEvent, state: AppState, nav: Navigator, vm: AppViewModel) {
    val ctx = LocalContext.current
    val today = vm.today()
    val going = state.hasResponded(event)
    Panel(onClick = { nav.to(Routes.event(event.id)) }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(event.title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
            PlainChip(event.type.label)
        }
        Muted("${event.date.pretty()} · ${event.venue}")
        if (event.isOpen(today)) Muted("Respond by ${event.deadline.pretty()}") else WarnChip("Responses closed")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { nav.to(Routes.event(event.id)) }) { Text("Details") }
            if (going) {
                OutlinedButton(onClick = { vm.respondToEvent(event.id, false) }) { Text("I'm in ✓") }
            } else {
                Button(onClick = { vm.respondToEvent(event.id, true) }, enabled = event.isOpen(today)) { Text("Respond") }
            }
            TextButton(onClick = { ctx.shareText("${event.title} · ${event.date.pretty()} · ${event.venue}. Shared from SLiSA Connect.") }) { Text("Share") }
        }
    }
}

@Composable
fun EventsScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var type by rememberSaveable { mutableStateOf<EventType?>(null) }
    val events = state.upcomingEvents(vm.today()).filter { type == null || it.type == type }
    ScreenFrame(title = "Events", actions = { BellAction(state.unreadCount) { nav.to(Routes.NOTIFICATIONS) } }) {
        FilterRow(listOf<EventType?>(null) + EventType.values().toList(), type, { it?.label ?: "All" }) { type = it }
        if (events.isEmpty()) EmptyState("Nothing here yet", "No published events match this filter. New ones appear as soon as SLiSA publishes them.")
        events.forEach { EventCard(it, state, nav, vm) }
        TextButton(onClick = { nav.to(Routes.MY_EVENTS) }) { Text("Events I'm going to") }
    }
}

@Composable
fun EventDetailScreen(id: String, nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val event = state.events.firstOrNull { it.id == id && (it.published || state.can(Permission.EVENTS_MANAGE)) }
    ScreenFrame(title = "Event", onBack = { nav.back() }) {
        if (event == null) {
            EmptyState("Event not found", "It may have been removed or is no longer published.")
            return@ScreenFrame
        }
        val today = vm.today()
        val going = state.hasResponded(event)
        Panel {
            PlainChip(event.type.label)
            Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FactRow("Date", event.date.pretty())
            FactRow("Venue", event.venue)
            FactRow("Respond by", event.deadline.pretty())
            if (event.sport != "Any" && event.sport.isNotBlank()) FactRow("Sport", event.sport)
            Text(event.description)
        }
        if (going) {
            NoteBox("You've said you're in.", NoteKind.GOOD)
            OutlinedButton(onClick = { vm.respondToEvent(event.id, false) }, Modifier.fillMaxWidth()) { Text("Remove my response") }
        } else {
            PillButton(if (event.isOpen(today)) "Respond" else "Responses closed", Modifier.fillMaxWidth(), enabled = event.isOpen(today)) {
                vm.respondToEvent(event.id, true)
            }
        }
        if (event.type.isCompetition) {
            val teams = state.myTeams.filter { it.status == TeamStatus.APPROVED && (event.sport == "Any" || event.sport.equals(it.sport, true)) }
            Panel {
                Text("Enter a team", fontWeight = FontWeight.Bold)
                when {
                    state.myTeams.isEmpty() -> {
                        Muted("Register your team once, then use it to enter competitions like this one.")
                        OutlinedButton(onClick = { nav.to(Routes.TEAM_REGISTER) }) { Text("Register a team") }
                    }
                    teams.isEmpty() -> Muted("None of your teams can enter yet. A team must be approved and match the sport.")
                    else -> teams.forEach { t ->
                        val entered = t.entries.any { it.competition == event.title }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(t.name)
                            if (entered) PlainChip("Requested")
                            else Button(onClick = { vm.requestEntry(t.id, event.id) }, enabled = t.managerId == state.currentUserId) { Text("Request entry") }
                        }
                    }
                }
                Muted("Requesting entry isn't the same as being accepted. A coordinator confirms each entry.")
            }
        }
        TextButton(onClick = { ctx.shareText("${event.title} · ${event.date.pretty()} · ${event.venue}. Shared from SLiSA Connect.") }) { Text("Share this event") }
    }
}

@Composable
fun MyEventsScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val mine = state.upcomingEvents(vm.today()).filter { state.hasResponded(it) }
    ScreenFrame(title = "My events", onBack = { nav.back() }) {
        if (mine.isEmpty()) EmptyState("No events yet", "Events you respond to will be listed here.")
        mine.forEach { EventCard(it, state, nav, vm) }
    }
}

@Composable
fun VolunteerScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(0) }
    val today = vm.today()
    val all = state.upcomingEvents(today).filter { it.type == EventType.VOLUNTEER || it.type == EventType.COMMUNITY }
    val shown = all.filter {
        (query.isBlank() || it.title.contains(query, true) || it.venue.contains(query, true)) &&
                when (filter) { 1 -> it.type == EventType.VOLUNTEER; 2 -> it.type == EventType.COMMUNITY; 3 -> state.hasResponded(it); else -> true }
    }
    ScreenFrame(title = "Volunteer", actions = { BellAction(state.unreadCount) { nav.to(Routes.NOTIFICATIONS) } }) {
        Muted("Tasks, events and hours")
        Field(query, { query = it }, "Search opportunities", imeAction = androidx.compose.ui.text.input.ImeAction.Search)
        FilterRow(listOf(0, 1, 2, 3), filter, { listOf("All", "Volunteer", "Community", "I'm in")[it] }) { filter = it }
        if (shown.isEmpty()) EmptyState("No opportunities right now", "Volunteer tasks and community events appear here when SLiSA publishes them.")
        shown.forEach { EventCard(it, state, nav, vm) }
    }
}
