package com.roteswasser.spotifysync.repositories

import com.roteswasser.spotifysync.entities.SpotifySyncUser
import com.roteswasser.spotifysync.entities.SyncJob
import org.springframework.data.repository.CrudRepository

interface SyncJobRepository : CrudRepository<SyncJob, String> {
    fun findByOwner(owner: SpotifySyncUser): List<SyncJob>
}