package com.roteswasser.spotifysync

import com.roteswasser.spotifysync.algorithms.computeLCS
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
        val latestSongs = spotifyConnection.getMySavedSongs(syncJob.owner.id, syncJob.amountToSync).take(syncJob.amountToSync)
        val itemsInPlaylist = spotifyConnection.getPlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId)

        // TODO: Optimize by checking the lists backwards to avoid having to compute too much
        val lcs = computeLCS(latestSongs.map{it.track}, itemsInPlaylist.map{it.track})

        // delete items in the playlist that don't belong
        val deletions = mutableListOf<SpotifyConnection.TrackDeletion>()
        var lcsHead = 0
        for (i in itemsInPlaylist.indices) {
            if (lcsHead < lcs.size && itemsInPlaylist[i].track == lcs[lcsHead]) {
                lcsHead++
            } else {
                deletions.add(SpotifyConnection.TrackDeletion(itemsInPlaylist[i].track.uri, listOf(i)))
            }
        }

        // Find track addition locations
        val additions = HashMap<Int, MutableList<SpotifyConnection.Track>>()
        lcsHead = 0
        for (i in latestSongs.indices) {
            val currentTrack = latestSongs[i].track

            if (lcsHead >= lcs.size || currentTrack != lcs[lcsHead]) {
                if (additions.containsKey(lcsHead))
                    additions[lcsHead]!!.add(currentTrack)
                else
                    additions[lcsHead] = mutableListOf(currentTrack)
            } else {
                lcsHead++
            }
        }

        // Coalesce into API Requests
        val additionRequests = additions.map { it -> SpotifyConnection.AdditionRequest(it.key, it.value.map { it.uri }) }
        val deletionBatches = deletions.chunked(100)

        var snapshotId: String? = null
        for(deletionBatch in deletionBatches)
            snapshotId = spotifyConnection.deletePlaylistItems(syncJob.owner.id, syncJob.targetPlaylistId, deletionBatch, snapshotId).snapshot_id

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
        TODO("Not yet implemented")
    }
}