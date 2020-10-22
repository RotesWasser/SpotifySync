package com.roteswasser.spotifysync.synchronization.hosted

import com.roteswasser.spotifysync.entities.SyncJob
import com.roteswasser.spotifysync.synchronization.SyncTrigger
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HostedSyncTrigger : SyncTrigger {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun queueSync(toRun: SyncJob) {
        logger.info("Got a request for a manual sync of ${toRun.playlistName}")
    }
}