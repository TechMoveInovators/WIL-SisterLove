package com.example.slisaconnect.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*

/** Totals only. Nothing here names a young person or exposes support records unless the role allows it. */
@Composable
fun ReportsScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ScreenFrame(title = "Reports & Impact", onBack = { nav.back() }) {
        if (state.can(Permission.USERS_VIEW)) {
            SectionTitle("People")
            Panel {
                FactRow("Accounts", "${state.users.size}")
                FactRow("Staff", "${state.users.count { it.role.isStaff }}")
                FactRow("Suspended", "${state.users.count { !it.active }}")
            }
        }
        if (state.can(Permission.TEAMS_REVIEW)) {
            SectionTitle("Teams")
            Panel {
                TeamStatus.values().forEach { s -> FactRow(s.label, "${state.teams.count { it.status == s }}") }
                FactRow("Players on rosters", "${state.teams.sumOf { it.players.size }}")
                FactRow("Play for Change approved", "${state.teams.count { it.challenge == ChallengeStatus.APPROVED }}")
                FactRow("Competition entries accepted", "${state.teams.sumOf { t -> t.entries.count { it.status == EntryStatus.ACCEPTED } }}")
            }
        }
        if (state.can(Permission.EVENTS_MANAGE)) {
            SectionTitle("Events")
            Panel {
                FactRow("Published", "${state.events.count { it.published }}")
                FactRow("Responses", "${state.events.sumOf { it.responses.size }}")
            }
        }
        if (state.can(Permission.LEARNING_MANAGE)) {
            SectionTitle("Learning")
            Panel { FactRow("Articles published", "${state.articles.count { it.published }}") }
        }
        if (state.can(Permission.SUPPORT_VIEW)) {
            SectionTitle("Support contacts")
            Panel { ResourceStatus.values().forEach { s -> FactRow(s.label, "${state.resources.count { it.status == s }}") } }
        }
        if (state.can(Permission.AUDIT_VIEW)) {
            SectionTitle("Activity log")
            if (state.audit.isEmpty()) Muted("Nothing recorded yet.")
            state.audit.sortedByDescending { it.time }.take(30).forEach { a ->
                Panel {
                    Text(a.message, fontWeight = FontWeight.SemiBold)
                    Muted("${a.actor} · ${a.time.timeText()}")
                }
            }
        }
    }
}
