package com.roteswasser.spotifysync.common.entities

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class SpotifySyncUser(
        @Id var id: String,
        var displayName: String,
        var invalidSpotifyCredentials: Boolean,
        @OneToMany(mappedBy = "owner") var syncJobs: Set<SyncJob>
)