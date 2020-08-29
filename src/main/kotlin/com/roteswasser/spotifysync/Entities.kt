package com.roteswasser.spotifysync

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class SpotifySyncUser(
    @Id var id: String,
    var displayName: String,
    @OneToMany(mappedBy = "owner") var syncJobs: Set<SyncJob>
)

@Entity
data class SyncJob(
        @Id
        var targetPlaylistId: String,

        // Failure because of expired / invalid credentials
        var failedBecauseOfInvalidCredentials: Boolean,

        // Checked periodically by a scheduled job. Playlists can be deleted and later
        // potentially undeleted by support.
        var playlistDeletedByOwner: Boolean,
        var amountToSync: Int,
        @ManyToOne var owner: SpotifySyncUser
) {
    override fun equals(other: Any?) = other is SyncJob && other.targetPlaylistId == targetPlaylistId
    override fun hashCode() = targetPlaylistId.hashCode()
}