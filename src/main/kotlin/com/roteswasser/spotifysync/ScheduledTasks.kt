package com.roteswasser.spotifysync

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
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
            try {
                val playlistIds = spotifyConnection.getMyPlaylists(user.id).map { it.id }

                // Determine living sync jobs, i.e. the playlists still registered to the users account
                val livingSyncJobs = user.syncJobs.filter { it.targetPlaylistId in playlistIds }
                doSync(livingSyncJobs)

                // Determine dead sync jobs, those that aren't registered to the users account anymore
                val deadSyncJobs = user.syncJobs.filter { it.targetPlaylistId !in playlistIds }
                processDeadSyncJobs(deadSyncJobs)

            } catch (ex: SpotifyConnection.SpotifyCredentialsException) {
                // Flag user account credentials as expired
                user.apply {
                    invalidSpotifyCredentials = true
                    userRepository.save(this)
                }
            }
        }
    }

    fun doSync(syncJobs: List<SyncJob>) = syncJobs.map { doSync(it) }

    fun doSync(syncJob: SyncJob) {
        // get the most recently saved songs from spotify
        val latestSongs = spotifyConnection.getMySavedSongs(syncJob.owner.id, syncJob.amountToSync)
        val itemsInPlaylist = spotifyConnection.getPlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId)

        val first100LatestSongs = latestSongs.take(100)
        var remainingLatestSongs = latestSongs.drop(100)

        // replace target playlist items (at most 100, as Spotify's API does not allow for more in a replace request)
        spotifyConnection.replacePlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId, first100LatestSongs.map { it.track.uri }).snapshot_id

        while (remainingLatestSongs.isNotEmpty()) {
            val additionsThisRound = remainingLatestSongs.take(100)
            spotifyConnection.addPlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId, null, additionsThisRound.map { it.track.uri })

            remainingLatestSongs = remainingLatestSongs.drop(100)
        }

        // update SyncJob
        syncJob.apply {
            lastSync = Instant.now()
            playlistDeletionTime = null
            playlistDeletedByOwner = false
            syncJobRepository.save(this)
        }
    }

    fun processDeadSyncJobs(syncJobs: List<SyncJob>) {

        // Only update the state of newly failed ones
        val newlyFailedJobs = syncJobs.filter { !it.playlistDeletedByOwner }

        for (syncJob in newlyFailedJobs) {
            syncJob.apply {
                playlistDeletedByOwner = true
                playlistDeletionTime = Instant.now()
                syncJobRepository.save(this)
            }
        }

        if (newlyFailedJobs.isNotEmpty())
            notifyUserAboutDeadJobs(newlyFailedJobs)
    }

    private fun notifyUserAboutDeadJobs(newlyFailedJobs: List<SyncJob>) {
        TODO("Not yet implemented")
    }
}