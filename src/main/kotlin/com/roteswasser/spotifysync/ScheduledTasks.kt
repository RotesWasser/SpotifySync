package com.roteswasser.spotifysync

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class ScheduledTasks(
        private val syncJobRepository: SyncJobRepository,
        private val userRepository: SpotifySyncUserRepository,
        private val spotifyConnection: SpotifyConnection
) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(fixedRate = 1000)
    @Transactional
    fun syncPlaylists() {
        logger.info("Starting Sync Job!")

        for (user in userRepository.findAll()) {
            val playlists = spotifyConnection.getMyPlaylists(user.id)

            // TODO: Better database query!
            for (syncJob in user.syncJobs) {
                if (!syncJob.playlistDeletedByOwner && !syncJob.failedBecauseOfInvalidCredentials) {
                    if (syncJob.targetPlaylistId !in playlists.map { it.id }) {
                        syncJob.playlistDeletedByOwner = true
                        syncJobRepository.save(syncJob)
                        continue
                    }

                    doSync(syncJob)
                }
            }
        }
    }

    fun doSync(syncJob: SyncJob) {
        // get the most recently saved songs from spotify

        // replace target playlist items
    }
}