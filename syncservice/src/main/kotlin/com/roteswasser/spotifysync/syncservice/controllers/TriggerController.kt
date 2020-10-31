package com.roteswasser.spotifysync.syncservice.controllers

import com.roteswasser.spotifysync.common.dto.ManualSyncTriggerResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TriggerController {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/trigger/{playlistId}")
    fun trigger(@PathVariable("playlistId") playlistId: String): ManualSyncTriggerResponse {
        logger.info("Received request to sync $playlistId")

        return ManualSyncTriggerResponse(playlistId, true)
    }

}