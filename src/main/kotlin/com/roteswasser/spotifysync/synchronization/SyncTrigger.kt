package com.roteswasser.spotifysync.synchronization

import com.roteswasser.spotifysync.entities.SyncJob

interface SyncTrigger {
    fun queueSync(toRun: SyncJob)
}