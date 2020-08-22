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
        var active: Boolean,
        var amountToSync: Int,
        @ManyToOne var owner: SpotifySyncUser
) {
    override fun equals(other: Any?) = other is SyncJob && other.targetPlaylistId == targetPlaylistId
    override fun hashCode() = targetPlaylistId.hashCode()
}