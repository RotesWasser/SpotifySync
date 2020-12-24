package com.roteswasser.spotifysync.syncservice

import com.roteswasser.spotifysync.common.repositories.SyncJobRepository
import org.springframework.context.annotation.Bean
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

    fun enqueuePlaylistForSync(targetId: String) {
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