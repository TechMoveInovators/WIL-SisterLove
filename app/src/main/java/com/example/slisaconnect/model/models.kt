package com.example.slisaconnect.model


import java.time.LocalDate

/** One thing a signed-in user may be allowed to do. Screens and the repository both check these. */
enum class Permission(val label: String) {
    USERS_VIEW("View users"),
    USERS_MANAGE("Change roles"),
    TEAMS_REVIEW("View teams"),
    TEAMS_DECIDE("Decide registrations"),
    EVENTS_MANAGE("Manage events"),
    CHALLENGE_APPROVE("Approve challenges"),
    LEARNING_MANAGE("Manage learning content"),
    SUPPORT_VIEW("Support records"),
    SUPPORT_EDIT("Add support contacts"),
    REFERRALS_VERIFY("Verify referrals"),
    SUPPORT_PUBLISH("Publish resources"),
    AUDIT_VIEW("Activity log"),
}

/**
 * Staff roles open the admin side. Everyone else only sees the user side.
 * Being a team manager is not a role: whoever registers a team manages it.
 */
enum class Role(val label: String, val description: String, val permissions: Set<Permission>) {
    ADMIN(
        "Platform admin",
        "Full access, including role changes and publishing support resources.",
        Permission.values().toSet(),
    ),
    COORDINATOR(
        "Sports coordinator",
        "Reviews and approves teams, manages events. No access to support records.",
        setOf(Permission.TEAMS_REVIEW, Permission.TEAMS_DECIDE, Permission.EVENTS_MANAGE),
    ),
    SUPPORT(
        "Support officer",
        "Adds and verifies support contacts. No access to teams or users.",
        setOf(Permission.SUPPORT_VIEW, Permission.SUPPORT_EDIT, Permission.REFERRALS_VERIFY),
    ),
    FACILITATOR(
        "Learning facilitator",
        "Approves Play for Change activities and publishes learning content. Can view teams but not decide registrations.",
        setOf(Permission.TEAMS_REVIEW, Permission.CHALLENGE_APPROVE, Permission.LEARNING_MANAGE),
    ),
    MEMBER(
        "Member",
        "Standard user. Can register a team, respond to events and volunteer.",
        emptySet(),
    );

    /** Staff can reach the protected admin side. Everyone else cannot. */
    val isStaff: Boolean get() = permissions.isNotEmpty()
}

enum class TeamStatus(val label: String, val message: String) {
    SUBMITTED("Submitted", "Registration received."),
    UNDER_REVIEW("Under review", "An authorised coordinator is checking the details."),
    APPROVED("Approved", "The team is registered."),
    ACTION_REQUIRED("Action required", "The manager must correct missing or invalid details."),
}

enum class ChallengeStatus(val label: String) {
    NOT_STARTED("Not started"),
    SUBMITTED("Awaiting facilitator"),
    APPROVED("Approved"),
}

enum class EntryStatus(val label: String) { PENDING("Pending"), ACCEPTED("Accepted"), DECLINED("Declined") }

/** Roster entries hold only initials and age. No names, ID numbers or contact details. */
data class Player(val initials: String, val age: Int, val guardianConsent: Boolean) {
    val needsConsent: Boolean get() = age < 18
}

data class CompetitionEntry(val competition: String, val status: EntryStatus)

data class Team(
    val id: String,
    val reference: String,
    val name: String,
    val sport: String,
    val ageGroup: String,
    val category: String,
    val province: String,
    val coach: String,
    val managerId: String,
    val status: TeamStatus,
    val managerNote: String,
    val submitted: LocalDate,
    val players: List<Player>,
    val challenge: ChallengeStatus,
    val entries: List<CompetitionEntry>,
    val description: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    /** Extra coaches who may maintain the roster. Only the primary manager can change team details. */
    val coachIds: List<String> = emptyList(),
) {
    private val consentPlayers get() = players.filter { it.needsConsent }
    val consentRequired: Int get() = consentPlayers.size
    val consentReceived: Int get() = consentPlayers.count { it.guardianConsent }
    val allConsentsIn: Boolean get() = consentReceived == consentRequired
    val readyForApproval: Boolean get() = players.isNotEmpty() && allConsentsIn
    val memberIds: List<String> get() = listOf(managerId) + coachIds
}

/** What a manager types into the registration form. No identity documents are collected. */
data class NewTeam(
    val name: String,
    val sport: String,
    val ageGroup: String,
    val category: String,
    val province: String,
    val coach: String,
)

/** A to-do shown on the team dashboard. Built from the team's current state, nothing is stored. */
data class TeamNotice(val title: String, val body: String, val urgent: Boolean = false)

fun Team.checklist(): List<TeamNotice> = buildList {
    when (status) {
        TeamStatus.ACTION_REQUIRED -> add(TeamNotice("Action required", managerNote.ifBlank { status.message }, urgent = true))
        TeamStatus.SUBMITTED -> add(TeamNotice("Submitted", "Registration received. A coordinator will start the review soon."))
        TeamStatus.UNDER_REVIEW -> add(TeamNotice("Under review", status.message))
        TeamStatus.APPROVED -> add(TeamNotice("Team approved", "The team is registered. You can now request competition entries."))
    }
    if (consentRequired > consentReceived) {
        add(TeamNotice("Missing documents", "${consentRequired - consentReceived} guardian consent forms are still outstanding.", urgent = true))
    }
    if (players.isEmpty()) add(TeamNotice("Roster is empty", "Add your players so the team can be approved.", urgent = true))
    entries.forEach { e ->
        when (e.status) {
            EntryStatus.ACCEPTED -> add(TeamNotice("Entry accepted", e.competition))
            EntryStatus.DECLINED -> add(TeamNotice("Entry declined", e.competition, urgent = true))
            EntryStatus.PENDING -> add(TeamNotice("Entry pending", "${e.competition} is waiting for a coordinator."))
        }
    }
    when (challenge) {
        ChallengeStatus.SUBMITTED -> add(TeamNotice("Play for Change", "Your learning activity is waiting for a facilitator."))
        ChallengeStatus.APPROVED -> add(TeamNotice("Play for Change", "Learning activity approved."))
        ChallengeStatus.NOT_STARTED -> Unit
    }
}

data class AppUser(
    val id: String,
    val name: String,
    val role: Role,
    val province: String,
    val active: Boolean,
    val joined: LocalDate,
)

/** Only what the app needs. No ID number, home address or date of birth. */
data class UserProfile(
    val first: String,
    val last: String,
    val email: String,
    val phone: String,
    val province: String,
    val ageGroup: String,
    val notifyApprovals: Boolean = true,
    val notifyReminders: Boolean = true,
    val notifyEvents: Boolean = true,
)

/** What a person types on the Sign Up screen. */
data class SignUpForm(
    val first: String,
    val last: String,
    val ageGroup: String,
    val province: String,
    val phone: String,
    val email: String,
    val password: String,
    val confirm: String,
)

enum class EventType(val label: String) {
    TOURNAMENT("Tournament"),
    TRAINING("Training"),
    SPORTS_DAY("Sports day"),
    PLAY_FOR_CHANGE("Play for Change"),
    LEARNING("Learning"),
    VOLUNTEER("Volunteer"),
    COMMUNITY("Community"),
}

/** Events a team can enter. Training, learning, volunteering and community events are not competitions. */
val EventType.isCompetition: Boolean
    get() = this == EventType.TOURNAMENT || this == EventType.SPORTS_DAY || this == EventType.PLAY_FOR_CHANGE

data class SportsEvent(
    val id: String,
    val title: String,
    val type: EventType,
    val sport: String,
    val date: LocalDate,
    val venue: String,
    /** Last day to enter a team or respond. */
    val deadline: LocalDate,
    val description: String,
    val published: Boolean,
    val createdBy: String,
    /** Ids of people who said they are going or can help. Only the count is shown to staff. */
    val responses: List<String> = emptyList(),
) {
    fun isOpen(today: LocalDate): Boolean = !deadline.isBefore(today)
    fun isUpcoming(today: LocalDate): Boolean = !date.isBefore(today)
}

enum class ResourceKind(val label: String) {
    SAFETY("Safety & support"),
    HEALTH("Health & wellness"),
    RIGHTS("Know your rights"),
}

enum class ResourceStatus(val label: String) { SUBMITTED("Submitted"), VERIFIED("Verified"), PUBLISHED("Published") }

/** A support or referral contact. Only published contacts are visible to users. */
data class SupportResource(
    val id: String,
    val name: String,
    val kind: ResourceKind,
    val contact: String,
    val notes: String,
    val status: ResourceStatus,
    val addedBy: String,
)

enum class LearnTopic(val label: String) {
    SRHR("SRHR"),
    GBVF("GBVF"),
    HIV("HIV"),
    PREP("PrEP"),
    RIGHTS("Your rights"),
}

data class Article(
    val id: String,
    val title: String,
    val topic: LearnTopic,
    val body: String,
    val published: Boolean,
    val author: String,
    val date: LocalDate,
)

enum class NoticeKind(val label: String) {
    EVENT("Events"),
    CONTENT("Learning"),
    TEAM("Team"),
    ENTRY("Competition entry"),
    CHALLENGE("Play for Change"),
    REMINDER("Reminder"),
    ACCOUNT("Account"),
    STAFF("Staff"),
}

/** One message in a person's notification inbox. [link] says where tapping it goes, see [Links]. */
data class Notice(
    val id: String,
    val userId: String,
    val kind: NoticeKind,
    val title: String,
    val body: String,
    val time: Long,
    val read: Boolean,
    val link: String?,
)

object Links {
    fun team(id: String) = "team:$id"
    fun event(id: String) = "event:$id"
    fun article(id: String) = "article:$id"
    const val ADMIN_TEAMS = "admin:teams"
    const val ADMIN_EVENTS = "admin:events"
    const val ADMIN_SUPPORT = "admin:support"
    const val ADMIN_LEARNING = "admin:learning"
}

data class AuditEntry(val time: Long, val actor: String, val message: String)

data class AppState(
    val currentUserId: String?,
    val users: List<AppUser>,
    val profiles: Map<String, UserProfile>,
    val teams: List<Team>,
    val events: List<SportsEvent>,
    val resources: List<SupportResource>,
    val articles: List<Article>,
    val notices: List<Notice>,
    val audit: List<AuditEntry>,
) {
    /** The signed-in person, or null when signed out or suspended. */
    val currentUser: AppUser? get() = users.firstOrNull { it.id == currentUserId && it.active }

    fun can(permission: Permission): Boolean = currentUser?.role?.permissions?.contains(permission) == true

    fun canEditRoster(team: Team): Boolean {
        val id = currentUser?.id ?: return false
        return team.managerId == id || team.coachIds.contains(id)
    }

    val myTeams: List<Team> get() = teams.filter { canEditRoster(it) }

    val pendingReviews: Int
        get() = teams.count { it.status == TeamStatus.SUBMITTED || it.status == TeamStatus.UNDER_REVIEW }

    val myNotices: List<Notice>
        get() = currentUser?.let { u -> notices.filter { it.userId == u.id }.sortedByDescending { it.time } } ?: emptyList()

    val unreadCount: Int get() = myNotices.count { !it.read }

    fun userName(id: String): String = users.firstOrNull { it.id == id }?.name ?: "Unknown"

    /** What the signed-in person may see in events lists: published ones, soonest first. */
    fun upcomingEvents(today: LocalDate): List<SportsEvent> =
        events.filter { it.published && it.isUpcoming(today) }.sortedBy { it.date }

    fun hasResponded(event: SportsEvent): Boolean = currentUserId?.let { event.responses.contains(it) } == true

    /** Staff with the support permission see every contact. Everyone else sees only published ones. */
    fun visibleResources(): List<SupportResource> =
        if (can(Permission.SUPPORT_VIEW)) resources else resources.filter { it.status == ResourceStatus.PUBLISHED }

    fun visibleArticles(): List<Article> =
        if (can(Permission.LEARNING_MANAGE)) articles else articles.filter { it.published }

    companion object {
        fun empty() = AppState(null, emptyList(), emptyMap(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

/** The protected admin screens and the permission each one needs. A null permission means any staff role. */
enum class AdminArea(val permission: Permission?) {
    DASHBOARD(null),
    USERS(Permission.USERS_VIEW),
    TEAMS(Permission.TEAMS_REVIEW),
    EVENTS(Permission.EVENTS_MANAGE),
    SUPPORT(Permission.SUPPORT_VIEW),
    LEARNING(Permission.LEARNING_MANAGE),
}

fun AppState.canOpen(area: AdminArea): Boolean {
    val user = currentUser ?: return false
    return area.permission?.let { can(it) } ?: user.role.isStaff
}

sealed interface Outcome {
    val message: String
    data class Done(override val message: String, val id: String? = null) : Outcome
    data class Failed(override val message: String) : Outcome
}

data class Helpline(val name: String, val number: String, val note: String)

/** Free national helplines shown on Help & Contact. Check these each year. Last checked October 2026. */
object Helplines {
    const val CHECKED = "October 2026"
    val all = listOf(
        Helpline("GBV Command Centre", "0800 428 428", "Free, 24 hours. SMS the word Help to 31531, or dial *120*7867# to ask for a call back."),
        Helpline("Childline South Africa", "0800 055 555", "Free helpline for children and young people."),
        Helpline("Police", "10111", "Emergency police number."),
        Helpline("Emergency from a cell phone", "112", "Works from any cell phone, even without airtime."),
    )
}


