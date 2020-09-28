package com.roteswasser.spotifysync.controllers

import com.roteswasser.spotifysync.OAuth2SpotifySyncUser
import com.roteswasser.spotifysync.SpotifyConnection
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
        private val userRepository: SpotifySyncUserRepository,
        private val syncJobRepository: SyncJobRepository,
        private val spotifyConnection: SpotifyConnection
) {

    @GetMapping("/configuration")
    fun configDisplay(model: Model): String {

        val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

        val user = userRepository.findById(principal.name).get()



        model["displayName"] = user.displayName
        model["syncJobs"] = user.syncJobs

        model["createSyncJobFormData"] = CreateNewSyncJobFormData(50)

        return "configuration"
    }

    @PostMapping("/configuration")
    fun createNewSyncJob(@ModelAttribute createSyncJobFormData: CreateNewSyncJobFormData, model: Model): RedirectView {
        val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

        val user = userRepository.findById(principal.name).get()

        val createdPlaylist = spotifyConnection.createPlaylistForUser(
                principalName = user.id,
                userId = user.id,
                playlistName = "Most Recent ${createSyncJobFormData.amount} Saved Songs",
                playlistDescription = "This playlist is managed automatically by the Spotify Sync application. \n" +
                        " It contains the most recent ${createSyncJobFormData.amount} Songs from your saved songs."
        )

        val syncJob = SyncJob(
                targetPlaylistId = createdPlaylist.id,
                amountToSync = createSyncJobFormData.amount,
                lastSync = null,
                playlistDeletedByOwner = false,
                playlistDeletionTime = null,
                owner = user)

        syncJobRepository.save(syncJob)

        return RedirectView("/configuration")
    }

    data class CreateNewSyncJobFormData(
            var amount: Int
    )
}