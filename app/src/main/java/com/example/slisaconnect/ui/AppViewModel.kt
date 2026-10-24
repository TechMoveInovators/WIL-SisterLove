package com.example.slisaconnect.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import za.org.slisa.connect.BuildConfig
import za.org.slisa.connect.data.AppRepository
import za.org.slisa.connect.data.SeedData
import za.org.slisa.connect.model.*

/** One instance shared by every screen (it is scoped to the activity). */
class AppViewModel(private val repo: AppRepository = AppRepository(startingState())) : ViewModel() {
    val state = repo.state

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private fun Outcome.report(): Outcome = also { _messages.tryEmit(message) }

    fun toast(text: String) { _messages.tryEmit(text) }

    // Prototype only
    fun switchUser(id: String) = repo.switchUser(id)

    fun startReview(teamId: String) = repo.startReview(teamId).report()
    fun approveTeam(teamId: String) = repo.approveTeam(teamId).report()
    fun requestChanges(teamId: String, note: String) = repo.requestChanges(teamId, note).report()
    fun approveChallenge(teamId: String) = repo.approveChallenge(teamId).report()
    fun decideEntry(teamId: String, index: Int, decision: EntryStatus) =
        repo.decideEntry(teamId, index, decision).report()

    fun setRole(userId: String, role: Role) = repo.setRole(userId, role).report()
    fun setActive(userId: String, active: Boolean) = repo.setActive(userId, active).report()

    fun saveEvent(event: SportsEvent) = repo.saveEvent(event).report()
    fun setEventPublished(id: String, published: Boolean) = repo.setEventPublished(id, published).report()
    fun deleteEvent(id: String) = repo.deleteEvent(id).report()

    fun verifyResource(id: String) = repo.verifyResource(id).report()
    fun publishResource(id: String) = repo.publishResource(id).report()

    fun updateProfile(profile: UserProfile) = repo.updateProfile(profile).report()

    // Team managers and coaches
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
}

/**
 * Debug builds open as the platform admin so every screen can be tried. Release builds open as a plain member
 * (staff tools hidden) until real sign-in replaces the demo users.
 */
private fun startingState(): AppState =
    SeedData.initial().let { if (BuildConfig.DEBUG) it else it.copy(currentUserId = "u6") }
