package com.example.slisaconnect.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.slisaconnect.data.AppRepository
import com.example.slisaconnect.data.FileStore
import com.example.slisaconnect.model.*

/**
 * One instance shared by every screen. It owns the saved data and hands every action to the repository,
 * which checks the signed-in person's permission again before changing anything.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(
        store = FileStore(File(app.filesDir, "slisa-connect.json")),
        // On-device data only: the first person to create an account on a fresh install becomes the platform admin.
        // Turn this off when accounts move online (see README, "Going online").
        firstAccountIsAdmin = true,
    )

    val state: StateFlow<AppState> = repo.state

    /** True until the first account exists. The sign-up screen uses it to explain who becomes the admin. */
    val isFreshInstall: Boolean get() = repo.isFreshInstall
    val firstAccountBecomesAdmin: Boolean get() = repo.firstAccountBecomesAdmin

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun today(): LocalDate = LocalDate.now()

    fun toast(text: String) { _messages.tryEmit(text) }

    /** Shows the outcome's message (when there is one) and warns if the phone could not save. */
    private fun Outcome.report(): Outcome {
        if (message.isNotBlank()) _messages.tryEmit(message)
        repo.lastSaveError?.let { _messages.tryEmit("Warning: your changes could not be saved on this phone.") }
        return this
    }

    // ---- sign in and out. Password checks are slow on purpose, so they run off the main thread. ----

    private fun background(work: () -> Outcome, done: (Outcome) -> Unit) {
        viewModelScope.launch {
            val out = try {
                withContext(Dispatchers.Default) { work() }
            } catch (e: Exception) {
                Outcome.Failed(e.message ?: "An unexpected error occurred. Please try again.")
            }
            done(out)
        }
    }

    fun signUp(form: SignUpForm, done: (Outcome) -> Unit) = background({ repo.signUp(form) }, done)
    fun signIn(email: String, password: String, remember: Boolean, done: (Outcome) -> Unit) =
        background({ repo.signIn(email, password, remember) }, done)

    fun signOut() = repo.signOut()

    fun changePassword(current: String, new: String, done: (Outcome) -> Unit) =
        background({ repo.changePassword(current, new) }, { out -> out.report(); done(out) })

    fun deleteAccount(password: String, done: (Outcome) -> Unit) =
        background({ repo.deleteAccount(password) }, { out -> out.report(); done(out) })

    // ---- everyone ----

    fun updateProfile(profile: UserProfile) = repo.updateProfile(profile).report()
    fun markNoticeRead(id: String) = repo.markNoticeRead(id)
    fun markAllNoticesRead() = repo.markAllNoticesRead().report()
    fun respondToEvent(eventId: String, going: Boolean) = repo.respondToEvent(eventId, going).report()

    // ---- team managers and coaches ----

    fun registerTeam(draft: NewTeam) = repo.registerTeam(draft).report()
    fun resubmitTeam(teamId: String) = repo.resubmitTeam(teamId).report()
    fun addPlayer(teamId: String, player: Player) = repo.addPlayer(teamId, player).report()
    fun updatePlayer(teamId: String, index: Int, player: Player) = repo.updatePlayer(teamId, index, player).report()
    fun removePlayer(teamId: String, index: Int) = repo.removePlayer(teamId, index).report()
    fun updateTeamProfile(teamId: String, description: String, email: String, phone: String) =
        repo.updateTeamProfile(teamId, description, email, phone).report()
    fun requestEntry(teamId: String, eventId: String) = repo.requestEntry(teamId, eventId).report()
    fun submitChallenge(teamId: String) = repo.submitChallenge(teamId).report()
    fun addCoachByEmail(teamId: String, email: String) = repo.addCoachByEmail(teamId, email).report()
    fun removeCoach(teamId: String, userId: String) = repo.removeCoach(teamId, userId).report()

    // ---- staff ----

    fun startReview(teamId: String) = repo.startReview(teamId).report()
    fun approveTeam(teamId: String) = repo.approveTeam(teamId).report()
    fun requestChanges(teamId: String, note: String) = repo.requestChanges(teamId, note).report()
    fun sendConsentReminder(teamId: String) = repo.sendConsentReminder(teamId).report()
    fun approveChallenge(teamId: String) = repo.approveChallenge(teamId).report()
    fun decideEntry(teamId: String, index: Int, decision: EntryStatus) = repo.decideEntry(teamId, index, decision).report()

    fun setRole(userId: String, role: Role) = repo.setRole(userId, role).report()
    fun setActive(userId: String, active: Boolean) = repo.setActive(userId, active).report()

    fun saveEvent(event: SportsEvent) = repo.saveEvent(event).report()
    fun setEventPublished(id: String, published: Boolean) = repo.setEventPublished(id, published).report()
    fun deleteEvent(id: String) = repo.deleteEvent(id).report()

    fun saveArticle(article: Article) = repo.saveArticle(article).report()
    fun setArticlePublished(id: String, published: Boolean) = repo.setArticlePublished(id, published).report()
    fun deleteArticle(id: String) = repo.deleteArticle(id).report()

    fun addResource(name: String, kind: ResourceKind, contact: String, notes: String) =
        repo.addResource(name, kind, contact, notes).report()
    fun verifyResource(id: String) = repo.verifyResource(id).report()
    fun publishResource(id: String) = repo.publishResource(id).report()
    fun unpublishResource(id: String) = repo.unpublishResource(id).report()
    fun deleteResource(id: String) = repo.deleteResource(id).report()

    /** Debug builds only: fills the app with clearly marked sample events, articles and helplines. */
    fun addSampleContent() = repo.addSampleContent().report()
}
