package com.roteswasser.spotifysync.controllers

import com.roteswasser.spotifysync.oauth.OAuth2SpotifySyncUser
import com.roteswasser.spotifysync.repositories.SpotifySyncUserRepository
import com.roteswasser.spotifysync.repositories.SyncJobRepository
import com.roteswasser.spotifysync.synchronization.SyncTrigger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@Controller
class ActionsController(
        private val syncJobRepository: SyncJobRepository,
        private val syncTrigger: SyncTrigger) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/api/syncjobs/{playlistId}/trigger")
    fun triggerSync(@PathVariable(name = "playlistId") playlistId: String, response: HttpServletResponse): String? {
        try {
            val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2SpotifySyncUser
                    ?: throw Exception("Somehow got a non-Spotify sync user Principal!")

            val syncJob = syncJobRepository.findById(playlistId).get()

            if (syncJob.owner.id != principal.name) {
               logger.info("${principal.name} tried to trigger a sync that does not belong to them")
                response.sendError(404)
                return null
            }

            syncTrigger.queueSync(syncJob)

            return "redirect:/configuration"
        } catch (ex: NoSuchElementException) {
            logger.info("Tried to trigger a sync job that does not exist", ex)
            response.sendError(404)
            return null
        }
    }
}