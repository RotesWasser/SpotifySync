package com.roteswasser.spotifysync

import javax.persistence.*

@Entity
data class SpotifySyncUser(
    @Id var id: String,
    var displayName: String,
    @OneToMany(mappedBy = "owner") var syncJobs: Set<SyncJob>
)

@Entity
data class SyncJob(
    @Id var targetPlaylistId: String,
    var active : Boolean,
    var amountToSync: Int,
    @ManyToOne var owner: SpotifySyncUser
)