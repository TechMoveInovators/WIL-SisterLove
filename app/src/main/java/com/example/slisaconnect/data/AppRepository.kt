package com.example.slisaconnect.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.slisaconnect.model.*

/**
 * In-memory store. Every write checks the signed-in user's permission here, not only in the UI.
 * When you add a backend, keep these same rules there (see README, "Server-side rules").
 */
class AppRepository(
    seed: AppState = SeedData.initial(),
    private val clock: () -> String = { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) },
    private val today: () -> LocalDate = { LocalDate.now() },
) {
    private val _state = MutableStateFlow(seed)
    val state: StateFlow<AppState> = _state.asStateFlow()

    private sealed interface Change {
        data class Ok(val state: AppState, val message: String, val audit: String? = null, val resultId: String? = null) : Change
        data class Fail(val message: String) : Change
    }

    private fun guarded(permission: Permission?, block: (AppState) -> Change): Outcome {
        val s = _state.value
        if (permission != null && !s.can(permission)) return Outcome.Failed("Your role can't do this.")
        return when (val c = block(s)) {
            is Change.Fail -> Outcome.Failed(c.message)
            is Change.Ok -> {
                val audit = c.audit?.let { listOf(AuditEntry(clock(), s.currentUser.name, it)) + c.state.audit }
                    ?: c.state.audit
                _state.value = c.state.copy(audit = audit.take(20))
                Outcome.Done(c.message, c.resultId)
            }
        }
    }

    private fun AppState.withTeam(id: String, f: (Team) -> Team) =
        copy(teams = teams.map { if (it.id == id) f(it) else it })

    // ---- demo session -------------------------------------------------------------------------

    /** Prototype only: lets you try each role. Remove when real sign-in exists. */
    fun switchUser(id: String) {
        if (_state.value.users.any { it.id == id && it.active }) _state.value = _state.value.copy(currentUserId = id)
    }

    // ---- teams --------------------------------------------------------------------------------

    fun startReview(teamId: String): Outcome = guarded(Permission.TEAMS_DECIDE) { s ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.SUBMITTED) return@guarded Change.Fail("Only submitted teams can be reviewed.")
        Change.Ok(s.withTeam(teamId) { it.copy(status = TeamStatus.UNDER_REVIEW) }, "Review started.", "Started review of ${t.name}")
    }

    /** Approving a team registers it. It does not enter the team into any competition. */
    fun approveTeam(teamId: String): Outcome = guarded(Permission.TEAMS_DECIDE) { s ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.UNDER_REVIEW) return@guarded Change.Fail("Start a review first.")
        if (t.players.isEmpty()) return@guarded Change.Fail("The roster is empty.")
        if (!t.allConsentsIn) return@guarded Change.Fail("Guardian consent is incomplete.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(status = TeamStatus.APPROVED, managerNote = "") },
            "Team approved. Competition entries are still decided separately.",
            "Approved ${t.name}",
        )
    }

    fun requestChanges(teamId: String, note: String): Outcome = guarded(Permission.TEAMS_DECIDE) { s ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.UNDER_REVIEW) return@guarded Change.Fail("Start a review first.")
        if (note.isBlank()) return@guarded Change.Fail("Add a message so the manager knows what to fix.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(status = TeamStatus.ACTION_REQUIRED, managerNote = note.trim()) },
            "Message sent to the team manager.",
            "Asked ${t.name} to correct details",
        )
    }

    fun approveChallenge(teamId: String): Outcome = guarded(Permission.CHALLENGE_APPROVE) { s ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.challenge != ChallengeStatus.SUBMITTED) return@guarded Change.Fail("No activity is waiting for approval.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(challenge = ChallengeStatus.APPROVED) },
            "Challenge approved.",
            "Approved Play for Change activity for ${t.name}",
        )
    }

    fun decideEntry(teamId: String, index: Int, decision: EntryStatus): Outcome = guarded(Permission.TEAMS_DECIDE) { s ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.APPROVED) return@guarded Change.Fail("The team must be approved first.")
        val entry = t.entries.getOrNull(index) ?: return@guarded Change.Fail("Entry not found.")
        if (entry.status != EntryStatus.PENDING || decision == EntryStatus.PENDING) return@guarded Change.Fail("Entry already decided.")
        Change.Ok(
            s.withTeam(teamId) { team ->
                team.copy(entries = team.entries.mapIndexed { i, e -> if (i == index) e.copy(status = decision) else e })
            },
            "Entry ${decision.label.lowercase()}.",
            "${decision.label} ${t.name} for ${entry.competition}",
        )
    }

    // ---- users --------------------------------------------------------------------------------

    fun setRole(userId: String, role: Role): Outcome = guarded(Permission.USERS_MANAGE) { s ->
        val u = s.users.firstOrNull { it.id == userId } ?: return@guarded Change.Fail("User not found.")
        if (u.id == s.currentUserId) return@guarded Change.Fail("You cannot change your own role.")
        if (u.role == role) return@guarded Change.Fail("That is already their role.")
        val admins = s.users.count { it.role == Role.ADMIN && it.active }
        if (u.role == Role.ADMIN && admins < 2) return@guarded Change.Fail("Keep at least one active platform admin.")
        Change.Ok(
            s.copy(users = s.users.map { if (it.id == userId) it.copy(role = role) else it }),
            "Role updated.",
            "Changed ${u.name} from ${u.role.label} to ${role.label}",
        )
    }

    fun setActive(userId: String, active: Boolean): Outcome = guarded(Permission.USERS_MANAGE) { s ->
        val u = s.users.firstOrNull { it.id == userId } ?: return@guarded Change.Fail("User not found.")
        if (u.id == s.currentUserId) return@guarded Change.Fail("You cannot suspend yourself.")
        Change.Ok(
            s.copy(users = s.users.map { if (it.id == userId) it.copy(active = active) else it }),
            if (active) "Account reactivated." else "Account suspended.",
            "${if (active) "Reactivated" else "Suspended"} ${u.name}",
        )
    }

    // ---- events -------------------------------------------------------------------------------

    /** Creates the event when [event].id is blank, otherwise updates it. */
    fun saveEvent(event: SportsEvent): Outcome = guarded(Permission.EVENTS_MANAGE) { s ->
        val errors = Validators.event(event.title, event.venue, event.date, event.deadline)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val creating = event.id.isBlank()
        val saved = if (creating) event.copy(id = UUID.randomUUID().toString()) else event
        val events = if (creating) s.events + saved else s.events.map { if (it.id == saved.id) saved else it }
        Change.Ok(s.copy(events = events), "Event saved.", "${if (creating) "Created" else "Edited"} ${saved.title}")
    }

    fun setEventPublished(id: String, published: Boolean): Outcome = guarded(Permission.EVENTS_MANAGE) { s ->
        val e = s.events.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Event not found.")
        Change.Ok(
            s.copy(events = s.events.map { if (it.id == id) it.copy(published = published) else it }),
            if (published) "Event published." else "Event moved to drafts.",
            "${if (published) "Published" else "Unpublished"} ${e.title}",
        )
    }

    fun deleteEvent(id: String): Outcome = guarded(Permission.EVENTS_MANAGE) { s ->
        val e = s.events.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Event not found.")
        Change.Ok(s.copy(events = s.events.filter { it.id != id }), "Event deleted.", "Deleted ${e.title}")
    }

    // ---- support resources (private) ----------------------------------------------------------

    fun verifyResource(id: String): Outcome = guarded(Permission.REFERRALS_VERIFY) { s ->
        val r = s.resources.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Resource not found.")
        if (r.status != ResourceStatus.SUBMITTED) return@guarded Change.Fail("Already verified.")
        Change.Ok(
            s.copy(resources = s.resources.map { if (it.id == id) it.copy(status = ResourceStatus.VERIFIED) else it }),
            "Contact verified. An admin can now publish it.",
            "Verified contact for ${r.name}",
        )
    }

    fun publishResource(id: String): Outcome = guarded(Permission.SUPPORT_PUBLISH) { s ->
        val r = s.resources.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Resource not found.")
        if (r.status != ResourceStatus.VERIFIED) return@guarded Change.Fail("Only verified contacts can be published.")
        Change.Ok(
            s.copy(resources = s.resources.map { if (it.id == id) it.copy(status = ResourceStatus.PUBLISHED) else it }),
            "Resource published.",
            "Published ${r.name}",
        )
    }

    // ---- own profile (every signed-in user) ---------------------------------------------------

    fun updateProfile(profile: UserProfile): Outcome = guarded(null) { s ->
        val errors = Validators.profile(profile.first, profile.last, profile.email, profile.phone)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val me = s.currentUser
        Change.Ok(
            s.copy(
                profiles = s.profiles + (me.id to profile),
                users = s.users.map {
                    if (it.id == me.id) it.copy(name = "${profile.first.trim()} ${profile.last.trim()}", province = profile.province) else it
                },
            ),
            "Profile saved.",
        )
    }

    // ---- team managers and coaches (any signed-in user; ownership is checked) --------------------

    private val notYours = "You don't have access to this team."
    private fun AppState.ownTeam(id: String): Team? = teams.firstOrNull { it.id == id && it.managerId == currentUserId }
    private fun AppState.rosterTeam(id: String): Team? = teams.firstOrNull { it.id == id && canEditRoster(it) }

    /** Registers a team once. The result's id is the new team's id. Registering does not enter any competition. */
    fun registerTeam(draft: NewTeam): Outcome = guarded(null) { s ->
        val errors = Validators.team(draft.name, draft.sport, draft.ageGroup, draft.coach)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val name = draft.name.trim()
        if (s.teams.any { it.managerId == s.currentUserId && it.name.equals(name, ignoreCase = true) }) {
            return@guarded Change.Fail("You already registered a team with that name.")
        }
        val next = (s.teams.mapNotNull { it.reference.substringAfterLast('-').toIntOrNull() }.maxOrNull() ?: 0) + 1
        val now = today()
        val ref = String.format(Locale.ROOT, "SLC-T-%02d-%04d", now.year % 100, next)
        val team = Team(
            id = "t-" + UUID.randomUUID(), reference = ref, name = name, sport = draft.sport, ageGroup = draft.ageGroup,
            coach = draft.coach.trim(), managerId = s.currentUserId, status = TeamStatus.SUBMITTED, managerNote = "",
            submitted = now, players = emptyList(), challenge = ChallengeStatus.NOT_STARTED, entries = emptyList(),
        )
        Change.Ok(s.copy(teams = s.teams + team), "Registration received. Reference $ref.", "Registered team $name ($ref)", team.id)
    }

    fun resubmitTeam(teamId: String): Outcome = guarded(null) { s ->
        val t = s.ownTeam(teamId) ?: return@guarded Change.Fail(notYours)
        if (t.status != TeamStatus.ACTION_REQUIRED) return@guarded Change.Fail("Only teams marked Action required can be resubmitted.")
        Change.Ok(s.withTeam(teamId) { it.copy(status = TeamStatus.SUBMITTED, managerNote = "") }, "Resubmitted. Registration received.", "Resubmitted ${t.name}")
    }

    private fun checkPlayer(p: Player): Pair<Player?, String?> {
        val initials = Validators.initials(p.initials) ?: return null to "Use initials only, like T.M."
        if (p.age !in 5..40) return null to "Enter an age between 5 and 40."
        return p.copy(initials = initials, guardianConsent = p.guardianConsent && p.age < 18) to null
    }

    private fun rosterChange(teamId: String, change: (List<Player>) -> Pair<List<Player>?, String?>, done: String): Outcome =
        guarded(null) { s ->
            val t = s.rosterTeam(teamId) ?: return@guarded Change.Fail(notYours)
            if (t.status == TeamStatus.UNDER_REVIEW) return@guarded Change.Fail("The roster is locked while a coordinator reviews the team.")
            val (updated, error) = change(t.players)
            if (updated == null) return@guarded Change.Fail(error ?: "Could not update the roster.")
            Change.Ok(s.withTeam(teamId) { it.copy(players = updated) }, done)
        }

    fun addPlayer(teamId: String, player: Player): Outcome = rosterChange(teamId, { list ->
        val (p, err) = checkPlayer(player)
        when {
            p == null -> null to err
            list.size >= MAX_PLAYERS -> null to "A roster can have up to $MAX_PLAYERS players."
            else -> (list + p) to null
        }
    }, "Player added.")

    fun updatePlayer(teamId: String, index: Int, player: Player): Outcome = rosterChange(teamId, { list ->
        val (p, err) = checkPlayer(player)
        when {
            p == null -> null to err
            index !in list.indices -> null to "Player not found."
            else -> list.mapIndexed { i, old -> if (i == index) p else old } to null
        }
    }, "Player updated.")

    fun removePlayer(teamId: String, index: Int): Outcome = rosterChange(teamId, { list ->
        if (index !in list.indices) null to "Player not found." else list.filterIndexed { i, _ -> i != index } to null
    }, "Player removed.")

    fun updateTeamProfile(teamId: String, description: String, email: String, phone: String): Outcome = guarded(null) { s ->
        val t = s.ownTeam(teamId) ?: return@guarded Change.Fail(notYours)
        val errors = Validators.teamContact(description, email, phone)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        Change.Ok(
            s.withTeam(teamId) { it.copy(description = description.trim(), contactEmail = email.trim(), contactPhone = phone.trim()) },
            "Team profile saved.", "Updated team profile for ${t.name}",
        )
    }

    /** Asks to enter an approved team into a competition. A coordinator still has to accept the entry. */
    fun requestEntry(teamId: String, eventId: String): Outcome = guarded(null) { s ->
        val t = s.ownTeam(teamId) ?: return@guarded Change.Fail(notYours)
        if (t.status != TeamStatus.APPROVED) return@guarded Change.Fail("The team must be approved first.")
        val e = s.events.firstOrNull { it.id == eventId && it.published && it.type.isCompetition }
            ?: return@guarded Change.Fail("Competition not found.")
        if (e.deadline.isBefore(today())) return@guarded Change.Fail("Registration for this competition has closed.")
        if (!e.sport.equals("Any", ignoreCase = true) && !e.sport.equals(t.sport, ignoreCase = true)) {
            return@guarded Change.Fail("This competition is for ${e.sport}.")
        }
        if (t.entries.any { it.competition == e.title }) return@guarded Change.Fail("You already asked to enter this competition.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(entries = it.entries + CompetitionEntry(e.title, EntryStatus.PENDING)) },
            "Entry requested. A coordinator will confirm it.", "Requested entry for ${t.name} to ${e.title}",
        )
    }

    fun submitChallenge(teamId: String): Outcome = guarded(null) { s ->
        val t = s.ownTeam(teamId) ?: return@guarded Change.Fail(notYours)
        if (t.status != TeamStatus.APPROVED) return@guarded Change.Fail("The team must be approved first.")
        if (t.challenge != ChallengeStatus.NOT_STARTED) return@guarded Change.Fail("The activity is already submitted.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(challenge = ChallengeStatus.SUBMITTED) },
            "Sent to a learning facilitator.", "Submitted Play for Change activity for ${t.name}",
        )
    }

    /** Looks the coach up by email. The reply is the same whether or not an account exists, so emails cannot be probed. */
    fun addCoachByEmail(teamId: String, email: String): Outcome = guarded(null) { s ->
        val t = s.ownTeam(teamId) ?: return@guarded Change.Fail(notYours)
        val wanted = email.trim()
        if (!Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(wanted)) return@guarded Change.Fail("Enter a valid email address.")
        val message = "If that person has an account, they can now help manage players."
        val match = s.profiles.entries.firstOrNull { it.value.email.equals(wanted, ignoreCase = true) }?.key
            ?.takeIf { id -> s.users.any { it.id == id && it.active } && id != t.managerId && id !in t.coachIds }
            ?: return@guarded Change.Ok(s, message)
        Change.Ok(s.withTeam(teamId) { it.copy(coachIds = it.coachIds + match) }, message, "Added a coach to ${t.name}")
    }

    fun removeCoach(teamId: String, userId: String): Outcome = guarded(null) { s ->
        val t = s.ownTeam(teamId) ?: return@guarded Change.Fail(notYours)
        Change.Ok(s.withTeam(teamId) { it.copy(coachIds = it.coachIds - userId) }, "Coach removed.", "Removed a coach from ${t.name}")
    }

    companion object {
        const val MAX_PLAYERS = 25
    }
}
