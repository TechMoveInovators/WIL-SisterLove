package com.example.slisaconnect.data


import java.io.File
import java.time.LocalDate
import com.example.slisaconnect.model.*

/** Everything that is saved on the phone: the app's data, the sign-ins, and whether to stay signed in. */
data class Snapshot(val state: AppState, val accounts: List<Account>, val rememberMe: Boolean)

interface Store {
    fun load(): Snapshot?
    fun save(snapshot: Snapshot)
}

class MemoryStore : Store {
    var last: Snapshot? = null
    override fun load(): Snapshot? = last
    override fun save(snapshot: Snapshot) { last = snapshot }
}

/**
 * Keeps one JSON file in the app's private storage. It is written to a temporary file first and then renamed,
 * so a crash never leaves a half-written file. An unreadable file is kept aside as ".damaged" instead of being erased.
 */
class FileStore(private val file: File) : Store {
    override fun load(): Snapshot? {
        if (!file.exists()) return null
        return try {
            SnapshotCodec.decode(file.readText())
        } catch (e: Exception) {
            file.renameTo(File(file.parentFile, file.name + ".damaged"))
            null
        }
    }

    override fun save(snapshot: Snapshot) {
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(SnapshotCodec.encode(snapshot))
        if (!tmp.renameTo(file)) {
            file.delete()
            check(tmp.renameTo(file)) { "Could not save the data file." }
        }
    }
}

object SnapshotCodec {
    private const val VERSION = 1

    fun encode(s: Snapshot): String {
        val st = s.state
        return Json.write(
            mapOf(
                "version" to VERSION,
                "session" to mapOf("userId" to st.currentUserId, "remember" to s.rememberMe),
                "accounts" to s.accounts.map { mapOf("userId" to it.userId, "email" to it.email, "salt" to it.salt, "hash" to it.hash) },
                "users" to st.users.map { u ->
                    mapOf("id" to u.id, "name" to u.name, "role" to u.role.name, "province" to u.province, "active" to u.active, "joined" to u.joined.toString())
                },
                "profiles" to st.profiles.map { (id, p) ->
                    mapOf(
                        "id" to id, "first" to p.first, "last" to p.last, "email" to p.email, "phone" to p.phone,
                        "province" to p.province, "ageGroup" to p.ageGroup,
                        "notifyApprovals" to p.notifyApprovals, "notifyReminders" to p.notifyReminders, "notifyEvents" to p.notifyEvents,
                    )
                },
                "teams" to st.teams.map { t ->
                    mapOf(
                        "id" to t.id, "reference" to t.reference, "name" to t.name, "sport" to t.sport, "ageGroup" to t.ageGroup,
                        "category" to t.category, "province" to t.province, "coach" to t.coach, "managerId" to t.managerId,
                        "status" to t.status.name, "managerNote" to t.managerNote, "submitted" to t.submitted.toString(),
                        "players" to t.players.map { mapOf("initials" to it.initials, "age" to it.age, "consent" to it.guardianConsent) },
                        "challenge" to t.challenge.name,
                        "entries" to t.entries.map { mapOf("competition" to it.competition, "status" to it.status.name) },
                        "description" to t.description, "contactEmail" to t.contactEmail, "contactPhone" to t.contactPhone,
                        "coachIds" to t.coachIds,
                    )
                },
                "events" to st.events.map { e ->
                    mapOf(
                        "id" to e.id, "title" to e.title, "type" to e.type.name, "sport" to e.sport, "date" to e.date.toString(),
                        "venue" to e.venue, "deadline" to e.deadline.toString(), "description" to e.description,
                        "published" to e.published, "createdBy" to e.createdBy, "responses" to e.responses,
                    )
                },
                "resources" to st.resources.map { r ->
                    mapOf(
                        "id" to r.id, "name" to r.name, "kind" to r.kind.name, "contact" to r.contact,
                        "notes" to r.notes, "status" to r.status.name, "addedBy" to r.addedBy,
                    )
                },
                "articles" to st.articles.map { a ->
                    mapOf(
                        "id" to a.id, "title" to a.title, "topic" to a.topic.name, "body" to a.body,
                        "published" to a.published, "author" to a.author, "date" to a.date.toString(),
                    )
                },
                "notices" to st.notices.map { n ->
                    mapOf(
                        "id" to n.id, "userId" to n.userId, "kind" to n.kind.name, "title" to n.title, "body" to n.body,
                        "time" to n.time, "read" to n.read, "link" to n.link,
                    )
                },
                "audit" to st.audit.map { mapOf("time" to it.time, "actor" to it.actor, "message" to it.message) },
            ),
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun decode(text: String): Snapshot {
        val root = Json.parse(text) as? Map<String, Any?> ?: throw IllegalArgumentException("Not a data file.")
        val session = root.obj("session")
        val remember = session.b("remember")
        val users = root.list("users").map {
            AppUser(it.s("id"), it.s("name"), it.enum("role", Role.MEMBER), it.s("province"), it.b("active", true), it.date("joined"))
        }
        val savedUser = session.sn("userId")
        val state = AppState(
            currentUserId = if (remember && users.any { it.id == savedUser }) savedUser else null,
            users = users,
            profiles = root.list("profiles").associate {
                it.s("id") to UserProfile(
                    it.s("first"), it.s("last"), it.s("email"), it.s("phone"), it.s("province"), it.s("ageGroup"),
                    it.b("notifyApprovals", true), it.b("notifyReminders", true), it.b("notifyEvents", true),
                )
            },
            teams = root.list("teams").map { t ->
                Team(
                    id = t.s("id"), reference = t.s("reference"), name = t.s("name"), sport = t.s("sport"), ageGroup = t.s("ageGroup"),
                    category = t.s("category"), province = t.s("province"), coach = t.s("coach"), managerId = t.s("managerId"),
                    status = t.enum("status", TeamStatus.SUBMITTED), managerNote = t.s("managerNote"), submitted = t.date("submitted"),
                    players = t.list("players").map { Player(it.s("initials"), it.i("age"), it.b("consent")) },
                    challenge = t.enum("challenge", ChallengeStatus.NOT_STARTED),
                    entries = t.list("entries").map { CompetitionEntry(it.s("competition"), it.enum("status", EntryStatus.PENDING)) },
                    description = t.s("description"), contactEmail = t.s("contactEmail"), contactPhone = t.s("contactPhone"),
                    coachIds = t.strings("coachIds"),
                )
            },
            events = root.list("events").map { e ->
                SportsEvent(
                    id = e.s("id"), title = e.s("title"), type = e.enum("type", EventType.COMMUNITY), sport = e.s("sport", "Any"),
                    date = e.date("date"), venue = e.s("venue"), deadline = e.date("deadline"), description = e.s("description"),
                    published = e.b("published"), createdBy = e.s("createdBy"), responses = e.strings("responses"),
                )
            },
            resources = root.list("resources").map {
                SupportResource(
                    it.s("id"), it.s("name"), it.enum("kind", ResourceKind.SAFETY), it.s("contact"), it.s("notes"),
                    it.enum("status", ResourceStatus.SUBMITTED), it.s("addedBy"),
                )
            },
            articles = root.list("articles").map {
                Article(it.s("id"), it.s("title"), it.enum("topic", LearnTopic.SRHR), it.s("body"), it.b("published"), it.s("author"), it.date("date"))
            },
            notices = root.list("notices").map {
                Notice(
                    it.s("id"), it.s("userId"), it.enum("kind", NoticeKind.ACCOUNT), it.s("title"), it.s("body"),
                    it.l("time"), it.b("read"), it.sn("link"),
                )
            },
            audit = root.list("audit").map { AuditEntry(it.l("time"), it.s("actor"), it.s("message")) },
        )
        val accounts = root.list("accounts").map { Account(it.s("userId"), it.s("email").trim().lowercase(), it.s("salt"), it.s("hash")) }
        return Snapshot(state, accounts, remember)
    }
}

// ---- small readers for the parsed JSON ----

private typealias JMap = Map<String, Any?>

private fun JMap.s(k: String, d: String = ""): String = (this[k] as? String) ?: d
private fun JMap.sn(k: String): String? = this[k] as? String
private fun JMap.i(k: String, d: Int = 0): Int = (this[k] as? Number)?.toInt() ?: d
private fun JMap.l(k: String, d: Long = 0L): Long = (this[k] as? Number)?.toLong() ?: d
private fun JMap.b(k: String, d: Boolean = false): Boolean = (this[k] as? Boolean) ?: d

@Suppress("UNCHECKED_CAST")
private fun JMap.obj(k: String): JMap = (this[k] as? Map<String, Any?>) ?: emptyMap()

@Suppress("UNCHECKED_CAST")
private fun JMap.list(k: String): List<JMap> = (this[k] as? List<*>)?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()

private fun JMap.strings(k: String): List<String> = (this[k] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

private fun JMap.date(k: String): LocalDate =
    try { LocalDate.parse(s(k)) } catch (e: Exception) { LocalDate.of(2000, 1, 1) }

private inline fun <reified E : Enum<E>> JMap.enum(k: String, d: E): E =
    enumValues<E>().firstOrNull { it.name == s(k) } ?: d
