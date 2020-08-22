package com.roteswasser.spotifysync

import org.springframework.data.repository.CrudRepository

interface SpotifySyncUserRepository : CrudRepository<SpotifySyncUser, String>

interface SyncJobRepository : CrudRepository<SyncJob, String>