package com.roteswasser.spotifysync.common.repositories

import com.roteswasser.spotifysync.common.entities.SpotifySyncUser
import com.roteswasser.spotifysync.common.entities.SyncJob
import org.springframework.data.repository.CrudRepository

interface SyncJobRepository : CrudRepository<SyncJob, String> {
    fun findByOwner(owner: SpotifySyncUser): List<SyncJob>
}