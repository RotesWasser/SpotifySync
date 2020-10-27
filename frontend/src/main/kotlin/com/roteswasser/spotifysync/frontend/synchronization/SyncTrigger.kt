package com.roteswasser.spotifysync.frontend.synchronization

import com.roteswasser.spotifysync.common.entities.SyncJob

interface SyncTrigger {
    fun queueSync(toRun: SyncJob)
}