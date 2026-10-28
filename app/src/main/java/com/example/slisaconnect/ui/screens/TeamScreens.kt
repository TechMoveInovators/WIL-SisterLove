
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slisaconnect.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import za.org.slisa.connect.data.AppRepository
import za.org.slisa.connect.model.*
import za.org.slisa.connect.ui.*
import za.org.slisa.connect.ui.theme.*

/** Youth Base: the hub from the Youth tile. Team registration is the main action. */
@Composable
fun YouthBaseScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val teams = state.myTeams
    ScreenFrame(title = "Youth Base", onBack = { nav.back() }, actions = { BellAction(state.unreadCount) { nav.to(Routes.NOTIFICATIONS) } }) {
        NightPanel {
            Text("Register once. Play all season.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Set up your soccer or netball team, keep one roster, and use the same team profile to enter SLiSA competitions.",
                style = MaterialTheme.typography.bodySmall,
            )
            LimeButton("Register your team", Modifier.fillMaxWidth()) { nav.to(Routes.TEAM_REGISTER) }
        }
        SectionTitle("My teams")
        if (teams.isEmpty()) EmptyState("No team yet", "Teams you register or coach will be listed here.")
        teams.forEach { t ->
            ListRow(
                title = t.name,
                subtitle = "${t.sport} · ${t.ageGroup} · ${t.reference}",
                onClick = { nav.to(Routes.team(t.id)) },
                trailing = { StatusChip(t.status) },
            )
        }
        SectionTitle("More for young people")
        ListRow("Find events", "Tournaments, training and sports days", onClick = { nav.tab(Routes.EVENTS) })
        ListRow("Learn", "SRHR, GBVF, HIV & PrEP", onClick = { nav.tab(Routes.LEARN) })
    }
}

// ---------------------------------------------------------------------------------------------
// Register a team
// ---------------------------------------------------------------------------------------------

@Composable
fun RegisterTeamScreen(nav: Navigator, vm: AppViewModel = viewModel()) {
    var name by rememberSaveable { mutableStateOf("") }
    var sport by rememberSaveable { mutableStateOf("Soccer") }
    var age by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var province by rememberSaveable { mutableStateOf("") }
    var coach by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    ScreenFrame(title = "Register your team", onBack = { nav.back() }) {
        NoteBox("No identity documents are needed. You will add players by initials and age, and collect guardian consent for anyone under 18.", NoteKind.GOOD)

        Text("Sport", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ColourTile("Soccer", "Boys, girls, mixed", Icons.Default.Star, TileBlue, Modifier.weight(1f).alpha(if (sport == "Soccer") 1f else 0.55f)) { sport = "Soccer" }
            ColourTile("Netball", "Girls, mixed", Icons.Default.Favorite, TilePink, Modifier.weight(1f).alpha(if (sport == "Netball") 1f else 0.55f)) { sport = "Netball" }
        }
        Field(name, { name = it; errors = errors - "name" }, "Team name", error = errors["name"])
        DropdownField("Age group", age, Validators.teamAgeGroups, errors["ageGroup"]) { age = it; errors = errors - "ageGroup" }
        DropdownField("Category", category, Validators.teamCategories, errors["category"]) { category = it; errors = errors - "category" }
        DropdownField("Province", province, Validators.provinces, errors["province"]) { province = it; errors = errors - "province" }
        Field(coach, { coach = it; errors = errors - "coach" }, "Coach's name", error = errors["coach"], imeAction = ImeAction.Done)

        PillButton("Submit registration", Modifier.fillMaxWidth()) {
            errors = Validators.team(name, sport, age, category, province, coach)
            if (errors.isEmpty()) {
                val result = vm.registerTeam(NewTeam(name.trim(), sport, age, category, province, coach.trim()))
                if (result is Outcome.Done && result.id != null) nav.replace(Routes.team(result.id), Routes.TEAM_REGISTER)
            }
        }
        Muted("You will get a reference number and the status Submitted. Registering a team does not enter it into a tournament.")
    }
}

// ---------------------------------------------------------------------------------------------
// Team dashboard
// ---------------------------------------------------------------------------------------------

private val tabTitles = listOf("Overview", "Players", "Compete", "Team")

@Composable
fun TeamDashboardScreen(teamId: String, nav: Navigator, vm: AppViewModel = viewModel()) {
    val onBack = { nav.back() }
    val state by vm.state.collectAsStateWithLifecycle()
    val team = state.teams.firstOrNull { it.id == teamId }
    var tab by rememberSaveable { mutableStateOf(0) }

    // A team is private to its manager and coaches. Anyone else is turned away.
    if (team == null || !state.canEditRoster(team)) {
        ScreenFrame(title = "Team", onBack = onBack) {
            Panel {
                Text("This team isn't available to you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Only the team manager and authorised coaches can open a team.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onBack) { Text("Go back") }
            }
        }
        return
    }
    val isManager = team.managerId == state.currentUserId

    ScreenFrame(title = team.name, onBack = onBack) {
        TabRow(selectedTabIndex = tab) {
            tabTitles.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (tab) {
                0 -> OverviewTab(team, state, isManager, vm)
                1 -> PlayersTab(team, vm)
                2 -> CompeteTab(team, state, isManager, vm)
                else -> TeamTab(team, state, isManager, vm)
            }
        }
    }
}

// ---- Overview: sport, age group, coach, status, notifications -----------------------------------

@Composable
private fun OverviewTab(team: Team, state: AppState, isManager: Boolean, vm: AppViewModel) {
    Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Registration status", fontWeight = FontWeight.Bold)
            StatusChip(team.status)
        }
        Text(team.status.message)
        Text("Reference ${team.reference}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (team.status == TeamStatus.ACTION_REQUIRED) {
            if (team.managerNote.isNotBlank()) NoteBox(team.managerNote, NoteKind.BAD)
            if (isManager) Button(onClick = { vm.resubmitTeam(team.id) }, Modifier.fillMaxWidth()) { Text("I've fixed it, resubmit") }
            else Text("The team manager can resubmit once the details are fixed.", style = MaterialTheme.typography.bodySmall)
        }
    }

    Panel {
        FactRow("Sport", team.sport)
        FactRow("Age group", team.ageGroup)
        FactRow("Category", team.category)
        FactRow("Province", team.province)
        FactRow("Coach", team.coach)
        FactRow("Manager", state.users.firstOrNull { it.id == team.managerId }?.name ?: "Unknown")
        FactRow("Players", "${team.players.size}")
    }
    NoteBox("Approved means the team is registered. Entering a competition is a separate step that a coordinator confirms.")

    SectionTitle("Notifications")
    team.checklist().forEach { n ->
        Panel {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (n.urgent) WarnChip("To do")
                Text(n.title, fontWeight = FontWeight.SemiBold)
            }
            Text(n.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---- Players: add, edit, remove -----------------------------------------------------------------

@Composable
private fun PlayersTab(team: Team, vm: AppViewModel) {
    var editing by rememberSaveable { mutableStateOf(NONE) }     // NONE = closed, NEW = add, otherwise the index
    val locked = team.status == TeamStatus.UNDER_REVIEW
    val full = team.players.size >= AppRepository.MAX_PLAYERS

    NoteBox("Initials and age only. Never enter full names, ID numbers or contact details. The roster is visible only to your team and authorised coordinators.")
    if (locked) NoteBox("The roster is locked while a coordinator reviews the team.", NoteKind.WARN)
    SectionTitle("Manage players", "${team.players.size} of ${AppRepository.MAX_PLAYERS}")
    LimeButton("Add player", Modifier.fillMaxWidth(), enabled = !locked && !full) { editing = NEW }
    if (team.players.isEmpty()) Panel { Text("No players yet. Add your first player to get started.", color = MaterialTheme.colorScheme.onSurfaceVariant) }

    team.players.forEachIndexed { i, p ->
        Panel {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("${p.initials} · ${p.age}", fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            !p.needsConsent -> "Adult, no guardian consent needed"
                            p.guardianConsent -> "Guardian consent form received"
                            else -> "Guardian consent missing"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (p.needsConsent && !p.guardianConsent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { editing = i }, enabled = !locked) { Text("Edit") }
                TextButton(onClick = { vm.removePlayer(team.id, i) }, enabled = !locked) { Text("Remove") }
            }
        }
    }

    if (editing != NONE) {
        ModalBottomSheet(
            onDismissRequest = { editing = NONE },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            PlayerForm(
                existing = team.players.getOrNull(editing),
                onSave = { player ->
                    val result = if (editing == NEW) vm.addPlayer(team.id, player) else vm.updatePlayer(team.id, editing, player)
                    if (result is Outcome.Done) editing = NONE
                },
            )
        }
    }
}

private const val NONE = -2
private const val NEW = -1

@Composable
private fun PlayerForm(existing: Player?, onSave: (Player) -> Unit) {
    var initials by rememberSaveable { mutableStateOf(existing?.initials ?: "") }
    var age by rememberSaveable { mutableStateOf(existing?.age?.toString() ?: "") }
    var consent by rememberSaveable { mutableStateOf(existing?.guardianConsent ?: false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val ageNumber = age.toIntOrNull()

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(if (existing == null) "Add player" else "Edit player", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            initials, { initials = it; errors = errors - "initials" }, label = { Text("Initials only (for example T.M.)") }, singleLine = true,
            isError = "initials" in errors, supportingText = { errors["initials"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            age, { age = it.filter(Char::isDigit).take(2); errors = errors - "age" }, label = { Text("Age") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = "age" in errors, supportingText = { errors["age"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth(),
        )
        if (ageNumber != null && ageNumber < 18) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Guardian consent form received", Modifier.weight(1f))
                Switch(checked = consent, onCheckedChange = { consent = it })
            }
            NoteBox("A coordinator checks consent forms before approving the team. Keep the signed form safe. Do not upload it here.")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val clean = Validators.initials(initials)
                errors = buildMap {
                    if (clean == null) put("initials", "Use initials only, like T.M.")
                    if (ageNumber == null || ageNumber !in 5..40) put("age", "Enter an age between 5 and 40.")
                }
                if (errors.isEmpty()) onSave(Player(clean!!, ageNumber!!, consent && ageNumber < 18))
            },
        ) { Text("Save player") }
    }
}

// ---- Compete: competitions, deadlines, challenge, team events -----------------------------------

@Composable
private fun CompeteTab(team: Team, state: AppState, isManager: Boolean, vm: AppViewModel) {
    val today = LocalDate.now()
    val sportMatches = { e: SportsEvent -> e.sport.equals("Any", true) || e.sport.equals(team.sport, true) }

    SectionTitle("Competitions entered")
    if (team.entries.isEmpty()) Panel { Text("No competitions yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    team.entries.forEach { e ->
        val event = state.events.firstOrNull { it.title == e.competition }
        Panel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(e.competition, fontWeight = FontWeight.SemiBold)
                    if (event != null) {
                        Text("${event.date.pretty()} · ${event.venue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Registration deadline ${event.deadline.pretty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                when (e.status) {
                    EntryStatus.ACCEPTED -> GoodChip("Accepted")
                    EntryStatus.DECLINED -> PillChip("Declined", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                    EntryStatus.PENDING -> PlainChip("Pending")
                }
            }
        }
    }

    SectionTitle("Enter a competition")
    if (team.status != TeamStatus.APPROVED) {
        NoteBox("You can enter competitions once the team is approved.")
    } else {
        val open = state.events.filter {
            it.published && it.type.isCompetition && sportMatches(it) && !it.deadline.isBefore(today) && team.entries.none { en -> en.competition == it.title }
        }.sortedBy { it.date }
        if (open.isEmpty()) Panel { Text("No open ${team.sport.lowercase()} competitions right now.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        open.forEach { e ->
            Panel {
                Text(e.title, fontWeight = FontWeight.SemiBold)
                Text("${e.date.pretty()} · ${e.venue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Register by ${e.deadline.pretty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isManager) Button(onClick = { vm.requestEntry(team.id, e.id) }) { Text("Request entry") }
            }
        }
    }

    SectionTitle("Play for Change challenge")
    Panel {
        when (team.challenge) {
            ChallengeStatus.APPROVED -> GoodChip(team.challenge.label)
            ChallengeStatus.SUBMITTED -> WarnChip(team.challenge.label)
            ChallengeStatus.NOT_STARTED -> PlainChip(team.challenge.label)
        }
        Text(
            "Complete a short learning activity approved by a facilitator as part of your tournament. It links your sport to SLiSA's work on preventing gender-based violence and empowering young people.",
            style = MaterialTheme.typography.bodySmall,
        )
        if (isManager && team.challenge == ChallengeStatus.NOT_STARTED) {
            if (team.status == TeamStatus.APPROVED) Button(onClick = { vm.submitChallenge(team.id) }) { Text("Submit learning activity") }
            else Text("Available once the team is approved.", style = MaterialTheme.typography.bodySmall)
        }
    }

    SectionTitle("Team events")
    val upcoming = state.events.filter { it.published && sportMatches(it) && !it.date.isBefore(today) }.sortedBy { it.date }
    if (upcoming.isEmpty()) Panel { Text("No upcoming events.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    upcoming.forEach { e ->
        Panel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(e.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                PlainChip(e.type.label)
            }
            Text("${e.date.pretty()} · ${e.venue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---- Team: profile and coaches ------------------------------------------------------------------

@Composable
private fun TeamTab(team: Team, state: AppState, isManager: Boolean, vm: AppViewModel) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var coachEmail by rememberSaveable { mutableStateOf("") }

    SectionTitle("Team profile")
    Panel {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(team.name, 52)
            Column {
                Text(team.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${team.sport} · ${team.ageGroup}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(team.description.ifBlank { "No description yet." })
        FactRow("Contact email", team.contactEmail.ifBlank { "Not set" })
        FactRow("Contact phone", team.contactPhone.ifBlank { "Not set" })
        Text("Logo upload arrives with the backend. Until then the team shows its initials.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (isManager) OutlinedButton(onClick = { editing = true }) { Text("Edit team profile") }
        else Text("Only the team manager can change the team profile.", style = MaterialTheme.typography.bodySmall)
    }

    SectionTitle("People with access")
    Panel {
        FactRow("Primary manager", state.users.firstOrNull { it.id == team.managerId }?.name ?: "Unknown")
        if (team.coachIds.isEmpty()) Text("No additional coaches.", style = MaterialTheme.typography.bodySmall)
        team.coachIds.forEach { id ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Coach: ${state.users.firstOrNull { it.id == id }?.name ?: "Unknown"}")
                if (isManager) TextButton(onClick = { vm.removeCoach(team.id, id) }) { Text("Remove") }
            }
        }
        if (isManager) {
            OutlinedTextField(
                coachEmail, { coachEmail = it }, label = { Text("Add a coach by email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                supportingText = { Text("They need a SLiSA Connect account. Coaches can manage players but not team details.") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = { vm.addCoachByEmail(team.id, coachEmail); coachEmail = "" }, enabled = coachEmail.isNotBlank()) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Add coach")
            }
        }
    }
    Text("Player access can be added later.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

    if (editing) {
        ModalBottomSheet(onDismissRequest = { editing = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            TeamProfileForm(team) { d, e, p -> if (vm.updateTeamProfile(team.id, d, e, p) is Outcome.Done) editing = false }
        }
    }
}

@Composable
private fun TeamProfileForm(team: Team, onSave: (String, String, String) -> Unit) {
    var description by rememberSaveable { mutableStateOf(team.description) }
    var email by rememberSaveable { mutableStateOf(team.contactEmail) }
    var phone by rememberSaveable { mutableStateOf(team.contactPhone) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Edit team profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        NoteBox("Use general contact details for the team, not a personal number.")
        OutlinedTextField(
            description, { description = it }, label = { Text("Description") }, minLines = 3,
            isError = "description" in errors, supportingText = { Text(errors["description"] ?: "${description.length}/200") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            email, { email = it }, label = { Text("General contact email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = "email" in errors, supportingText = { errors["email"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            phone, { phone = it }, label = { Text("General contact phone") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = "phone" in errors, supportingText = { errors["phone"]?.let { Text(it) } }, modifier = Modifier.fillMaxWidth(),
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                errors = Validators.teamContact(description, email, phone)
                if (errors.isEmpty()) onSave(description, email, phone)
            },
        ) { Text("Save") }
    }
}
