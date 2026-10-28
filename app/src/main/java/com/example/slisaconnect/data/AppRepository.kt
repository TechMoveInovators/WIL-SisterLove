package com.example.slisaconnect.data


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.org.slisa.connect.model.*

/**
 * The app's data, kept on this phone. Every write checks the signed-in person's permission here, not only in the screens,
 * and every change that matters to someone else drops a message into that person's notification inbox.
 *
 * This is the layer a Firebase version replaces. Keep the public functions the same and the screens do not change.
 */
class AppRepository(
    private val store: Store = MemoryStore(),
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val today: () -> LocalDate = { LocalDate.now() },
    private val hasher: PasswordHasher = PasswordHasher(),
    /** Test setups only: the very first account on a fresh install becomes the platform admin. */
    private val firstAccountIsAdmin: Boolean = false,
) {
    private val _state: MutableStateFlow<AppState>
    private var accounts: List<Account>
    private var rememberMe: Boolean

    init {
        val snapshot = store.load()
        val loaded = snapshot?.state ?: AppState.empty()
        rememberMe = snapshot?.rememberMe ?: false
        // Whoever chose not to stay signed in starts the next launch signed out, whichever store is in use.
        _state = MutableStateFlow(if (rememberMe) loaded else loaded.copy(currentUserId = null))
        accounts = snapshot?.accounts ?: emptyList()
    }

    val state: StateFlow<AppState> = _state.asStateFlow()

    /** True when nobody has signed up yet. The sign-up screen uses this to explain that the first account is the admin. */
    val isFreshInstall: Boolean get() = _state.value.users.isEmpty()
    val firstAccountBecomesAdmin: Boolean get() = firstAccountIsAdmin

    var lastSaveError: String? = null
        private set

    // ---- plumbing ------------------------------------------------------------------------------

    private data class Draft(val userIds: List<String>, val kind: NoticeKind, val title: String, val body: String, val link: String?)

    private sealed interface Change {
        data class Ok(
            val state: AppState,
            val message: String,
            val audit: String? = null,
            val id: String? = null,
            val drafts: List<Draft> = emptyList(),
            val accounts: List<Account>? = null,
        ) : Change

        data class Fail(val message: String) : Change
    }

    private fun newId(prefix: String) = prefix + "-" + UUID.randomUUID().toString()

    private fun commit(next: AppState) {
        _state.value = next
        persist()
    }

    private fun persist() {
        try {
            store.save(Snapshot(_state.value, accounts, rememberMe))
            lastSaveError = null
        } catch (e: Exception) {
            lastSaveError = e.message ?: "Could not save."
        }
    }

    /** Keeps at most 100 messages per person, newest first. */
    private fun capNotices(list: List<Notice>): List<Notice> {
        val seen = HashMap<String, Int>()
        return list.filter { n ->
            val count = (seen[n.userId] ?: 0) + 1
            seen[n.userId] = count
            count <= 100
        }
    }

    @Synchronized
    private fun guarded(permission: Permission?, block: (AppState, AppUser) -> Change): Outcome {
        val s = _state.value
        val actor = s.currentUser ?: return Outcome.Failed("Please sign in.")
        if (permission != null && !s.can(permission)) return Outcome.Failed("Your role can't do this.")
        return when (val c = block(s, actor)) {
            is Change.Fail -> Outcome.Failed(c.message)
            is Change.Ok -> {
                val now = clock()
                var next = c.state
                if (c.drafts.isNotEmpty()) {
                    val made = c.drafts.flatMap { d ->
                        d.userIds.map { uid -> Notice(newId("n"), uid, d.kind, d.title, d.body, now, false, d.link) }
                    }
                    next = next.copy(notices = capNotices(made + next.notices))
                }
                if (c.audit != null) {
                    next = next.copy(audit = (listOf(AuditEntry(now, actor.name, c.audit)) + next.audit).take(50))
                }
                c.accounts?.let { accounts = it }
                commit(next)
                Outcome.Done(c.message, c.id)
            }
        }
    }

    private fun AppState.wants(userId: String, kind: NoticeKind): Boolean {
        val p = profiles[userId] ?: return true
        return when (kind) {
            NoticeKind.EVENT, NoticeKind.CONTENT -> p.notifyEvents
            NoticeKind.TEAM, NoticeKind.ENTRY, NoticeKind.CHALLENGE -> p.notifyApprovals
            NoticeKind.REMINDER -> p.notifyReminders
            NoticeKind.ACCOUNT, NoticeKind.STAFF -> true
        }
    }

    /** A message for these people. The sender is left out, suspended accounts are skipped, and notification choices are respected. */
    private fun AppState.draft(kind: NoticeKind, title: String, body: String, link: String?, to: Collection<String>): Draft {
        val me = currentUserId
        val ids = to.distinct().filter { id -> id != me && users.any { it.id == id && it.active } && wants(id, kind) }
        return Draft(ids, kind, title, body, link)
    }

    private fun AppState.staffWith(p: Permission): List<String> =
        users.filter { it.active && it.role.permissions.contains(p) }.map { it.id }

    private fun AppState.withTeam(id: String, f: (Team) -> Team) = copy(teams = teams.map { if (it.id == id) f(it) else it })

    private fun AppState.ownTeam(id: String, userId: String): Team? = teams.firstOrNull { it.id == id && it.managerId == userId }

    private fun AppState.rosterTeam(id: String, userId: String): Team? =
        teams.firstOrNull { it.id == id && (it.managerId == userId || it.coachIds.contains(userId)) }

    private val dayText = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    private fun LocalDate.text(): String = format(dayText)
    private fun people(n: Int) = if (n == 1) "1 person" else "$n people"

    private val notYours = "You don't have access to this team."

    // ---- sign up, sign in, sign out ------------------------------------------------------------

    @Synchronized
    fun signUp(form: SignUpForm): Outcome {
        val errors = Validators.signUp(form)
        if (errors.isNotEmpty()) return Outcome.Failed(errors.values.first())
        val email = form.email.trim().lowercase()
        if (accounts.any { it.email == email }) return Outcome.Failed("An account with this email already exists. Try signing in.")
        val s = _state.value
        val id = newId("u")
        val salt = hasher.newSalt()
        val admin = firstAccountIsAdmin && s.users.isEmpty()
        val first = form.first.trim()
        val last = form.last.trim()
        val user = AppUser(id, "$first $last", if (admin) Role.ADMIN else Role.MEMBER, form.province, true, today())
        val profile = UserProfile(first, last, email, form.phone.trim(), form.province, form.ageGroup)
        val welcome = Notice(
            newId("n"), id, NoticeKind.ACCOUNT, "Welcome to SLiSA Connect",
            if (admin) "You are the platform admin. Use the Admin side to publish events, review teams and share support contacts."
            else "Register a team, respond to events and explore learning content. You'll hear from us here when something changes.",
            clock(), false, null,
        )
        accounts = accounts + Account(id, email, salt, hasher.hash(form.password, salt))
        rememberMe = true
        commit(
            s.copy(
                currentUserId = id,
                users = s.users + user,
                profiles = s.profiles + (id to profile),
                notices = listOf(welcome) + s.notices,
            ),
        )
        return Outcome.Done("Welcome, $first.", id)
    }

    @Synchronized
    fun signIn(email: String, password: String, remember: Boolean): Outcome {
        val acc = accounts.firstOrNull { it.email == email.trim().lowercase() }
        // Hash even when the email is unknown so both cases take the same time.
        val ok = if (acc != null) hasher.matches(password, acc.salt, acc.hash) else { hasher.hash(password, hasher.newSalt()); false }
        if (acc == null || !ok) return Outcome.Failed("Email or password is incorrect.")
        val s = _state.value
        val user = s.users.firstOrNull { it.id == acc.userId } ?: return Outcome.Failed("Email or password is incorrect.")
        if (!user.active) return Outcome.Failed("This account is suspended. Please contact SLiSA.")
        rememberMe = remember
        commit(s.copy(currentUserId = user.id))
        return Outcome.Done("Welcome back, ${user.name.substringBefore(" ")}.", user.id)
    }

    @Synchronized
    fun signOut() {
        rememberMe = false
        commit(_state.value.copy(currentUserId = null))
    }

    @Synchronized
    fun changePassword(current: String, new: String): Outcome {
        val me = _state.value.currentUser ?: return Outcome.Failed("Please sign in.")
        val acc = accounts.firstOrNull { it.userId == me.id } ?: return Outcome.Failed("Account not found.")
        if (!hasher.matches(current, acc.salt, acc.hash)) return Outcome.Failed("Your current password is not correct.")
        Validators.password(new)?.let { return Outcome.Failed(it) }
        if (new == current) return Outcome.Failed("Choose a different password.")
        val salt = hasher.newSalt()
        accounts = accounts.map { if (it.userId == me.id) it.copy(salt = salt, hash = hasher.hash(new, salt)) else it }
        persist()
        return Outcome.Done("Password changed.")
    }

    /** Removes the person's account, profile, notifications and responses. Their name is removed from the activity log. */
    @Synchronized
    fun deleteAccount(password: String): Outcome {
        val s = _state.value
        val me = s.currentUser ?: return Outcome.Failed("Please sign in.")
        val acc = accounts.firstOrNull { it.userId == me.id } ?: return Outcome.Failed("Account not found.")
        if (!hasher.matches(password, acc.salt, acc.hash)) return Outcome.Failed("That password is not correct.")
        if (s.teams.any { it.managerId == me.id }) {
            return Outcome.Failed("You manage a team. Ask SLiSA to transfer or remove it before deleting your account.")
        }
        if (me.role == Role.ADMIN && s.users.count { it.role == Role.ADMIN && it.active } < 2) {
            return Outcome.Failed("You are the only platform admin. Make someone else an admin first.")
        }
        accounts = accounts.filter { it.userId != me.id }
        rememberMe = false
        commit(
            s.copy(
                currentUserId = null,
                users = s.users.filter { it.id != me.id },
                profiles = s.profiles - me.id,
                teams = s.teams.map { it.copy(coachIds = it.coachIds - me.id) },
                events = s.events.map { it.copy(responses = it.responses - me.id) },
                notices = s.notices.filter { it.userId != me.id },
                audit = s.audit.map { if (it.actor == me.name) it.copy(actor = "Deleted user") else it },
            ),
        )
        return Outcome.Done("Your account and personal details were deleted.")
    }

    // ---- my profile and inbox (any signed-in person) -------------------------------------------

    fun updateProfile(profile: UserProfile): Outcome = guarded(null) { s, me ->
        val errors = Validators.profile(profile.first, profile.last, profile.email, profile.phone)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val email = profile.email.trim().lowercase()
        if (accounts.any { it.userId != me.id && it.email == email }) {
            return@guarded Change.Fail("That email is already used by another account.")
        }
        val clean = profile.copy(first = profile.first.trim(), last = profile.last.trim(), email = email, phone = profile.phone.trim())
        Change.Ok(
            s.copy(
                profiles = s.profiles + (me.id to clean),
                users = s.users.map { if (it.id == me.id) it.copy(name = "${clean.first} ${clean.last}", province = clean.province) else it },
            ),
            "Profile saved.",
            accounts = accounts.map { if (it.userId == me.id) it.copy(email = email) else it },
        )
    }

    fun markNoticeRead(id: String): Outcome = guarded(null) { s, me ->
        Change.Ok(s.copy(notices = s.notices.map { if (it.id == id && it.userId == me.id) it.copy(read = true) else it }), "")
    }

    fun markAllNoticesRead(): Outcome = guarded(null) { s, me ->
        Change.Ok(s.copy(notices = s.notices.map { if (it.userId == me.id) it.copy(read = true) else it }), "All caught up.")
    }

    // ---- events: everyone can respond ----------------------------------------------------------

    fun respondToEvent(eventId: String, going: Boolean): Outcome = guarded(null) { s, me ->
        val e = s.events.firstOrNull { it.id == eventId && it.published } ?: return@guarded Change.Fail("Event not found.")
        val already = e.responses.contains(me.id)
        if (going && already) return@guarded Change.Fail("You have already responded.")
        if (going && !e.isOpen(today())) return@guarded Change.Fail("Responses for this event have closed.")
        val updated = if (going) e.responses + me.id else e.responses - me.id
        Change.Ok(
            s.copy(events = s.events.map { if (it.id == eventId) it.copy(responses = updated) else it }),
            if (going) "Thanks. We've noted that you're in." else "Response removed.",
        )
    }

    // ---- teams: managers and coaches -----------------------------------------------------------

    /** Registers a team once. The outcome's id is the new team's id. Registering does not enter any competition. */
    fun registerTeam(draft: NewTeam): Outcome = guarded(null) { s, me ->
        val errors = Validators.team(draft.name, draft.sport, draft.ageGroup, draft.category, draft.province, draft.coach)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val name = draft.name.trim()
        if (s.teams.any { it.managerId == me.id && it.name.equals(name, ignoreCase = true) }) {
            return@guarded Change.Fail("You already registered a team with that name.")
        }
        val next = (s.teams.mapNotNull { it.reference.substringAfterLast('-').toIntOrNull() }.maxOrNull() ?: 0) + 1
        val now = today()
        val ref = String.format(Locale.ROOT, "SLC-T-%02d-%04d", now.year % 100, next)
        val team = Team(
            id = newId("t"), reference = ref, name = name, sport = draft.sport, ageGroup = draft.ageGroup,
            category = draft.category, province = draft.province, coach = draft.coach.trim(), managerId = me.id,
            status = TeamStatus.SUBMITTED, managerNote = "", submitted = now, players = emptyList(),
            challenge = ChallengeStatus.NOT_STARTED, entries = emptyList(),
        )
        Change.Ok(
            s.copy(teams = s.teams + team),
            "Registration received. Reference $ref.",
            "Registered team $name ($ref)",
            team.id,
            listOf(s.draft(NoticeKind.STAFF, "New team registered", "$name ($ref) is waiting for review.", Links.ADMIN_TEAMS, s.staffWith(Permission.TEAMS_DECIDE))),
        )
    }

    fun resubmitTeam(teamId: String): Outcome = guarded(null) { s, me ->
        val t = s.ownTeam(teamId, me.id) ?: return@guarded Change.Fail(notYours)
        if (t.status != TeamStatus.ACTION_REQUIRED) return@guarded Change.Fail("Only teams marked Action required can be resubmitted.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(status = TeamStatus.SUBMITTED, managerNote = "") },
            "Resubmitted. Registration received.",
            "Resubmitted ${t.name}",
            drafts = listOf(s.draft(NoticeKind.STAFF, "Team resubmitted", "${t.name} (${t.reference}) fixed the details and is ready to review again.", Links.ADMIN_TEAMS, s.staffWith(Permission.TEAMS_DECIDE))),
        )
    }

    private fun checkPlayer(p: Player): Pair<Player?, String?> {
        val initials = Validators.initials(p.initials) ?: return null to "Use initials only, like T.M."
        if (p.age !in 5..40) return null to "Enter an age between 5 and 40."
        return p.copy(initials = initials, guardianConsent = p.guardianConsent && p.age < 18) to null
    }

    private fun rosterChange(teamId: String, change: (List<Player>) -> Pair<List<Player>?, String?>, done: String): Outcome =
        guarded(null) { s, me ->
            val t = s.rosterTeam(teamId, me.id) ?: return@guarded Change.Fail(notYours)
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

    fun updateTeamProfile(teamId: String, description: String, email: String, phone: String): Outcome = guarded(null) { s, me ->
        val t = s.ownTeam(teamId, me.id) ?: return@guarded Change.Fail(notYours)
        val errors = Validators.teamContact(description, email, phone)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        Change.Ok(
            s.withTeam(teamId) { it.copy(description = description.trim(), contactEmail = email.trim(), contactPhone = phone.trim()) },
            "Team profile saved.", "Updated team profile for ${t.name}",
        )
    }

    /** Asks to enter an approved team into a competition. A coordinator still has to accept the entry. */
    fun requestEntry(teamId: String, eventId: String): Outcome = guarded(null) { s, me ->
        val t = s.ownTeam(teamId, me.id) ?: return@guarded Change.Fail(notYours)
        if (t.status != TeamStatus.APPROVED) return@guarded Change.Fail("The team must be approved first.")
        val e = s.events.firstOrNull { it.id == eventId && it.published && it.type.isCompetition }
            ?: return@guarded Change.Fail("Competition not found.")
        if (!e.isOpen(today())) return@guarded Change.Fail("Registration for this competition has closed.")
        if (!e.sport.equals("Any", ignoreCase = true) && !e.sport.equals(t.sport, ignoreCase = true)) {
            return@guarded Change.Fail("This competition is for ${e.sport}.")
        }
        if (t.entries.any { it.competition == e.title }) return@guarded Change.Fail("You already asked to enter this competition.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(entries = it.entries + CompetitionEntry(e.title, EntryStatus.PENDING)) },
            "Entry requested. A coordinator will confirm it.",
            "Requested entry for ${t.name} to ${e.title}",
            drafts = listOf(s.draft(NoticeKind.STAFF, "Competition entry requested", "${t.name} wants to enter ${e.title}.", Links.ADMIN_TEAMS, s.staffWith(Permission.TEAMS_DECIDE))),
        )
    }

    fun submitChallenge(teamId: String): Outcome = guarded(null) { s, me ->
        val t = s.ownTeam(teamId, me.id) ?: return@guarded Change.Fail(notYours)
        if (t.status != TeamStatus.APPROVED) return@guarded Change.Fail("The team must be approved first.")
        if (t.challenge != ChallengeStatus.NOT_STARTED) return@guarded Change.Fail("The activity is already submitted.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(challenge = ChallengeStatus.SUBMITTED) },
            "Sent to a learning facilitator.",
            "Submitted Play for Change activity for ${t.name}",
            drafts = listOf(s.draft(NoticeKind.STAFF, "Play for Change activity submitted", "${t.name} is waiting for a facilitator.", Links.ADMIN_TEAMS, s.staffWith(Permission.CHALLENGE_APPROVE))),
        )
    }

    /** Looks the coach up by email. The reply is the same whether or not an account exists, so emails cannot be probed. */
    fun addCoachByEmail(teamId: String, email: String): Outcome = guarded(null) { s, me ->
        val t = s.ownTeam(teamId, me.id) ?: return@guarded Change.Fail(notYours)
        val wanted = email.trim()
        if (!Validators.isEmail(wanted)) return@guarded Change.Fail("Enter a valid email address.")
        val message = "If that person has an account, they can now help manage players."
        val match = s.profiles.entries.firstOrNull { it.value.email.equals(wanted, ignoreCase = true) }?.key
            ?.takeIf { id -> s.users.any { it.id == id && it.active } && id != t.managerId && !t.coachIds.contains(id) }
            ?: return@guarded Change.Ok(s, message)
        Change.Ok(
            s.withTeam(teamId) { it.copy(coachIds = it.coachIds + match) },
            message,
            "Added a coach to ${t.name}",
            drafts = listOf(s.draft(NoticeKind.TEAM, "You were added as a coach", "You can now manage the players of ${t.name}.", Links.team(t.id), listOf(match))),
        )
    }

    fun removeCoach(teamId: String, userId: String): Outcome = guarded(null) { s, me ->
        val t = s.ownTeam(teamId, me.id) ?: return@guarded Change.Fail(notYours)
        Change.Ok(s.withTeam(teamId) { it.copy(coachIds = it.coachIds - userId) }, "Coach removed.", "Removed a coach from ${t.name}")
    }

    // ---- teams: staff ---------------------------------------------------------------------------

    fun startReview(teamId: String): Outcome = guarded(Permission.TEAMS_DECIDE) { s, _ ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.SUBMITTED) return@guarded Change.Fail("Only submitted teams can be reviewed.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(status = TeamStatus.UNDER_REVIEW) },
            "Review started.",
            "Started review of ${t.name}",
            drafts = listOf(s.draft(NoticeKind.TEAM, "Your team is under review", "${t.name}: an authorised coordinator is checking the details.", Links.team(t.id), t.memberIds)),
        )
    }

    /** Approving a team registers it. It does not enter the team into any competition. */
    fun approveTeam(teamId: String): Outcome = guarded(Permission.TEAMS_DECIDE) { s, _ ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.UNDER_REVIEW) return@guarded Change.Fail("Start a review first.")
        if (t.players.isEmpty()) return@guarded Change.Fail("The roster is empty.")
        if (!t.allConsentsIn) return@guarded Change.Fail("Guardian consent is incomplete.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(status = TeamStatus.APPROVED, managerNote = "") },
            "Team approved. Competition entries are still decided separately.",
            "Approved ${t.name}",
            drafts = listOf(s.draft(NoticeKind.TEAM, "Team approved", "${t.name} is registered. You can now request competition entries.", Links.team(t.id), t.memberIds)),
        )
    }

    fun requestChanges(teamId: String, note: String): Outcome = guarded(Permission.TEAMS_DECIDE) { s, _ ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.UNDER_REVIEW) return@guarded Change.Fail("Start a review first.")
        if (note.isBlank()) return@guarded Change.Fail("Add a message so the manager knows what to fix.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(status = TeamStatus.ACTION_REQUIRED, managerNote = note.trim()) },
            "Message sent to the team manager.",
            "Asked ${t.name} to correct details",
            drafts = listOf(s.draft(NoticeKind.TEAM, "Action required for ${t.name}", note.trim(), Links.team(t.id), t.memberIds)),
        )
    }

    fun sendConsentReminder(teamId: String): Outcome = guarded(Permission.TEAMS_DECIDE) { s, _ ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        val missing = t.consentRequired - t.consentReceived
        if (missing <= 0) return@guarded Change.Fail("No guardian consent forms are missing.")
        Change.Ok(
            s,
            "Reminder sent to the team.",
            "Sent a consent reminder to ${t.name}",
            drafts = listOf(s.draft(NoticeKind.REMINDER, "Guardian consent forms missing", "${t.name}: $missing guardian consent forms are still outstanding.", Links.team(t.id), t.memberIds)),
        )
    }

    fun approveChallenge(teamId: String): Outcome = guarded(Permission.CHALLENGE_APPROVE) { s, _ ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.challenge != ChallengeStatus.SUBMITTED) return@guarded Change.Fail("No activity is waiting for approval.")
        Change.Ok(
            s.withTeam(teamId) { it.copy(challenge = ChallengeStatus.APPROVED) },
            "Challenge approved.",
            "Approved Play for Change activity for ${t.name}",
            drafts = listOf(s.draft(NoticeKind.CHALLENGE, "Play for Change approved", "${t.name}'s learning activity was approved.", Links.team(t.id), t.memberIds)),
        )
    }

    fun decideEntry(teamId: String, index: Int, decision: EntryStatus): Outcome = guarded(Permission.TEAMS_DECIDE) { s, _ ->
        val t = s.teams.firstOrNull { it.id == teamId } ?: return@guarded Change.Fail("Team not found.")
        if (t.status != TeamStatus.APPROVED) return@guarded Change.Fail("The team must be approved first.")
        val entry = t.entries.getOrNull(index) ?: return@guarded Change.Fail("Entry not found.")
        if (entry.status != EntryStatus.PENDING || decision == EntryStatus.PENDING) return@guarded Change.Fail("Entry already decided.")
        val word = decision.label.lowercase()
        Change.Ok(
            s.withTeam(teamId) { team ->
                team.copy(entries = team.entries.mapIndexed { i, e -> if (i == index) e.copy(status = decision) else e })
            },
            "Entry $word.",
            "${decision.label} ${t.name} for ${entry.competition}",
            drafts = listOf(s.draft(NoticeKind.ENTRY, "Entry $word", "${t.name} was $word for ${entry.competition}.", Links.team(t.id), t.memberIds)),
        )
    }

    // ---- users ---------------------------------------------------------------------------------

    fun setRole(userId: String, role: Role): Outcome = guarded(Permission.USERS_MANAGE) { s, me ->
        val u = s.users.firstOrNull { it.id == userId } ?: return@guarded Change.Fail("User not found.")
        if (u.id == me.id) return@guarded Change.Fail("You cannot change your own role.")
        if (u.role == role) return@guarded Change.Fail("That is already their role.")
        val admins = s.users.count { it.role == Role.ADMIN && it.active }
        if (u.role == Role.ADMIN && admins < 2) return@guarded Change.Fail("Keep at least one active platform admin.")
        Change.Ok(
            s.copy(users = s.users.map { if (it.id == userId) it.copy(role = role) else it }),
            "Role updated.",
            "Changed ${u.name} from ${u.role.label} to ${role.label}",
            drafts = listOf(s.draft(NoticeKind.ACCOUNT, "Your role changed", "You are now: ${role.label}.", null, listOf(userId))),
        )
    }

    fun setActive(userId: String, active: Boolean): Outcome = guarded(Permission.USERS_MANAGE) { s, me ->
        val u = s.users.firstOrNull { it.id == userId } ?: return@guarded Change.Fail("User not found.")
        if (u.id == me.id) return@guarded Change.Fail("You cannot suspend yourself.")
        Change.Ok(
            s.copy(users = s.users.map { if (it.id == userId) it.copy(active = active) else it }),
            if (active) "Account reactivated." else "Account suspended.",
            "${if (active) "Reactivated" else "Suspended"} ${u.name}",
        )
    }

    // ---- events: staff -------------------------------------------------------------------------

    /** Creates the event when [event].id is blank, otherwise updates it. Publishing tells every user who wants event updates. */
    fun saveEvent(event: SportsEvent): Outcome = guarded(Permission.EVENTS_MANAGE) { s, me ->
        val errors = Validators.event(event.title, event.venue, event.description, event.date, event.deadline)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val clean = event.copy(title = event.title.trim(), venue = event.venue.trim(), description = event.description.trim())
        val old = s.events.firstOrNull { it.id == event.id }
        if (event.id.isNotBlank() && old == null) return@guarded Change.Fail("Event not found.")
        if (old == null && clean.date.isBefore(today())) return@guarded Change.Fail("Choose an event date that isn't in the past.")
        val saved = if (old == null) clean.copy(id = newId("e"), createdBy = me.id, responses = emptyList())
        else clean.copy(createdBy = old.createdBy, responses = old.responses)
        val events = if (old == null) s.events + saved else s.events.map { if (it.id == saved.id) saved else it }

        val becamePublic = saved.published && (old == null || !old.published)
        val changedDetails = old != null && old.published && saved.published &&
                (old.date != saved.date || old.venue != saved.venue || old.title != saved.title)
        val drafts = when {
            becamePublic -> listOf(
                s.draft(NoticeKind.EVENT, "New event: ${saved.title}", "${saved.date.text()} · ${saved.venue}", Links.event(saved.id), s.users.map { it.id }),
            )
            changedDetails -> listOf(
                s.draft(NoticeKind.EVENT, "Event updated: ${saved.title}", "${saved.date.text()} · ${saved.venue}", Links.event(saved.id), saved.responses),
            )
            else -> emptyList()
        }
        val told = drafts.sumOf { it.userIds.size }
        Change.Ok(
            s.copy(events = events),
            if (becamePublic) "Event published. ${people(told)} notified." else "Event saved.",
            "${if (old == null) "Created" else "Edited"} ${saved.title}",
            saved.id,
            drafts,
        )
    }

    fun setEventPublished(id: String, published: Boolean): Outcome = guarded(Permission.EVENTS_MANAGE) { s, _ ->
        val e = s.events.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Event not found.")
        if (e.published == published) return@guarded Change.Fail(if (published) "Already published." else "Already a draft.")
        val drafts = if (published) listOf(
            s.draft(NoticeKind.EVENT, "New event: ${e.title}", "${e.date.text()} · ${e.venue}", Links.event(e.id), s.users.map { it.id }),
        ) else emptyList()
        val told = drafts.sumOf { it.userIds.size }
        Change.Ok(
            s.copy(events = s.events.map { if (it.id == id) it.copy(published = published) else it }),
            if (published) "Event published. ${people(told)} notified." else "Event moved to drafts.",
            "${if (published) "Published" else "Unpublished"} ${e.title}",
            drafts = drafts,
        )
    }

    fun deleteEvent(id: String): Outcome = guarded(Permission.EVENTS_MANAGE) { s, _ ->
        val e = s.events.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Event not found.")
        val drafts = if (e.published && e.isUpcoming(today())) listOf(
            s.draft(NoticeKind.EVENT, "Event cancelled: ${e.title}", "${e.date.text()} · ${e.venue} will not go ahead.", null, e.responses),
        ) else emptyList()
        Change.Ok(s.copy(events = s.events.filter { it.id != id }), "Event deleted.", "Deleted ${e.title}", drafts = drafts)
    }

    // ---- learning content ----------------------------------------------------------------------

    /** Creates the article when [article].id is blank, otherwise updates it. */
    fun saveArticle(article: Article): Outcome = guarded(Permission.LEARNING_MANAGE) { s, me ->
        val errors = Validators.article(article.title, article.body)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val clean = article.copy(title = article.title.trim(), body = article.body.trim())
        val old = s.articles.firstOrNull { it.id == article.id }
        if (article.id.isNotBlank() && old == null) return@guarded Change.Fail("Article not found.")
        val saved = if (old == null) clean.copy(id = newId("a"), author = me.name, date = today()) else clean.copy(author = old.author, date = old.date)
        val articles = if (old == null) s.articles + saved else s.articles.map { if (it.id == saved.id) saved else it }
        val becamePublic = saved.published && (old == null || !old.published)
        val drafts = if (becamePublic) listOf(
            s.draft(NoticeKind.CONTENT, "New to read: ${saved.title}", saved.topic.label, Links.article(saved.id), s.users.map { it.id }),
        ) else emptyList()
        val told = drafts.sumOf { it.userIds.size }
        Change.Ok(
            s.copy(articles = articles),
            if (becamePublic) "Article published. ${people(told)} notified." else "Article saved.",
            "${if (old == null) "Created" else "Edited"} article ${saved.title}",
            saved.id,
            drafts,
        )
    }

    fun setArticlePublished(id: String, published: Boolean): Outcome = guarded(Permission.LEARNING_MANAGE) { s, _ ->
        val a = s.articles.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Article not found.")
        if (a.published == published) return@guarded Change.Fail(if (published) "Already published." else "Already a draft.")
        val drafts = if (published) listOf(
            s.draft(NoticeKind.CONTENT, "New to read: ${a.title}", a.topic.label, Links.article(a.id), s.users.map { it.id }),
        ) else emptyList()
        val told = drafts.sumOf { it.userIds.size }
        Change.Ok(
            s.copy(articles = s.articles.map { if (it.id == id) it.copy(published = published) else it }),
            if (published) "Article published. ${people(told)} notified." else "Article moved to drafts.",
            "${if (published) "Published" else "Unpublished"} article ${a.title}",
            drafts = drafts,
        )
    }

    fun deleteArticle(id: String): Outcome = guarded(Permission.LEARNING_MANAGE) { s, _ ->
        val a = s.articles.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Article not found.")
        Change.Ok(s.copy(articles = s.articles.filter { it.id != id }), "Article deleted.", "Deleted article ${a.title}")
    }

    // ---- support resources (private until published) -------------------------------------------

    fun addResource(name: String, kind: ResourceKind, contact: String, notes: String): Outcome = guarded(Permission.SUPPORT_EDIT) { s, me ->
        val errors = Validators.resource(name, contact)
        if (errors.isNotEmpty()) return@guarded Change.Fail(errors.values.first())
        val r = SupportResource(newId("r"), name.trim(), kind, contact.trim(), notes.trim(), ResourceStatus.SUBMITTED, me.name)
        Change.Ok(s.copy(resources = s.resources + r), "Added. It stays private until it is verified and published.", "Added support contact ${r.name}", r.id)
    }

    fun verifyResource(id: String): Outcome = guarded(Permission.REFERRALS_VERIFY) { s, _ ->
        val r = s.resources.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Resource not found.")
        if (r.status != ResourceStatus.SUBMITTED) return@guarded Change.Fail("Already verified.")
        Change.Ok(
            s.copy(resources = s.resources.map { if (it.id == id) it.copy(status = ResourceStatus.VERIFIED) else it }),
            "Contact verified. An admin can now publish it.",
            "Verified contact for ${r.name}",
            drafts = listOf(s.draft(NoticeKind.STAFF, "Contact ready to publish", "${r.name} was verified.", Links.ADMIN_SUPPORT, s.staffWith(Permission.SUPPORT_PUBLISH))),
        )
    }

    fun publishResource(id: String): Outcome = guarded(Permission.SUPPORT_PUBLISH) { s, _ ->
        val r = s.resources.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Resource not found.")
        if (r.status != ResourceStatus.VERIFIED) return@guarded Change.Fail("Only verified contacts can be published.")
        Change.Ok(
            s.copy(resources = s.resources.map { if (it.id == id) it.copy(status = ResourceStatus.PUBLISHED) else it }),
            "Published. Users can now see it.",
            "Published ${r.name}",
        )
    }

    fun unpublishResource(id: String): Outcome = guarded(Permission.SUPPORT_PUBLISH) { s, _ ->
        val r = s.resources.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Resource not found.")
        if (r.status != ResourceStatus.PUBLISHED) return@guarded Change.Fail("It is not published.")
        Change.Ok(
            s.copy(resources = s.resources.map { if (it.id == id) it.copy(status = ResourceStatus.VERIFIED) else it }),
            "Hidden from users.",
            "Unpublished ${r.name}",
        )
    }

    fun deleteResource(id: String): Outcome = guarded(Permission.SUPPORT_PUBLISH) { s, _ ->
        val r = s.resources.firstOrNull { it.id == id } ?: return@guarded Change.Fail("Resource not found.")
        Change.Ok(s.copy(resources = s.resources.filter { it.id != id }), "Contact deleted.", "Deleted support contact ${r.name}")
    }

    // ---- testing aid (the screens only offer this in debug builds) -----------------------------

    /** Adds a few clearly marked sample events, articles and helplines so every screen has something to show. */
    fun addSampleContent(): Outcome = guarded(Permission.USERS_MANAGE) { s, me ->
        if (s.events.any { it.description.startsWith(SAMPLE_MARK) }) return@guarded Change.Fail("Sample content is already loaded.")
        val t = today()
        fun ev(title: String, type: EventType, sport: String, inDays: Long, venue: String, text: String) = SportsEvent(
            newId("e"), title, type, sport, t.plusDays(inDays), venue, t.plusDays(inDays - 5), "$SAMPLE_MARK$text", true, me.id,
        )
        val events = listOf(
            ev("Youth Cup Gauteng", EventType.TOURNAMENT, "Soccer", 24, "Orlando Stadium Grounds, Soweto", "A one-day under-17 soccer tournament."),
            ev("Play for Change Netball Day", EventType.PLAY_FOR_CHANGE, "Netball", 31, "Alexandra Sports Complex", "Netball matches with a short learning activity on preventing gender-based violence."),
            ev("Coaches safeguarding workshop", EventType.TRAINING, "Any", 9, "SLiSA Hub, Johannesburg", "A workshop for coaches and team managers."),
            ev("Pride Event", EventType.VOLUNTEER, "Any", 18, "Sandton, JHB", "We need volunteers for registration and information tables."),
            ev("Sports Youth Fight against GBV", EventType.VOLUNTEER, "Any", 27, "N-Section, KwaMashu, DBN", "Help run games and conversations with young people."),
            ev("Women's community gathering", EventType.COMMUNITY, "Any", 15, "Community hall, Soweto", "A safe space to talk about health and rights."),
        )
        val articles = listOf(
            Article(newId("a"), "What is SRHR?", LearnTopic.SRHR,
                "$SAMPLE_MARK SRHR stands for sexual and reproductive health and rights. It means everyone can make their own decisions about their body, relationships and health, and can get the information and services they need to do so safely.",
                true, me.name, t),
            Article(newId("a"), "Understanding gender-based violence", LearnTopic.GBVF,
                "$SAMPLE_MARK Gender-based violence is harm done to someone because of their gender. It can be physical, sexual, emotional or economic, and it is never the survivor's fault. If you or someone you know needs help, the GBV Command Centre is free and open 24 hours on 0800 428 428.",
                true, me.name, t),
            Article(newId("a"), "PrEP and HIV prevention", LearnTopic.PREP,
                "$SAMPLE_MARK PrEP is a daily medicine that helps prevent HIV in people who do not have HIV. Ask a nurse at your local clinic whether it is right for you.",
                true, me.name, t),
        )
        val resources = listOf(
            SupportResource(newId("r"), "GBV Command Centre", ResourceKind.SAFETY, "0800 428 428", "Free, 24 hours. SMS the word Help to 31531.", ResourceStatus.PUBLISHED, "Sample content"),
            SupportResource(newId("r"), "Childline South Africa", ResourceKind.SAFETY, "0800 055 555", "Free helpline for children and young people.", ResourceStatus.PUBLISHED, "Sample content"),
        )
        Change.Ok(
            s.copy(events = s.events + events, articles = s.articles + articles, resources = s.resources + resources),
            "Sample content added.",
            "Loaded sample content",
        )
    }

    companion object {
        const val MAX_PLAYERS = 25
        const val SAMPLE_MARK = "Sample: "
    }
}
