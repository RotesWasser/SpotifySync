package com.roteswasser.spotifysync.frontend.controllers

import com.roteswasser.spotifysync.common.repositories.SyncJobRepository
import com.roteswasser.spotifysync.frontend.oauth.OAuth2SpotifySyncUser
import com.roteswasser.spotifysync.frontend.synchronization.SyncTrigger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import java.time.Instant
import javax.servlet.http.HttpServletResponse
import javax.transaction.Transactional

@Controller
class ActionsController(
        private val syncJobRepository: SyncJobRepository,
        private val syncTrigger: SyncTrigger) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/api/syncjobs/{playlistId}/trigger")
    fun triggerSync(@PathVariable(name = "playlistId") playlistId: String, response: HttpServletResponse): String? {
        val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

        try {
            val syncJob = syncJobRepository.findById(playlistId).get()

            if (syncJob.owner.id != principal.name) {
               logger.info("${principal.name} tried to trigger a sync that does not belong to them")
                response.sendError(404)
                return null
            }

            syncTrigger.queueSync(syncJob)

            return "redirect:/configuration"
        } catch (ex: NoSuchElementException) {
            logger.info("${principal.name} tried to trigger a sync job that does not exist", ex)
            response.sendError(404)
            return null
        }
    }

    @PostMapping("/api/syncjobs/{playlistId}/pause")
    fun pauseSync(@PathVariable(name = "playlistId") playlistId: String, response: HttpServletResponse)
        = setPausedState(playlistId, true, response)

    @PostMapping("/api/syncjobs/{playlistId}/unpause")
    fun unpauseSync(@PathVariable(name = "playlistId") playlistId: String, response: HttpServletResponse)
            = setPausedState(playlistId, false, response)


    @Transactional
    fun setPausedState(playlistId: String, shouldPause: Boolean, response: HttpServletResponse): String? {
        val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2SpotifySyncUser
                ?: throw Exception("Somehow got a non-Spotify sync user Principal!")
        try {
            val syncJob = syncJobRepository.findById(playlistId).get()

            if (syncJob.owner.id != principal.name) {
                logger.info("${principal.name} tried to ${if (shouldPause) "pause" else "unpause"} a sync that does not belong to them")
                response.sendError(404)
                return null
            }

            syncJob.apply {
                syncPausedByOwner = shouldPause
                syncPauseTime = if (shouldPause) Instant.now() else null
                syncJobRepository.save(this)
            }

            if (!shouldPause) {
                syncTrigger.queueSync(syncJob)
            }

            return "redirect:/configuration"
        } catch (ex: NoSuchElementException) {
            logger.info("${principal.name} tried to ${if (shouldPause) "pause" else "unpause"} a sync job that does not exist", ex)
            response.sendError(404)
            return null
        }
    }
}