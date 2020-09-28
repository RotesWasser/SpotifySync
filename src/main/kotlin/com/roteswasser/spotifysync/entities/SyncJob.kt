package com.roteswasser.spotifysync.entities

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class SyncJob(
        @Id
        var targetPlaylistId: String,
        var amountToSync: Int,
        var lastSync: Instant?,

        // Checked periodically by a scheduled job. Playlists can be deleted and later
        // potentially undeleted by support.
        var playlistDeletedByOwner: Boolean,
        var playlistDeletionTime: Instant?,

        @ManyToOne var owner: SpotifySyncUser
) {
    override fun equals(other: Any?) = other is SyncJob && other.targetPlaylistId == targetPlaylistId
    override fun hashCode() = targetPlaylistId.hashCode()
}