package com.roteswasser.spotifysync.frontend.controllers

import com.roteswasser.spotifysync.common.entities.SyncJob
import com.roteswasser.spotifysync.common.extensions.formatAsAgo
import com.roteswasser.spotifysync.common.repositories.SpotifySyncUserRepository
import com.roteswasser.spotifysync.common.repositories.SyncJobRepository
import com.roteswasser.spotifysync.frontend.SpotifyConnectionBuilder
import com.roteswasser.spotifysync.frontend.oauth.OAuth2SpotifySyncUser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.view.RedirectView
import java.time.Duration
import java.time.Instant

@Controller
class ConfigurationController(
        userRepository: SpotifySyncUserRepository,
        private val syncJobRepository: SyncJobRepository,
        private val spotifyConnectionBuilder: SpotifyConnectionBuilder
) : SpotifySyncCustomController(userRepository) {

    @GetMapping("/configuration")
    fun configDisplay(model: Model): String {
        populateHeaderFields(model)

        val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

        val user = userRepository.findById(principal.name).get()

        val now = Instant.now()

        model["activeSyncJobs"] = user.syncJobs.filter { !it.playlistDeletedByOwner && !it.syncPausedByOwner }.map { UISyncJobRepresentation.fromSyncJob(it, now) }
        model["pausedSyncJobs"] = user.syncJobs.filter { !it.playlistDeletedByOwner && it.syncPausedByOwner }.map { UISyncJobRepresentation.fromSyncJob(it, now) }
        model["inactiveSyncJobs"] = user.syncJobs.filter { it.playlistDeletedByOwner }.map { UISyncJobRepresentation.fromSyncJob(it, now) }
        model["createSyncJobFormData"] = CreateNewSyncJobFormData(50)

        return "configuration"
    }

    @PostMapping("/configuration")
    fun createNewSyncJob(@ModelAttribute createSyncJobFormData: CreateNewSyncJobFormData, model: Model): RedirectView {
        val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

        val user = userRepository.findById(principal.name).get()
        val spotifyConnection = spotifyConnectionBuilder.getClient(user.id)

        val createdPlaylist = spotifyConnection.createPlaylistForMyself(
                playlistName = "Most Recent ${createSyncJobFormData.amount} Liked Songs",
                playlistDescription = "This playlist is managed automatically by the Spotify Sync application. \n" +
                        " It contains your most recent ${createSyncJobFormData.amount} liked songs."
        )

        val syncJob = SyncJob(
                targetPlaylistId = createdPlaylist.id,
                playlistName = createdPlaylist.name,
                amountToSync = createSyncJobFormData.amount,
                lastSync = null,
                playlistDeletedByOwner = false,
                playlistDeletionTime = null,
                syncPausedByOwner = false,
                syncPauseTime = null,
                owner = user,
                markedForImmediateSync = true)

        syncJobRepository.save(syncJob)

        return RedirectView("/configuration")
    }

    data class CreateNewSyncJobFormData(
            var amount: Int
    )

    data class UISyncJobRepresentation (
            val syncJob: SyncJob,
            val formattedLastSync: String
    ) {
        companion object {
            fun fromSyncJob(syncJob: SyncJob, currentTime: Instant) = UISyncJobRepresentation(
                    syncJob,
                    formatAgo(syncJob, currentTime)
            )

            private fun formatAgo(syncJob: SyncJob, currentTime: Instant) : String {
                return if (syncJob.lastSync == null)
                    "never"
                else
                    Duration.between(syncJob.lastSync, currentTime).formatAsAgo()
            }
        }
    }
}