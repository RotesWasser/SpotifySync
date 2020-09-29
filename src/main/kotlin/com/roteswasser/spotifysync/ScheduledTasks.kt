package com.roteswasser.spotifysync

import com.roteswasser.spotifysync.algorithms.ListDiff
import com.roteswasser.spotifysync.algorithms.computeLCS
import com.roteswasser.spotifysync.entities.SyncJob
import com.roteswasser.spotifysync.repositories.SpotifySyncUserRepository
import com.roteswasser.spotifysync.repositories.SyncJobRepository
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

    @Scheduled(fixedRate = 1000 * 60)
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

        logger.info("Sync Job Completed!")
    }

    fun doSync(syncJobs: List<SyncJob>) = syncJobs.map { doSync(it) }

    fun doSync(syncJob: SyncJob) {
        // get the most recently saved songs from spotify
        val latestSongs = spotifyConnection.getMySavedSongs(syncJob.owner.id, syncJob.amountToSync).map { it.track }
        val itemsInPlaylist = spotifyConnection.getPlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId).map { it.track }

        val playlistDiff = ListDiff.createFrom(itemsInPlaylist, latestSongs)

        // Coalesce into API Requests
        val additionRequests = playlistDiff.additions.map { addition -> addition.elements.chunked(100).map { chunk -> SpotifyConnection.AdditionRequest(addition.position, chunk.map { it.uri }) } }.flatten()
        val deletionBatches = playlistDiff.removals.map { SpotifyConnection.TrackDeletion(it.element.uri, listOf(it.position)) }.chunked(100)

        // Execute deletions
        var snapshotId: String? = null
        for(deletionBatch in deletionBatches)
            snapshotId = spotifyConnection.deletePlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId, deletionBatch, snapshotId).snapshot_id

        // Execute additions
        for (additionRequest in additionRequests.reversed())
            spotifyConnection.addPlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId, additionRequest.position, additionRequest.uris)

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
        // TODO
    }
}