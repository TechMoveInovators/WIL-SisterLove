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
    SUPPORT_VIEW("Support records"),
    REFERRALS_VERIFY("Verify referrals"),
    SUPPORT_PUBLISH("Publish resources"),
    AUDIT_VIEW("Activity log"),
}

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
        "Verifies support and referral contacts. No access to teams or users.",
        setOf(Permission.SUPPORT_VIEW, Permission.REFERRALS_VERIFY),
    ),
    FACILITATOR(
        "Learning facilitator",
        "Approves Play for Change activities. Can view teams but not decide registrations.",
        setOf(Permission.TEAMS_REVIEW, Permission.CHALLENGE_APPROVE),
    ),
    MANAGER("Team manager", "Manages their own team. No admin access.", emptySet()),
    MEMBER("Member", "Standard signed-in user. No admin access.", emptySet());

    /** Staff can reach the protected admin area. Everyone else cannot. */
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
}

/** What a manager types into the registration form. No identity documents are collected. */
data class NewTeam(val name: String, val sport: String, val ageGroup: String, val coach: String)

/** A message shown on the team dashboard. Built from the team's current state, nothing is stored. */
data class TeamNotice(val title: String, val body: String, val urgent: Boolean = false)

fun Team.notices(): List<TeamNotice> = buildList {
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
    val lastActive: String,
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

enum class EventType(val label: String) {
    TOURNAMENT("Tournament"), TRAINING("Training"), SPORTS_DAY("Sports day"),
    PLAY_FOR_CHANGE("Play for Change"), LEARNING("Learning"),
}

/** Events a team can enter. Training and learning sessions are not competitions. */
val EventType.isCompetition: Boolean
    get() = this == EventType.TOURNAMENT || this == EventType.SPORTS_DAY || this == EventType.PLAY_FOR_CHANGE

data class SportsEvent(
    val id: String,
    val title: String,
    val type: EventType,
    val sport: String,
    val date: LocalDate,
    val venue: String,
    val deadline: LocalDate,
    val published: Boolean,
)

enum class ResourceStatus(val label: String) { SUBMITTED("Submitted"), VERIFIED("Verified"), PUBLISHED("Published") }

/** A support or referral contact. The real number never leaves the secure store; only a masked form is shown. */
data class SupportResource(
    val id: String,
    val name: String,
    val kind: String,
    val maskedContact: String,
    val status: ResourceStatus,
    val source: String,
)

data class AuditEntry(val time: String, val actor: String, val message: String)

data class AppState(
    val currentUserId: String,
    val users: List<AppUser>,
    val profiles: Map<String, UserProfile>,
    val teams: List<Team>,
    val events: List<SportsEvent>,
    val resources: List<SupportResource>,
    val audit: List<AuditEntry>,
) {
    val currentUser: AppUser get() = users.first { it.id == currentUserId }
    fun can(permission: Permission): Boolean = permission in currentUser.role.permissions
    fun canEditRoster(team: Team): Boolean = team.managerId == currentUserId || currentUserId in team.coachIds
    val myTeams: List<Team> get() = teams.filter { canEditRoster(it) }
    val pendingReviews: Int
        get() = teams.count { it.status == TeamStatus.SUBMITTED || it.status == TeamStatus.UNDER_REVIEW }
}

/** The protected admin screens and the permission each one needs. A null permission means any staff role. */
enum class AdminArea(val permission: Permission?) {
    DASHBOARD(null),
    USERS(Permission.USERS_VIEW),
    TEAMS(Permission.TEAMS_REVIEW),
    EVENTS(Permission.EVENTS_MANAGE),
}

fun AppState.canOpen(area: AdminArea): Boolean =
    area.permission?.let { can(it) } ?: currentUser.role.isStaff

sealed interface Outcome {
    val message: String
    data class Done(override val message: String, val id: String? = null) : Outcome
    data class Failed(override val message: String) : Outcome
}
