package com.example.slisaconnect.ui.screens

@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun TeamApprovalScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<TeamStatus?>(null) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = state.teams.firstOrNull { it.id == selectedId }
    val teams = state.teams.filter { filter == null || it.status == filter }

    ScreenFrame(title = "Team approval", route = "/admin/teams", onBack = onBack) {
        NoteBox("Approving a registration does not enter a team into a tournament. Each competition entry is decided separately.")

        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
            TeamStatus.values().forEach { s ->
                FilterChip(
                    selected = filter == s,
                    onClick = { filter = s },
                    label = { Text("${s.label} ${state.teams.count { it.status == s }}") },
                )
            }
        }

        if (teams.isEmpty()) {
            Panel { Text("No teams have this status.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        teams.forEach { t ->
            Panel(onClick = { selectedId = t.id }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(t.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${t.sport} · ${t.ageGroup} · Coach ${t.coach}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusChip(t.status)
                }
                Text("${t.reference} · submitted ${t.submitted.pretty()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${t.players.size} players · consent ${t.consentReceived}/${t.consentRequired}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedId = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) { TeamSheet(selected, state, vm) }
    }
}

@Composable
private fun TeamSheet(team: Team, state: AppState, vm: AppViewModel) {
    val canDecide = state.can(Permission.TEAMS_DECIDE)
    var note by rememberSaveable(team.id) { mutableStateOf("") }
    var noteError by remember(team.id) { mutableStateOf<String?>(null) }
    val managerName = state.users.firstOrNull { it.id == team.managerId }?.name ?: "Unknown"

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(team.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(team.reference, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(team.status)
            Text(team.status.message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
        if (team.managerNote.isNotBlank()) NoteBox("Sent to manager: ${team.managerNote}", NoteKind.BAD)

        FactRow("Sport", team.sport)
        FactRow("Age group", team.ageGroup)
        FactRow("Coach", team.coach)
        FactRow("Manager", managerName)

        SectionTitle("Guardian consent", "${team.consentReceived} of ${team.consentRequired}")
        LinearProgressIndicator(
            progress = { if (team.consentRequired == 0) 1f else team.consentReceived / team.consentRequired.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = if (team.allConsentsIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        )

        SectionTitle("Roster", "Private · initials only")
        Column(Modifier.fillMaxWidth().heightIn(max = 160.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            team.players.forEach { p ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${p.initials} · ${p.age}")
                    Text(
                        when {
                            !p.needsConsent -> "Adult"
                            p.guardianConsent -> "Consent"
                            else -> "Missing"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (p.needsConsent && !p.guardianConsent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SectionTitle("Play for Change challenge")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (team.challenge) {
                ChallengeStatus.APPROVED -> GoodChip(team.challenge.label)
                ChallengeStatus.SUBMITTED -> WarnChip(team.challenge.label)
                ChallengeStatus.NOT_STARTED -> PlainChip(team.challenge.label)
            }
            if (team.challenge == ChallengeStatus.SUBMITTED) {
                if (state.can(Permission.CHALLENGE_APPROVE)) {
                    Button(onClick = { vm.approveChallenge(team.id) }) { Text("Approve activity") }
                } else {
                    Text("A learning facilitator approves this.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SectionTitle("Competition entries")
        if (team.entries.isEmpty()) Text("No competitions entered yet.", style = MaterialTheme.typography.bodySmall)
        team.entries.forEachIndexed { i, e ->
            Panel {
                Text(e.competition, fontWeight = FontWeight.SemiBold)
                if (e.status == EntryStatus.PENDING && canDecide && team.status == TeamStatus.APPROVED) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LimeButton("Accept") { vm.decideEntry(team.id, i, EntryStatus.ACCEPTED) }
                        OutlinedButton(onClick = { vm.decideEntry(team.id, i, EntryStatus.DECLINED) }) { Text("Decline") }
                    }
                } else {
                    when (e.status) {
                        EntryStatus.ACCEPTED -> GoodChip("Entry accepted")
                        EntryStatus.DECLINED -> PillChip("Entry declined", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                        EntryStatus.PENDING -> PlainChip("Entry pending")
                    }
                }
            }
        }
        if (team.status != TeamStatus.APPROVED && team.entries.isNotEmpty()) {
            Text("Entries can be accepted once the team is approved.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SectionTitle("Decision")
        if (!canDecide) {
            NoteBox("View only. Your role cannot change registration decisions.")
        } else when (team.status) {
            TeamStatus.SUBMITTED -> Button(onClick = { vm.startReview(team.id) }, Modifier.fillMaxWidth()) { Text("Start review") }
            TeamStatus.UNDER_REVIEW -> {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it; noteError = null },
                    label = { Text("Message to the manager (needed to request changes)") },
                    isError = noteError != null,
                    supportingText = { noteError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LimeButton("Approve team", enabled = team.readyForApproval) { vm.approveTeam(team.id) }
                    OutlinedButton(onClick = {
                        if (note.isBlank()) noteError = "Add a message so the manager knows what to fix."
                        else vm.requestChanges(team.id, note)
                    }) { Text("Request changes") }
                }
                if (team.players.isEmpty()) {
                    NoteBox("Approve once the manager has added players to the roster.", NoteKind.WARN)
                } else if (!team.allConsentsIn) {
                    NoteBox("Approve once all ${team.consentRequired - team.consentReceived} missing guardian consents are in.", NoteKind.WARN)
                }
            }
            TeamStatus.ACTION_REQUIRED -> NoteBox("Waiting for the manager to correct the details.", NoteKind.WARN)
            TeamStatus.APPROVED -> NoteBox("Team is registered. Tournament entries are decided above.", NoteKind.GOOD)
        }
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
