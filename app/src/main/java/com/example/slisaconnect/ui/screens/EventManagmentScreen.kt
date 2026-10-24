package com.example.slisaconnect.ui.screens

@file:OptIn(ExperimentalMaterial3Api::class)


import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*

@Composable
fun EventManagementScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }   // "" means a new event
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val events = state.events.sortedBy { it.date }
    val toDelete = state.events.firstOrNull { it.id == deleteId }

    ScreenFrame(title = "Event management", route = "/admin/events", onBack = onBack) {
        LimeButton("New event", Modifier.fillMaxWidth()) { editingId = "" }
        if (events.isEmpty()) Panel { Text("No events yet. Create the first one to see it here.") }
        events.forEach { e ->
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(e.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${e.type.label} · ${e.sport}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (e.published) GoodChip("Published") else PlainChip("Draft")
                }
                Text("${e.date.pretty()} · ${e.venue}", style = MaterialTheme.typography.bodySmall)
                Text("Registration closes ${e.deadline.pretty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { editingId = e.id }) { Icon(Icons.Default.Edit, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Edit") }
                    OutlinedButton(onClick = { vm.setEventPublished(e.id, !e.published) }) { Text(if (e.published) "Unpublish" else "Publish") }
                    OutlinedButton(onClick = { deleteId = e.id }) { Text("Delete") }
                }
            }
        }
    }

    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Delete this event?") },
            text = { Text("\"${toDelete.title}\" will be removed. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { vm.deleteEvent(toDelete.id); deleteId = null }) { Text("Yes, delete") } },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Keep it") } },
        )
    }

    val editing = editingId
    if (editing != null) {
        ModalBottomSheet(
            onDismissRequest = { editingId = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            EventForm(
                existing = state.events.firstOrNull { it.id == editing },
                onSave = { event -> if (vm.saveEvent(event) is Outcome.Done) editingId = null },
            )
        }
    }
}

@Composable
private fun EventForm(existing: SportsEvent?, onSave: (SportsEvent) -> Unit) {
    var title by rememberSaveable { mutableStateOf(existing?.title ?: "") }
    var venue by rememberSaveable { mutableStateOf(existing?.venue ?: "") }
    var date by rememberSaveable { mutableStateOf(existing?.date?.toString() ?: "") }
    var deadline by rememberSaveable { mutableStateOf(existing?.deadline?.toString() ?: "") }
    var type by rememberSaveable { mutableStateOf(existing?.type ?: EventType.TOURNAMENT) }
    var sport by rememberSaveable { mutableStateOf(existing?.sport ?: "Any") }
    var published by rememberSaveable { mutableStateOf(existing?.published ?: false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(if (existing == null) "New event" else "Edit event", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(title, { title = it }, label = { Text("Event name") }, singleLine = true, isError = "title" in errors,
            supportingText = { errors["title"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())

        Text("Type", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EventType.values().forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.label) }) }
        }
        Text("Sport", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Any", "Soccer", "Netball").forEach { s -> FilterChip(selected = sport == s, onClick = { sport = s }, label = { Text(s) }) }
        }

        OutlinedTextField(date, { date = it }, label = { Text("Event date (yyyy-mm-dd)") }, singleLine = true, isError = "date" in errors,
            supportingText = { errors["date"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(deadline, { deadline = it }, label = { Text("Registration deadline (yyyy-mm-dd)") }, singleLine = true, isError = "deadline" in errors,
            supportingText = { errors["deadline"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(venue, { venue = it }, label = { Text("Venue") }, singleLine = true, isError = "venue" in errors,
            supportingText = { errors["venue"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Publish now (visible to all users)")
            Switch(checked = published, onCheckedChange = { published = it })
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val d = Validators.parseDate(date)
                val dl = Validators.parseDate(deadline)
                errors = Validators.event(title, venue, d, dl)
                if (errors.isEmpty()) {
                    onSave(SportsEvent(existing?.id ?: "", title.trim(), type, sport, d!!, venue.trim(), dl!!, published))
                }
            },
        ) { Text("Save event") }
    }
}
