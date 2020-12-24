package com.roteswasser.spotifysync.syncservice

import com.roteswasser.spotifysync.common.repositories.SyncJobRepository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Coordinates access to the current set of queued syncs.
 *
 * All methods here need to be open for Spring AOP to be able to create a proxy class
 */
open class SyncCoordinator(
        private val syncJobRepository: SyncJobRepository
) {
    private val currentlyQueued = mutableSetOf<String>()
    private val currentlyQueuedSetLock = ReentrantLock()

    @Transactional(isolation = Isolation.SERIALIZABLE)
    open fun markPlaylistAsQueued(targetId: String) {
        val syncJob = syncJobRepository.findById(targetId).get()
        syncJob.markedForImmediateSync = true
        syncJobRepository.save(syncJob)
    }

    open fun enqueuePlaylistForSync(targetId: String) {
        currentlyQueuedSetLock.withLock {
            if (currentlyQueued.contains(targetId)) {
                return@enqueuePlaylistForSync
            } else {
                markPlaylistAsQueued(targetId)
                currentlyQueued.add(targetId)
            }
        }

    }
}