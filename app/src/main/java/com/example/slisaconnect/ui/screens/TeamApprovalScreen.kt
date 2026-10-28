
package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.slisaconnect.model.*
import com.example.slisaconnect.ui.*

@Composable
fun TeamApprovalScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<TeamStatus?>(null) }
    val teams = state.teams.filter { filter == null || it.status == filter }.sortedBy { it.submitted }
    val decide = state.can(Permission.TEAMS_DECIDE)
    val approveChallenge = state.can(Permission.CHALLENGE_APPROVE)

    ScreenFrame(title = "Team Approval", onBack = { nav.back() }) {
        if (!decide) NoteBox("Your role can view teams but not decide registrations.")
        FilterRow(listOf<TeamStatus?>(null) + TeamStatus.values().toList(), filter, { it?.label ?: "All" }) { filter = it }
        if (teams.isEmpty()) EmptyState("No teams here", "Registered teams show up as soon as a manager submits them.")
        teams.forEach { t -> TeamReview(t, state, decide, approveChallenge, vm) }
    }
}

@Composable
private fun TeamReview(t: Team, state: AppState, decide: Boolean, approveChallenge: Boolean, vm: AppViewModel) {
    var open by rememberSaveable(t.id) { mutableStateOf(false) }
    var asking by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    Panel(onClick = { open = !open }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(t.name, fontWeight = FontWeight.Bold)
                Muted("${t.sport} · ${t.ageGroup} · ${t.category} · ${t.province}")
                Mono(t.reference)
            }
            StatusChip(t.status)
        }
        Muted("Manager ${state.userName(t.managerId)} · coach ${t.coach} · submitted ${t.submitted.pretty()}")
        Muted("${t.players.size} players · consent ${t.consentReceived}/${t.consentRequired}")

        if (open) {
            t.checklist().forEach { n ->
                NoteBox("${n.title}: ${n.body}", if (n.urgent) NoteKind.WARN else NoteKind.NEUTRAL)
            }
            if (t.players.isNotEmpty()) {
                Text("Roster (initials and age only)", fontWeight = FontWeight.SemiBold)
                Muted(t.players.joinToString(", ") { "${it.initials} ${it.age}" + if (it.needsConsent && !it.guardianConsent) " (no consent)" else "" })
            }
            if (decide) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (t.status == TeamStatus.SUBMITTED) Button(onClick = { vm.startReview(t.id) }) { Text("Start review") }
                    if (t.status == TeamStatus.UNDER_REVIEW) {
                        Button(onClick = { vm.approveTeam(t.id) }, enabled = t.readyForApproval) { Text("Approve") }
                        OutlinedButton(onClick = { asking = true }) { Text("Request changes") }
                    }
                }
                if (t.status == TeamStatus.UNDER_REVIEW && !t.readyForApproval) Muted("Can't approve until the roster has players and every guardian consent is in.")
                if (!t.allConsentsIn && t.status != TeamStatus.APPROVED) {
                    OutlinedButton(onClick = { vm.sendConsentReminder(t.id) }) { Text("Remind manager about consent") }
                }
            }
            if (approveChallenge && t.challenge == ChallengeStatus.SUBMITTED) {
                Button(onClick = { vm.approveChallenge(t.id) }) { Text("Approve Play for Change activity") }
            }
            if (decide) {
                t.entries.forEachIndexed { i, e ->
                    if (e.status == EntryStatus.PENDING) {
                        Text("Entry request: ${e.competition}", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.decideEntry(t.id, i, EntryStatus.ACCEPTED) }) { Text("Accept") }
                            OutlinedButton(onClick = { vm.decideEntry(t.id, i, EntryStatus.DECLINED) }) { Text("Decline") }
                        }
                    }
                }
            }
        } else {
            Muted("Tap to review")
        }
    }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text("Request changes") },
            text = { Field(note, { note = it }, "What needs fixing?", singleLine = false, minLines = 3) },
            confirmButton = {
                TextButton(onClick = {
                    if (vm.requestChanges(t.id, note) is Outcome.Done) { asking = false; note = "" }
                }) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { asking = false }) { Text("Cancel") } },
        )
    }
}
