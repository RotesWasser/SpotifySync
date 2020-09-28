package com.roteswasser.spotifysync

import com.roteswasser.spotifysync.entities.SpotifySyncUser
import com.roteswasser.spotifysync.entities.SyncJob
import org.springframework.data.repository.CrudRepository

interface SpotifySyncUserRepository : CrudRepository<SpotifySyncUser, String>

interface SyncJobRepository : CrudRepository<SyncJob, String> {
    fun findByOwner(owner: SpotifySyncUser): List<SyncJob>
}