@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*

/** Where admins publish events. Published events show on every user's dashboard and notify people who want event updates. */
@Composable
fun EventManagementScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<SportsEvent?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<SportsEvent?>(null) }
    val events = state.events.sortedByDescending { it.date }

    ScreenFrame(title = "Events & Competitions", onBack = { nav.back() }) {
        PillButton("New event", Modifier.fillMaxWidth()) { creating = true }
        Muted("Published events appear on every user's dashboard and Events tab, and users who want event updates get a notification.")
        if (events.isEmpty()) EmptyState("No events yet", "Create one and switch Publish on to send it to users.")
        events.forEach { e ->
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(e.title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    if (e.published) GoodChip("Published") else WarnChip("Draft")
                }
                Muted("${e.type.label} · ${e.date.pretty()} · ${e.venue}")
                Muted("${e.responses.size} responses · respond by ${e.deadline.pretty()}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { editing = e }) { Text("Edit") }
                    OutlinedButton(onClick = { vm.setEventPublished(e.id, !e.published) }) { Text(if (e.published) "Unpublish" else "Publish") }
                    TextButton(onClick = { deleting = e }) { Text("Delete") }
                }
            }
        }
    }

    if (creating || editing != null) {
        ModalBottomSheet(
            onDismissRequest = { creating = false; editing = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            EventForm(editing, vm.today()) { ev ->
                if (vm.saveEvent(ev) is Outcome.Done) { creating = false; editing = null }
            }
        }
    }
    deleting?.let { e ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete this event?") },
            text = { Text("\"${e.title}\" will be removed for everyone.") },
            confirmButton = { TextButton(onClick = { vm.deleteEvent(e.id); deleting = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun EventForm(existing: SportsEvent?, today: LocalDate, onSave: (SportsEvent) -> Unit) {
    var title by rememberSaveable { mutableStateOf(existing?.title ?: "") }
    var type by rememberSaveable { mutableStateOf(existing?.type ?: EventType.TOURNAMENT) }
    var sport by rememberSaveable { mutableStateOf(existing?.sport ?: "Any") }
    var date by remember { mutableStateOf(existing?.date) }
    var deadline by remember { mutableStateOf(existing?.deadline) }
    var venue by rememberSaveable { mutableStateOf(existing?.venue ?: "") }
    var description by rememberSaveable { mutableStateOf(existing?.description ?: "") }
    var publish by rememberSaveable { mutableStateOf(existing?.published ?: true) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(if (existing == null) "New event" else "Edit event", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Field(title, { title = it; errors = errors - "title" }, "Event name", error = errors["title"])
        Text("Type", style = MaterialTheme.typography.labelLarge)
        FilterRow(EventType.values().toList(), type, { it.label }) { type = it }
        Text("Sport", style = MaterialTheme.typography.labelLarge)
        FilterRow(listOf("Any", "Soccer", "Netball"), sport, { it }) { sport = it }
        DateField("Event date", date, errors["date"]) { date = it; errors = errors - "date" }
        DateField("Last day to respond", deadline, errors["deadline"]) { deadline = it; errors = errors - "deadline" }
        Field(venue, { venue = it; errors = errors - "venue" }, "Venue", error = errors["venue"])
        Field(description, { description = it; errors = errors - "description" }, "Details", error = errors["description"], singleLine = false, minLines = 3, hint = "${description.length}/600")
        SwitchRow("Publish to users", publish, "On: shows on every user's dashboard and sends a notification.") { publish = it }
        PillButton(if (publish) "Save and publish" else "Save as draft", Modifier.fillMaxWidth()) {
            errors = Validators.event(title, venue, description, date, deadline)
            if (errors.isEmpty()) {
                onSave(SportsEvent(existing?.id ?: "", title, type, sport, date!!, venue, deadline!!, description, publish, existing?.createdBy ?: ""))
            }
        }
    }
}
