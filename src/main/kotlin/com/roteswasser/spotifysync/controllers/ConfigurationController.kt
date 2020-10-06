package com.roteswasser.spotifysync.controllers

import com.roteswasser.spotifysync.oauth.OAuth2SpotifySyncUser
import com.roteswasser.spotifysync.SpotifyConnectionBuilder
import com.roteswasser.spotifysync.entities.SyncJob
import com.roteswasser.spotifysync.repositories.SpotifySyncUserRepository
import com.roteswasser.spotifysync.repositories.SyncJobRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.view.RedirectView

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

        model["activeSyncJobs"] = user.syncJobs.filter { !it.playlistDeletedByOwner && !it.syncPausedByOwner }
        model["pausedSyncJobs"] = user.syncJobs.filter { !it.playlistDeletedByOwner && it.syncPausedByOwner }
        model["inactiveSyncJobs"] = user.syncJobs.filter { it.playlistDeletedByOwner }
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
                owner = user)

        syncJobRepository.save(syncJob)

        return RedirectView("/configuration")
    }

    data class CreateNewSyncJobFormData(
            var amount: Int
    )
}